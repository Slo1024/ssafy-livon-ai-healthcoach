package com.livon.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import java.time.LocalDate

// DTO matching backend; adjust fields if backend differs
data class ReservationDto(
    val id: String,
    val date: String, // ISO date e.g. 2025-11-06
    val className: String,
    val coachName: String,
    val coachRole: String,
    val coachIntro: String,
    val timeText: String,
    val classIntro: String,
    val imageResId: Int? = null,
    val isLive: Boolean = false,
    val sessionTypeLabel: String? = null,
    val hasAiReport: Boolean = false
)

interface ReservationApiService {
    @GET("/reservations/upcoming")
    suspend fun getUpcoming(): List<ReservationDto>

    @POST("/reservations")
    suspend fun createReservation(@Body req: Any): ReservationDto
}

