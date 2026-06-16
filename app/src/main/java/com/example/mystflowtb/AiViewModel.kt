package com.example.mystflowtb

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.mystflowtb.network.ApiClient
import com.example.mystflowtb.AiInsightResponse

class AiViewModel : ViewModel() {

    var insightMessage = mutableStateOf("Se analizează securitatea contului...")
        private set

    var chatResponse = mutableStateOf("")
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

    fun fetchChatResponse(message: String) {
        viewModelScope.launch {
            try {
                chatResponse.value = ""
                val response = ApiClient.apiService.getChatResponse(message)
                if (response.isSuccessful && response.body() != null) {
                    chatResponse.value = response.body()!!.insight
                } else {
                    chatResponse.value = "MystBot nu a putut răspunde."
                }
            } catch (e: Exception) {
                chatResponse.value = "Probleme de conexiune cu MystBot."
            }
        }
    }
}