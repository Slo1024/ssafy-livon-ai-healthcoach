package com.livon.app.data.repository

import com.livon.app.core.network.RetrofitProvider
import com.livon.app.data.remote.api.ReservationApiService
import com.livon.app.data.remote.api.ReserveCoachRequest
import com.livon.app.domain.repository.ReservationRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

class ReservationRepositoryImpl : ReservationRepository {
    private val api = RetrofitProvider.createService(ReservationApiService::class.java)
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    // Local types to represent cached reservations
    data class LocalReservation(
        val id: Int,
        val type: ReservationType,
        val coachId: String,
        val startAt: String,
        val endAt: String,
        val classTitle: String? = null,
        val coachName: String? = null,
        val preQna: String? = null
    )

    // ReservationType moved to a top-level declaration (ReservationModels.kt)
    // to avoid cross-file nested-type resolution issues when referenced from viewmodels.

    override suspend fun reserveCoach(
        coachId: String,
        startAt: LocalDateTime,
        endAt: LocalDateTime,
        preQna: String?,
        coachName: String?
    ): Result<Int> {
        return try {
            val req = ReserveCoachRequest(
                coachId = coachId,
                startAt = startAt.format(fmt),
                endAt = endAt.format(fmt),
                preQnA = preQna
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
                        endAt = endAt.format(fmt),
                        coachName = coachName,
                        preQna = preQna
                    )
                )
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                Result.success(res.result)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // Adjusted to match interface: include preQna param (backend ignores it for class reservation)
    override suspend fun reserveClass(classId: String, preQna: String?): Result<Int> {
        return try {
            // backend does not expect a body for class reservation; log preQna for debugging
            if (preQna != null) {
                Log.d("ReservationRepo", "reserveClass: preQna provided but ignored by API: $preQna")
            }
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
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                Result.success(res.result)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            // Improved error logging: if this is an HTTP exception, try to extract server error body
            try {
                if (t is retrofit2.HttpException) {
                    val errorBody = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                    Log.e("ReservationRepo", "reserveClass failed: http ${t.code()} ${t.message()} body=$errorBody", t)
                    // if server indicates duplicate participant (DB unique constraint), mark as ALREADY_RESERVED
                    if (errorBody != null && (errorBody.contains("uk_participant_user_consultation") || errorBody.contains("Duplicate entry"))) {
                        Log.w("ReservationRepo", "reserveClass: duplicate participant detected -> returning ALREADY_RESERVED body=$errorBody")
                        // Attempt to enrich local cache entry with class detail (startAt/title/coach)
                        try {
                            val numericId = classId.toIntOrNull() ?: -(classId.hashCode())
                            val exists = localReservations.any { it.id == numericId }
                            if (!exists) {
                                // try fetch class detail to get accurate start/end and names
                                try {
                                    val groupApi = com.livon.app.core.network.RetrofitProvider.createService(com.livon.app.data.remote.api.GroupConsultationApiService::class.java)
                                    val detailRes = groupApi.findClassDetail(classId)
                                    if (detailRes.isSuccess && detailRes.result != null) {
                                        val dto = detailRes.result
                                        val startIso = dto.startAt ?: java.time.LocalDateTime.now().format(fmt)
                                        val endIso = dto.endAt ?: java.time.LocalDateTime.now().plusHours(1).format(fmt)
                                        val title = dto.title
                                        val coachName = dto.coach?.nickname ?: dto.coach?.id ?: ""
                                        localReservations.add(
                                            LocalReservation(
                                                id = numericId,
                                                type = ReservationType.GROUP,
                                                coachId = dto.coach?.id ?: "",
                                                startAt = startIso,
                                                endAt = endIso,
                                                classTitle = title,
                                                coachName = coachName
                                            )
                                        )
                                        // Emit updated reservations list after adding fallback entry
                                        localReservationsFlow.emit(localReservations.toList())
                                    } else {
                                        // fallback minimal entry
                                        localReservations.add(
                                            LocalReservation(
                                                id = numericId,
                                                type = ReservationType.GROUP,
                                                coachId = "",
                                                startAt = java.time.LocalDateTime.now().format(fmt),
                                                endAt = java.time.LocalDateTime.now().plusHours(1).format(fmt),
                                                classTitle = null,
                                                coachName = null
                                            )
                                        )
                                        // Emit updated reservations list after adding fallback entry
                                        localReservationsFlow.emit(localReservations.toList())
                                    }
                                } catch (_: Throwable) {
                                    // network/detail fetch failed: still add minimal local reservation
                                    localReservations.add(
                                        LocalReservation(
                                            id = numericId,
                                            type = ReservationType.GROUP,
                                            coachId = "",
                                            startAt = java.time.LocalDateTime.now().format(fmt),
                                            endAt = java.time.LocalDateTime.now().plusHours(1).format(fmt),
                                            classTitle = null,
                                            coachName = null
                                        )
                                    )
                                    // Emit updated reservations list after adding fallback entry
                                    localReservationsFlow.emit(localReservations.toList())
                                }
                            }
                        } catch (_: Throwable) { }
                        // Return a failure but with a sentinel message so ViewModel can act idempotently
                        return Result.failure(Exception("ALREADY_RESERVED:${errorBody}"))
                    }
                    return Result.failure(Exception(errorBody ?: t.message))
                }
            } catch (t2: Throwable) {
                Log.e("ReservationRepo", "Failed to extract http error body", t2)
            }
            Log.e("ReservationRepo", "reserveClass failed", t)
            Result.failure(t)
        }
    }

    // New cancel implementations
    override suspend fun cancelIndividual(consultationId: Int): Result<Unit> {
        return try {
            Log.d("ReservationRepo", "cancelIndividual: calling API for id=$consultationId")
            val res = api.cancelIndividual(consultationId)
            Log.d("ReservationRepo", "cancelIndividual: api returned isSuccess=${res.isSuccess}, message=${res.message}")
            if (res.isSuccess) {
                // remove from local cache if present
                localReservations.removeAll { it.id == consultationId }
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.message ?: "Unknown"))
            }
        } catch (t: Throwable) {
            Log.e("ReservationRepo", "cancelIndividual failed, attempting recovery check", t)
            // Recovery: server might have processed the deletion but response parsing failed
            return try {
                val check = api.getMyReservations(status = "upcoming")
                if (check.isSuccess && check.result != null) {
                    val exists = check.result.items.any { it.consultationId == consultationId }
                    if (!exists) {
                        // Item no longer present -> treat as success
                        localReservations.removeAll { it.id == consultationId }
                        Log.d("ReservationRepo", "cancelIndividual: recovery -> item not found on server; treating as success id=$consultationId")
                        // Emit updated reservations list
                        localReservationsFlow.emit(localReservations.toList())
                        Result.success(Unit)
                    } else {
                        // If present but marked CANCELLED, treat as success
                        val remote = check.result.items.first { it.consultationId == consultationId }
                        if (remote.status == "CANCELLED") {
                            localReservations.removeAll { it.id == consultationId }
                            Log.d("ReservationRepo", "cancelIndividual: recovery -> item status=CANCELLED; treating as success id=$consultationId")
                            // Emit updated reservations list
                            localReservationsFlow.emit(localReservations.toList())
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception(t))
                        }
                    }
                } else {
                    Result.failure(Exception(t))
                }
            } catch (t2: Throwable) {
                Log.e("ReservationRepo", "cancelIndividual: recovery check failed", t2)
                Result.failure(Exception(t))
            }
        }
    }

    override suspend fun cancelGroupParticipation(consultationId: Int): Result<Unit> {
        return try {
            Log.d("ReservationRepo", "cancelGroupParticipation: calling API for id=$consultationId")
            val res = api.cancelGroupParticipation(consultationId)
            Log.d("ReservationRepo", "cancelGroupParticipation: api returned isSuccess=${res.isSuccess}, message=${res.message}")
            if (res.isSuccess) {
                // remove from local cache if present
                localReservations.removeAll { it.id == consultationId }
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                Result.success(Unit)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Log.e("ReservationRepo", "cancelGroupParticipation failed, attempting recovery check", t)
            // Recovery: attempt to verify via my-reservations
            return try {
                val check = api.getMyReservations(status = "upcoming")
                if (check.isSuccess && check.result != null) {
                    val exists = check.result.items.any { it.consultationId == consultationId }
                    if (!exists) {
                        localReservations.removeAll { it.id == consultationId }
                        Log.d("ReservationRepo", "cancelGroupParticipation: recovery -> item not found on server; treating as success id=$consultationId")
                        // Emit updated reservations list
                        localReservationsFlow.emit(localReservations.toList())
                        Result.success(Unit)
                    } else {
                        val remote = check.result.items.first { it.consultationId == consultationId }
                        if (remote.status == "CANCELLED") {
                            localReservations.removeAll { it.id == consultationId }
                            Log.d("ReservationRepo", "cancelGroupParticipation: recovery -> item status=CANCELLED; treating as success id=$consultationId")
                            // Emit updated reservations list
                            localReservationsFlow.emit(localReservations.toList())
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception(t))
                        }
                    }
                } else Result.failure(Exception(t))
            } catch (t2: Throwable) {
                Log.e("ReservationRepo", "cancelGroupParticipation: recovery check failed", t2)
                Result.failure(Exception(t))
            }
        }
    }

    // New: fetch reservations from server
    override suspend fun getMyReservations(status: String, type: String?): Result<com.livon.app.data.remote.api.ReservationListResponse> {
        return try {
            val res = api.getMyReservations(status = status, type = type)
            if (res.isSuccess && res.result != null) {
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
        // Flow that emits a snapshot list whenever localReservations changes.
        val localReservationsFlow: MutableStateFlow<List<LocalReservation>> = MutableStateFlow(localReservations.toList())
    }
}
