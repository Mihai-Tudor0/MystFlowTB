package com.example.mystflowtb.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.mystflowtb.data.model.CardLookupResult
import com.example.mystflowtb.data.model.Transaction
import com.example.mystflowtb.data.model.UserProfile
import com.example.mystflowtb.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.MessageDigest
import kotlin.random.Random

/**
 * Single source of truth for authentication and banking operations.
 *
 * Handles:
 * - Remote Auth (Supabase Phone OTP)
 * - Local Auth (Biometric/PIN)
 * - Profile management (name, card, balance)
 * - Banking operations (top-up, transfer, transaction history)
 */
class AuthRepository(context: Context) {

    private val supabase = SupabaseProvider.client
    private val auth = supabase.auth
    private val profilesTable = "profiles"
    private val transactionsTable = "transactions"

    // --- Local Storage for PIN ---
    // EncryptedSharedPreferences ensures the local PIN is stored securely.
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "mystflow_local_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_LOCAL_PIN = "local_pin"
        private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
        private const val CARD_PREFIX = "4821" // MystFlow card BIN prefix
    }

    // ========================================================================
    // 1. SUPABASE OTP AUTHENTICATION
    // ========================================================================

    /**
     * Step 1: Send SMS OTP to the phone number.
     * Works for both Sign-Up and Sign-In.
     */
    suspend fun sendOtp(phoneNumber: String) {
        auth.signInWith(OTP) {
            phone = phoneNumber
        }
    }

    /**
     * Step 2: Verify the SMS OTP code.
     * If successful, Supabase creates/restores the user session.
     */
    suspend fun verifyOtp(phoneNumber: String, otpCode: String) {
        auth.verifyPhoneOtp(
            type = io.github.jan.supabase.auth.OtpType.Phone.SMS,
            phone = phoneNumber,
            token = otpCode
        )
    }

    // ========================================================================
    // 2. REMOTE PROFILE MANAGEMENT
    // ========================================================================

    /**
     * After successful OTP verification during Sign-Up, save the user's first/last name
     * to the public.profiles table and generate a unique card number.
     */
    suspend fun createProfile(firstName: String, lastName: String, phoneNumber: String) {
        val user = auth.currentUserOrNull() ?: throw Exception("User not authenticated")
        
        val cardNumber = generateCardNumber()

        val profile = UserProfile(
            id = user.id,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            balance = 0.0,
            cardNumber = cardNumber
        )

        supabase.postgrest[profilesTable].insert(profile)
    }

    /**
     * Fetches the profile of the currently logged-in user.
     */
    suspend fun getCurrentProfile(): UserProfile? {
        val user = auth.currentUserOrNull() ?: return null
        val result = supabase.postgrest[profilesTable]
            .select {
                filter { eq("id", user.id) }
            }
            .decodeList<UserProfile>()
        return result.firstOrNull()
    }

    // ========================================================================
    // 3. CARD NUMBER GENERATION
    // ========================================================================

    /**
     * Generates a unique 16-digit card number with the MystFlow prefix (4821).
     * Format: "4821 XXXX XXXX XXXX" where X are random digits.
     *
     * The number is checked for uniqueness against the database.
     * Uses Luhn algorithm for a realistic card number.
     */
    private suspend fun generateCardNumber(): String {
        var attempts = 0
        while (attempts < 10) {
            val cardNumber = generateLuhnCardNumber()
            
            // Check uniqueness
            val existing = supabase.postgrest[profilesTable]
                .select {
                    filter { eq("card_number", cardNumber) }
                }
                .decodeList<UserProfile>()
            
            if (existing.isEmpty()) {
                return cardNumber
            }
            attempts++
        }
        throw Exception("Could not generate unique card number after 10 attempts")
    }

    /**
     * Generates a 16-digit card number that passes the Luhn check.
     * Prefix: 4821 (MystFlow)
     */
    private fun generateLuhnCardNumber(): String {
        // Start with prefix + 11 random digits = 15 digits
        val prefix = CARD_PREFIX
        val randomPart = buildString {
            repeat(11) {
                append(Random.nextInt(0, 10))
            }
        }
        val partial = prefix + randomPart // 15 digits

        // Calculate Luhn check digit
        val checkDigit = calculateLuhnCheckDigit(partial)
        return partial + checkDigit
    }

    /**
     * Calculates the Luhn check digit for a partial card number.
     */
    private fun calculateLuhnCheckDigit(partial: String): Int {
        var sum = 0
        var alternate = true // Start with doubling (from right to left of final number)
        for (i in partial.length - 1 downTo 0) {
            var n = partial[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return (10 - (sum % 10)) % 10
    }

    // ========================================================================
    // 4. BANKING OPERATIONS
    // ========================================================================

    /**
     * Top up the current user's account with the given amount.
     * Uses the Supabase RPC function `top_up` for atomic operation.
     */
    suspend fun topUp(amount: Double) {
        supabase.postgrest.rpc(
            "top_up",
            buildJsonObject {
                put("p_amount", amount)
            }
        )
    }

    /**
     * Transfer money to another user by their card number.
     * Uses the Supabase RPC function `perform_transfer` for atomic operation.
     *
     * @throws Exception if insufficient balance, recipient not found, or self-transfer
     */
    suspend fun transfer(recipientCardNumber: String, amount: Double) {
        supabase.postgrest.rpc(
            "perform_transfer",
            buildJsonObject {
                put("p_recipient_card", recipientCardNumber)
                put("p_amount", amount)
            }
        )
    }

    /**
     * Look up a recipient's name by their card number.
     * Uses the `lookup_card` RPC function so we don't need broad SELECT access.
     *
     * @return The recipient's name info, or null if not found
     */
    suspend fun lookupCardNumber(cardNumber: String): CardLookupResult? {
        val results = supabase.postgrest.rpc(
            "lookup_card",
            buildJsonObject {
                put("p_card_number", cardNumber)
            }
        ).decodeList<CardLookupResult>()
        return results.firstOrNull()
    }

    /**
     * Fetches the transaction history for the current user.
     * Returns the 20 most recent transactions (sent or received).
     */
    suspend fun getTransactionHistory(): List<Transaction> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return supabase.postgrest[transactionsTable]
            .select {
                filter {
                    or {
                        eq("sender_id", user.id)
                        eq("receiver_id", user.id)
                    }
                }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(20)
            }
            .decodeList<Transaction>()
    }

    /**
     * Returns the current user's balance.
     */
    suspend fun getBalance(): Double {
        val profile = getCurrentProfile()
        return profile?.balance ?: 0.0
    }

    // ========================================================================
    // 5. LOCAL AUTHENTICATION (PIN)
    // ========================================================================

    /**
     * Saves the 6-digit local PIN securely.
     */
    fun saveLocalPin(pin: String) {
        sharedPrefs.edit()
            .putString(KEY_LOCAL_PIN, hashPin(pin))
            .apply()
    }

    /**
     * Verifies the local PIN against the saved hash.
     */
    fun verifyLocalPin(pin: String): Boolean {
        val savedHash = sharedPrefs.getString(KEY_LOCAL_PIN, null) ?: return false
        return savedHash == hashPin(pin)
    }

    /**
     * Checks if a local PIN is already set up.
     */
    fun hasLocalPin(): Boolean {
        return sharedPrefs.contains(KEY_LOCAL_PIN)
    }

    /**
     * Checks if there's an active Supabase session.
     */
    fun hasActiveSession(): Boolean {
        return auth.currentUserOrNull() != null
    }

    /**
     * Logs the user out locally and remotely.
     */
    suspend fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignore network errors on logout
        }
    }

    // --- Helpers ---

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
