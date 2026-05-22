package network

import retrofit2.http.Body
import retrofit2.http.POST


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

//interfata
interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
}