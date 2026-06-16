package com.example.mystflowtb.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.mystflowtb.AiInsightResponse

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String,
    val user_id: Int? = null
)

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @GET("/ai/insights/{user_id}")
    suspend fun getUserInsight(
        @Path("user_id") userId: Int
    ): Response<AiInsightResponse>

    @POST("ai/chat")
    suspend fun getChatResponse(
        @Query("message") message: String
    ): Response<AiInsightResponse>
}