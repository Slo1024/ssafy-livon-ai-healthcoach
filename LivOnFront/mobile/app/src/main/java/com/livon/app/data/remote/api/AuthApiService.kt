package com.livon.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

// Request for sign-in
data class SignInRequest(val email: String, val password: String)

// Result object inside the API wrapper
data class SignInResult(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val refreshTokenExpirationTime: Long? = null,
    val role: List<String>? = null
)

// Generic API wrapper used by backend
data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String? = null,
    val message: String? = null,
    val result: T? = null
)

interface AuthApiService {
    @POST("user/sign-in")
    suspend fun signIn(@Body req: SignInRequest): ApiResponse<SignInResult>
}
