package com.example.mystflowtb.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystflowtb.data.model.UserProfile
import com.example.mystflowtb.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    
    // OTP Flow
    data object OtpSent : AuthUiState()
    data object OtpVerified : AuthUiState()
    data object ProfileCreated : AuthUiState()
    
    // Final Success
    data class Success(val user: UserProfile?) : AuthUiState()
    
    // Error
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // ========================================================================
    // 1. SESSION CHECK (Daily Login)
    // ========================================================================

    /**
     * Called when the app starts.
     * Returns true if the user has an active Supabase session AND a local PIN is set.
     * If true, the UI should jump straight to the Biometric/Local PIN screen.
     */
    fun hasActiveSessionAndLocalPin(): Boolean {
        return repository.hasActiveSession() && repository.hasLocalPin()
    }

    /**
     * Verifies the local PIN (used for daily login or fallback).
     */
    fun verifyLocalPin(pin: String) {
        if (repository.verifyLocalPin(pin)) {
            // Fetch profile quietly in the background if needed
            viewModelScope.launch {
                val profile = repository.getCurrentProfile()
                _currentUser.value = profile
                _authState.value = AuthUiState.Success(profile)
            }
        } else {
            _authState.value = AuthUiState.Error("PIN incorect.")
        }
    }

    /**
     * Saves the local PIN during initial setup.
     */
    fun saveLocalPin(pin: String) {
        repository.saveLocalPin(pin)
        // Once PIN is saved, the setup is complete!
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            _currentUser.value = profile
            _authState.value = AuthUiState.Success(profile)
        }
    }

    // ========================================================================
    // 2. REMOTE OTP FLOW (Sign Up / Sign In)
    // ========================================================================

    fun sendOtp(phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                repository.sendOtp(phoneNumber)
                _authState.value = AuthUiState.OtpSent
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error("Eroare trimitere SMS: ${e.message}")
            }
        }
    }

    fun verifyOtp(phoneNumber: String, otpCode: String, isSignUp: Boolean, firstName: String = "", lastName: String = "") {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                repository.verifyOtp(phoneNumber, otpCode)
                
                if (isSignUp) {
                    // Create the profile in the database
                    repository.createProfile(firstName, lastName, phoneNumber)
                }
                
                _authState.value = AuthUiState.OtpVerified
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthUiState.Error("Database Error: ${e.message}")
            }
        }
    }

    // ========================================================================
    // 3. HELPERS
    // ========================================================================

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }

    fun signOut() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            _authState.value = AuthUiState.Idle
        }
    }
}

// ViewModel Factory needed to pass Context to AuthRepository
class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(AuthRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
