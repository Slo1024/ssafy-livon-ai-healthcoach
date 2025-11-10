package com.livon.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApiService {
    /**
     * 개인 상담 예약
     * POST /api/v1/individual-consultations
     * Body: ReserveCoachRequest
     */
    @POST("api/v1/individual-consultations")
    suspend fun reserveCoach(@Body req: ReserveCoachRequest): ApiResponse<Int>

    /**
     * 그룹(클래스) 예약
     * POST /api/v1/group-consultations/{classId}
     */
    @POST("api/v1/group-consultations/{classId}")
    suspend fun reserveClass(@Path("classId") classId: String): ApiResponse<Int>
}
