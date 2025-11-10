package com.livon.app.domain.repository

import java.time.LocalDateTime

interface ReservationRepository {
    suspend fun reserveCoach(coachId: String, startAt: LocalDateTime, endAt: LocalDateTime, preQnA: String?): Result<Int>
    suspend fun reserveClass(classId: String): Result<Int>
}

