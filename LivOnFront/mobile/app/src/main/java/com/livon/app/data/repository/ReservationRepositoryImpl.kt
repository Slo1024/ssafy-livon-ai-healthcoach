package com.livon.app.data.repository

import com.livon.app.core.network.RetrofitProvider
import com.livon.app.data.remote.api.ReservationApi
import com.livon.app.data.remote.api.ReserveCoachRequest
import com.livon.app.domain.repository.ReservationRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReservationRepositoryImpl : ReservationRepository {
    private val api = RetrofitProvider.createService(ReservationApi::class.java)
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    override suspend fun reserveCoach(
        coachId: String,
        startAt: LocalDateTime,
        endAt: LocalDateTime,
        preQnA: String?
    ): Result<Int> {
        return try {
            val req = com.livon.app.data.remote.api.ReserveCoachRequest(
                coachId = coachId,
                startAt = startAt.format(fmt),
                endAt = endAt.format(fmt),
                preQnA = preQnA
            )
            val res = api.reserveCoach(req)
            if (res.isSuccess && res.result != null) Result.success(res.result) else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun reserveClass(classId: String): Result<Int> {
        return try {
            val res = api.reserveClass(classId)
            if (res.isSuccess && res.result != null) Result.success(res.result) else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}

