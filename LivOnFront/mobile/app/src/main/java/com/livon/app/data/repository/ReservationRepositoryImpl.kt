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

    // Local types to represent cached reservations
    data class LocalReservation(
        val id: Int,
        val type: ReservationType,
        val coachId: String,
        val startAt: String,
        val endAt: String
    )

    enum class ReservationType { PERSONAL, GROUP }

    override suspend fun reserveCoach(
        coachId: String,
        startAt: LocalDateTime,
        endAt: LocalDateTime,
        preQnA: String?
    ): Result<Int> {
        return try {
            val req = ReserveCoachRequest(
                coachId = coachId,
                startAt = startAt.format(fmt),
                endAt = endAt.format(fmt),
                preQnA = preQnA
            )
            val res = api.reserveCoach(req)
            if (res.isSuccess && res.result != null) {
                // store into local cache so UI can read it even if different VM instance
                localReservations.add(
                    LocalReservation(
                        id = res.result,
                        type = ReservationType.PERSONAL,
                        coachId = coachId,
                        startAt = startAt.format(fmt),
                        endAt = endAt.format(fmt)
                    )
                )
                Result.success(res.result)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun reserveClass(classId: String): Result<Int> {
        return try {
            val res = api.reserveClass(classId)
            if (res.isSuccess && res.result != null) {
                localReservations.add(
                    LocalReservation(
                        id = res.result,
                        type = ReservationType.GROUP,
                        coachId = "",
                        startAt = LocalDateTime.now().format(fmt),
                        endAt = LocalDateTime.now().plusHours(1).format(fmt)
                    )
                )
                Result.success(res.result)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    companion object {
        // Simple in-memory cache to persist created reservations during app session.
        // This is intentionally minimal and for UX until the backend provides an "upcoming" endpoint.
        // Each entry stores server-created id (if available), type, coachId and ISO datetimes.
        val localReservations: MutableList<LocalReservation> = mutableListOf()
    }
}
