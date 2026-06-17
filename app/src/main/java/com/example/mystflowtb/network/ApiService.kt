package com.example.mystflowtb.network

import com.example.mystflowtb.AiInsightResponse
import com.example.mystflowtb.ChatMessageRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// supabase updated
interface ApiService {
    @GET("ai/insights/{user_id}")
    suspend fun getUserInsight(
        @Path("user_id") userId: String
    ): Response<AiInsightResponse>


    @POST("ai/chat")
    suspend fun getChatResponse(
        @Body request: ChatMessageRequest
    ): Response<AiInsightResponse>
}