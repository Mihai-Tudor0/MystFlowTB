package com.example.mystflowtb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystflowtb.data.model.CardLookupResult
import com.example.mystflowtb.data.model.Transaction
import com.example.mystflowtb.data.model.UserProfile
import com.example.mystflowtb.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for banking operations (top-up, transfer).
 */
sealed class BankingUiState {
    data object Idle : BankingUiState()
    data object Loading : BankingUiState()
    data class Success(val message: String) : BankingUiState()
    data class Error(val message: String) : BankingUiState()
}

/**
 * ViewModel for banking operations: top-up, transfer, and transaction history.
 *
 * Shares the same [AuthRepository] instance as [AuthViewModel] to ensure
 * consistent state across the app.
 */
class BankingViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _bankingState = MutableStateFlow<BankingUiState>(BankingUiState.Idle)
    val bankingState: StateFlow<BankingUiState> = _bankingState.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _recipientLookup = MutableStateFlow<CardLookupResult?>(null)
    val recipientLookup: StateFlow<CardLookupResult?> = _recipientLookup.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    // ========================================================================
    // 1. TOP-UP
    // ========================================================================

    /**
     * Top up the current user's account.
     * After success, refreshes the profile to get the updated balance.
     */
    fun topUp(amount: Double) {
        viewModelScope.launch {
            _bankingState.value = BankingUiState.Loading
            try {
                repository.topUp(amount)
                refreshProfile()
                _bankingState.value = BankingUiState.Success(
                    "Contul a fost alimentat cu %.2f RON".format(amount)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _bankingState.value = BankingUiState.Error(
                    "Eroare la alimentare: ${e.message}"
                )
            }
        }
    }

    // ========================================================================
    // 2. TRANSFER
    // ========================================================================

    /**
     * Transfer money to another user by their card number.
     * After success, refreshes the profile to get the updated balance.
     */
    fun transfer(recipientCardNumber: String, amount: Double) {
        viewModelScope.launch {
            _bankingState.value = BankingUiState.Loading
            try {
                // Clean card number (remove spaces)
                val cleanCard = recipientCardNumber.replace(" ", "")
                repository.transfer(cleanCard, amount)
                refreshProfile()
                loadTransactions()
                _bankingState.value = BankingUiState.Success(
                    "Transfer de %.2f RON efectuat cu succes!".format(amount)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = when {
                    e.message?.contains("Insufficient", ignoreCase = true) == true ->
                        "Fonduri insuficiente"
                    e.message?.contains("not found", ignoreCase = true) == true ->
                        "Numărul de card nu a fost găsit"
                    e.message?.contains("yourself", ignoreCase = true) == true ->
                        "Nu poți transfera către propriul cont"
                    else -> "Eroare la transfer: ${e.message}"
                }
                _bankingState.value = BankingUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Look up a recipient by card number for transfer preview.
     * Shows the recipient's name before confirming the transfer.
     */
    fun lookupRecipient(cardNumber: String) {
        viewModelScope.launch {
            try {
                val cleanCard = cardNumber.replace(" ", "")
                if (cleanCard.length == 16) {
                    val result = repository.lookupCardNumber(cleanCard)
                    _recipientLookup.value = result
                } else {
                    _recipientLookup.value = null
                }
            } catch (e: Exception) {
                _recipientLookup.value = null
            }
        }
    }

    /**
     * Clears the recipient lookup state.
     */
    fun clearRecipientLookup() {
        _recipientLookup.value = null
    }

    // ========================================================================
    // 3. TRANSACTION HISTORY
    // ========================================================================

    /**
     * Loads the recent transaction history.
     */
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _transactions.value = repository.getTransactionHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========================================================================
    // 4. PROFILE REFRESH
    // ========================================================================

    /**
     * Refreshes the cached profile (used after top-up/transfer to update balance).
     */
    fun refreshProfile() {
        viewModelScope.launch {
            try {
                _currentProfile.value = repository.getCurrentProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Resets the banking UI state to Idle.
     */
    fun resetState() {
        _bankingState.value = BankingUiState.Idle
    }
}

/**
 * Factory for BankingViewModel — shares the same AuthRepository as AuthViewModel.
 */
class BankingViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
