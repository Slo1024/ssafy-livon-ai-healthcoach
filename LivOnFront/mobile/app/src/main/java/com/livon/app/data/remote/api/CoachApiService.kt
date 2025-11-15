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
    @GET("coaches")
    suspend fun findCoaches(): ApiResponse<CoachListResult>

    @GET("coaches/{coachId}")
    suspend fun findCoachDetail(@Path("coachId") coachId: String): ApiResponse<CoachItemResponse>
    
    /**
     * 코치의 예약 가능 시간 조회
     * GET /coaches/{coachId}/available-times?date=YYYY-MM-DD
     * 해당 날짜에 이미 예약된 시간 슬롯을 반환하여 UI에서 비활성화 처리에 사용
     */
    @GET("coaches/{coachId}/available-times")
    suspend fun getCoachAvailableTimes(
        @Path("coachId") coachId: String,
        @retrofit2.http.Query("date") date: String? = null
    ): ApiResponse<CoachAvailableTimesResponse>
}

/**
 * 코치의 예약 가능 시간 응답
 * bookedTimes: 해당 날짜에 이미 예약된 시간 슬롯 리스트 (예: ["AM_9:00", "PM_2:00"])
 */
data class CoachAvailableTimesResponse(
    val coachId: String,
    val bookedTimes: List<String>? = null, // 이미 예약된 시간 토큰 리스트
    val date: String? = null // 조회한 날짜 (YYYY-MM-DD 형식)
)
