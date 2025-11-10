package com.livon.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// Request for reserving individual consultation (1:1)
data class ReserveCoachRequest(
    val coachId: String,
    val startAt: String, // ISO local date-time like "2025-11-09T14:00:00"
    val endAt: String,
    val preQnA: String? = null
)

// Response for creating resources often returns an id in result
// We'll reuse ApiResponse<T> defined in AuthApiService.kt

interface ReservationApi {
    @POST("individual-consultations")
    suspend fun reserveCoach(@Body req: ReserveCoachRequest): ApiResponse<Int>

    @POST("group-consultations/{classId}")
    suspend fun reserveClass(@Path("classId") classId: String): ApiResponse<Int>
}
