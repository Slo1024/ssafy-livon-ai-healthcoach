package com.livon.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

data class EmailLoginRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String, val refreshToken: String? = null, val userId: String? = null)

interface AuthApiService {
    @POST("/auth/email/login")
    suspend fun login(@Body req: EmailLoginRequest): AuthResponse
}
