package com.livon.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path

// Backend coach list response (matches provided spec)
data class CoachItemResponse(
    val userId: String,
    val nickname: String,
    val job: String?,
    val introduce: String?,
    val profileImage: String?,
    val certificates: List<String>? = null
)

data class CoachListResult(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<CoachItemResponse>
)

interface CoachApiService {
    @GET("/api/v1/coaches")
    suspend fun findCoaches(): ApiResponse<CoachListResult>

    @GET("/api/v1/coaches/{coachId}")
    suspend fun findCoachDetail(@Path("coachId") coachId: String): ApiResponse<CoachItemResponse>
}
