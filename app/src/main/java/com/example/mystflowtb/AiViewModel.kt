package com.example.mystflowtb

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.mystflowtb.network.ApiClient
import com.example.mystflowtb.AiInsightResponse
data class ChatMessageRequest(
    val message: String,
    val user_id: String
)
class AiViewModel : ViewModel() {

    var insightMessage = mutableStateOf("Se analizează securitatea contului...")
        private set

    var chatResponse = mutableStateOf("")
        private set

    fun fetchInsight(userId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getUserInsight(userId)
                if (response.isSuccessful && response.body() != null) {
                    insightMessage.value = response.body()!!.insight
                } else {
                    insightMessage.value = "Eroare server: ${response.code()}"
                }
            } catch (e: Exception) {
                insightMessage.value = "Nu s-a putut conecta la serverul AI."
            }
        }
    }

    fun fetchChatResponse(message: String, userId: String) {
        viewModelScope.launch {
            try {
                chatResponse.value = ""
                // Împachetăm datele exact cum le vrea Python
                val requestBody = ChatMessageRequest(message = message, user_id = userId)

                val response = ApiClient.apiService.getChatResponse(requestBody)
                if (response.isSuccessful && response.body() != null) {
                    chatResponse.value = response.body()!!.insight
                } else {
                    chatResponse.value = "Eroare chat: ${response.code()}"
                }
            } catch (e: Exception) {
                chatResponse.value = "Probleme de conexiune cu MystBot."
            }
        }
    }

    // Al doilea agent AI: Clasificator de tranzacții
    var transactionCategories = androidx.compose.runtime.mutableStateMapOf<String, String>()
        private set

    fun classifyTransaction(transactionId: String, description: String) {
        viewModelScope.launch {
            try {
                transactionCategories[transactionId] = "Analiză..."
                val prompt = "Ești un clasificator bancar. Răspunde doar cu UN SINGUR cuvânt care reprezintă categoria (ex: Food, Bills, Transfer, Entertainment, Income) pentru această tranzacție: $description"
                val response = ApiClient.apiService.getChatResponse(com.example.mystflowtb.ChatMessageRequest(message = prompt, user_id = "classifier"))
                if (response.isSuccessful && response.body() != null) {
                    transactionCategories[transactionId] = response.body()!!.insight.trim()
                } else {
                    transactionCategories[transactionId] = "Unknown"
                }
            } catch (e: Exception) {
                transactionCategories[transactionId] = "Error"
            }
        }
    }
}