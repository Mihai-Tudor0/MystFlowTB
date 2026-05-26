package com.example.mystflowtb

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {

    var insightMessage = mutableStateOf("Se analizează securitatea contului...")
        private set

    fun fetchInsight(userId: Int) {
        viewModelScope.launch {
            try {

                val response = ApiClient.apiService.getUserInsight(userId)

                if (response.isSuccessful && response.body() != null) {
                    insightMessage.value = response.body()!!.insight
                } else {
                    insightMessage.value = "Eroare la preluarea datelor de la asistent."
                }
            } catch (e: Exception) {
                insightMessage.value = "Nu s-a putut conecta la serverul AI."
            }
        }
    }
}