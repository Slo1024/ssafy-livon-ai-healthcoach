package com.livon.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApiService {
    /**
     * 개인 상담 예약
     * POST /individual-consultations  (baseUrl에 /api/v1 포함된 경우를 지원)
     * Body: ReserveCoachRequest
     */
    @POST("individual-consultations")
    suspend fun reserveCoach(@Body req: ReserveCoachRequest): ApiResponse<Int>

    /**
     * 그룹(클래스) 예약
     * POST /group-consultations/{classId}  (baseUrl에 /api/v1 포함된 경우를 지원)
     */
    @POST("group-consultations/{classId}")
    suspend fun reserveClass(@Path("classId") classId: String): ApiResponse<Int>
}
