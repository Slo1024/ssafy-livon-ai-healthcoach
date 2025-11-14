package com.livon.app.domain.repository

import java.time.LocalDateTime

interface ReservationRepository {
    suspend fun reserveCoach(coachId: String, startAt: LocalDateTime, endAt: LocalDateTime, preQna: String?): Result<Int>
    suspend fun reserveClass(classId: String, preQna: String?): Result<Int>
    suspend fun getMyReservations(status: String, type: String? = null): Result<com.livon.app.data.remote.api.ReservationListResponse>
    suspend fun cancelIndividual(consultationId: Int): Result<Unit>
    suspend fun cancelGroupParticipation(consultationId: Int): Result<Unit>
}
