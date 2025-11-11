package com.livon.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Request for reserving individual consultation (1:1)
data class ReserveCoachRequest(
    val coachId: String,
    val startAt: String, // ISO local date-time like "2025-11-09T14:00:00"
    val endAt: String,
    val preQnA: String? = null
)

// Generic API wrapper used by backend
// ApiResponse<T> is defined elsewhere (AuthApiService.kt)

// Response DTOs for my-reservations
data class ReservationListResponse(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<ReservationItemDto>
)

data class ReservationItemDto(
    val consultationId: Int,
    val type: String?,
    val status: String?,
    val startAt: String?,
    val endAt: String?,
    val sessionId: String?,
    val coach: CoachDto?,
    val preQna: String?,
    val aiSummary: String?,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val capacity: Int?,
    val currentParticipants: Int?
)

data class CoachDto(
    val userId: String?,
    val nickname: String?,
    val job: String?,
    val introduce: String?,
    val profileImage: String?,
    val certificates: List<String>?,
    val organizations: String?
)

interface ReservationApi {
    @POST("individual-consultations")
    suspend fun reserveCoach(@Body req: ReserveCoachRequest): ApiResponse<Int>

    @POST("group-consultations/{classId}")
    suspend fun reserveClass(@Path("classId") classId: String): ApiResponse<Int>

    /**
     * 내 예약 조회
     * GET /consultations/my-reservations?status=upcoming&type=ONE
     */
    @GET("consultations/my-reservations")
    suspend fun getMyReservations(
        @Query("status") status: String,
        @Query("type") type: String? = null
    ): ApiResponse<ReservationListResponse>
}
