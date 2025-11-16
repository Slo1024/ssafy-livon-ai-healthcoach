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

data class InstantConsultationRequest(
    val durationMinutes: Int = 60,
    val capacity: Int = 1,
    val preQnA: String? = null
)

data class InstantConsultationResponse(
    val consultationId: Long,
    val sessionId: String,
    val startAt: String,
    val endAt: String
)

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

interface ReservationApiService {
    /**
     * 개인 상담 예약
     * POST /individual-consultations  (baseUrl에 /api/v1 포함된 경우를 지원)
     * Body: ReserveCoachRequest
     */
    @POST("individual-consultations")
    suspend fun reserveCoach(@Body req: ReserveCoachRequest): ApiResponse<Int>

    @POST("individual-consultations/instant")
    suspend fun createInstantConsultation(
        @Body req: InstantConsultationRequest
    ): ApiResponse<InstantConsultationResponse>

    /**
     * 그룹(클래스) 예약
     * POST /group-consultations/{classId}  (baseUrl에 /api/v1 포함된 경우를 지원)
     */
    @POST("group-consultations/{classId}")
    suspend fun reserveClass(@Path("classId") classId: String): ApiResponse<Int>

    /**
     * 나의 예약 조회
     * GET /consultations/my-reservations  (baseUrl에 /api/v1 포함된 경우를 지원)
     * Query: status, type
     */
    @GET("consultations/my-reservations")
    suspend fun getMyReservations(
        @Query("status") status: String,
        @Query("type") type: String? = null
    ): ApiResponse<ReservationListResponse>

    /**
     * 개인 상담 예약 취소
     * DELETE /individual-consultations/{consultationId}  (baseUrl에 /api/v1 포함된 경우를 지원)
     */
    @retrofit2.http.DELETE("individual-consultations/{consultationId}")
    suspend fun cancelIndividual(@Path("consultationId") consultationId: Int): ApiResponse<Any?>

    /**
     * 그룹 상담 예약 취소
     * DELETE /group-consultations/{consultationId}/participants  (baseUrl에 /api/v1 포함된 경우를 지원)
     */
    @retrofit2.http.DELETE("group-consultations/{consultationId}/participants")
    suspend fun cancelGroupParticipation(@Path("consultationId") consultationId: Int): ApiResponse<Any?>
}
