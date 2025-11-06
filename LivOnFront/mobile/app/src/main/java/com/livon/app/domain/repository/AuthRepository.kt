package com.livon.app.domain.repository

import android.util.Log
import com.livon.app.data.remote.api.AuthApiService
import com.livon.app.data.remote.api.SignInRequest
import com.livon.app.data.remote.api.SignInResult


class AuthRepository(private val api: AuthApiService) {
    suspend fun login(email: String, password: String): Result<SignInResult> {
        return try {
            val res = api.signIn(SignInRequest(email, password))
            Log.d("AuthRepository", "signIn response: isSuccess=${res.isSuccess}, code=${res.code}, message=${res.message}, resultPresent=${res.result != null}")
            if (res.isSuccess && res.result != null) {
                Result.success(res.result)
            } else {
                Log.d("AuthRepository", "signIn failed: ${res.message}")
                Result.failure(Exception(res.message ?: "login failed"))
            }
        } catch (t: Throwable) {
            Log.d("AuthRepository", "signIn exception: ${t.message}")
            Result.failure(t)
        }
    }
}