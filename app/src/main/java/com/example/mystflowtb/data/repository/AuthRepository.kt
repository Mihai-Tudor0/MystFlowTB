package com.example.mystflowtb.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.mystflowtb.data.model.UserProfile
import com.example.mystflowtb.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import java.security.MessageDigest

/**
 * Single source of truth for authentication operations.
 *
 * Handles both Remote Auth (Supabase Phone OTP) and Local Auth (Biometric/PIN).
 */
class AuthRepository(context: Context) {

    private val supabase = SupabaseProvider.client
    private val auth = supabase.auth
    private val profilesTable = "profiles"

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
     * After successful OTP verification during Sign-Up, save the user's name/surname
     * to the public.profiles table.
     */
    suspend fun createProfile(name: String, surname: String, phoneNumber: String) {
        val user = auth.currentUserOrNull() ?: throw Exception("User not authenticated")
        
        val profile = UserProfile(
            id = user.id,
            name = name,
            surname = surname,
            phoneNumber = phoneNumber
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
    // 3. LOCAL AUTHENTICATION (PIN)
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
        // Do NOT clear the local PIN, so they can log back in locally if the session is still valid.
        // Actually, if we sign out of Supabase, we lose the session.
        // For a banking app, if they explicitly logout, we might want to keep the PIN for next time 
        // they sign in, but they will need OTP again to get a new session.
    }

    // --- Helpers ---

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
