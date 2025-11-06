package com.livon.app.domain.repository

import com.livon.app.data.remote.api.AuthApiService
import com.livon.app.data.remote.api.AuthResponse
import com.livon.app.data.remote.api.EmailLoginRequest


class AuthRepository(private val api: AuthApiService) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val res = api.login(EmailLoginRequest(email, password))
            Result.success(res)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}