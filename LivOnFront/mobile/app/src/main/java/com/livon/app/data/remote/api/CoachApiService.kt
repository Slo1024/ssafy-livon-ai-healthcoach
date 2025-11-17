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
    
    /**
     * 코치가 스스로 막아놓은 시간대 조회
     * GET /coaches/block-times?date=YYYY-MM-DD
     * 해당 날짜에 코치가 차단한 시간대를 반환하여 UI에서 비활성화 처리에 사용
     */
    @GET("coaches/block-times")
    suspend fun getBlockedTimes(
        @retrofit2.http.Query("date") date: String
    ): ApiResponse<BlockedTimesResponse>
}

/**
 * 코치의 예약 가능 시간 응답
 * [주의] API는 actual로 availableTimes(예약 가능한 시간, "HH:MM-HH:MM" 형식)를 반환합니다.
 * bookedTimes는 사용하지 않습니다. availableTimes를 기반으로 예약 불가능한 시간을 계산합니다.
 */
data class CoachAvailableTimesResponse(
    val coachId: String,
    val availableTimes: List<String>? = null, // 예약 가능한 시간 리스트 (예: ["10:00-11:00", "14:00-15:00"])
    val bookedTimes: List<String>? = null, // [미사용] 이미 예약된 시간 토큰 리스트
    val date: String? = null // 조회한 날짜 (YYYY-MM-DD 형식)
)

/**
 * 코치가 막아놓은 시간대 응답
 * blockedTimes: 차단된 시간대 리스트 (예: ["09:00-10:00", "11:00-12:00", "15:00-16:00"])
 */
data class BlockedTimesResponse(
    val date: String, // 조회한 날짜 (YYYY-MM-DD 형식)
    val blockedTimes: List<String>? = null // 차단된 시간대 리스트 (HH:MM-HH:MM 형식)
)
