package com.livon.app.data.repository

import com.livon.app.core.network.RetrofitProvider
import com.livon.app.data.remote.api.ReservationApiService
import com.livon.app.data.remote.api.ReserveCoachRequest
import com.livon.app.domain.repository.ReservationRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Context
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val preQna: String? = null,
        val ownerToken: String? = null // token identifying owning user session
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
                val createdId = res.result
                // Add a local placeholder entry so UI can show the newly created reservation immediately
                try {
                    synchronized(cacheLock) {
                        // remove existing placeholder with same id for this owner if present
                        val owner = com.livon.app.data.session.SessionManager.getTokenSync()
                        localReservations.removeAll { it.id == createdId && (it.ownerToken ?: "") == (owner ?: "") }
                        localReservations.add(LocalReservation(
                            id = createdId,
                            type = ReservationType.PERSONAL,
                            coachId = coachId,
                            startAt = startAt.format(fmt),
                            endAt = endAt.format(fmt),
                            classTitle = null,
                            coachName = coachName,
                            preQna = preQna,
                            ownerToken = owner
                        ))
                    }
                    try { localReservationsFlow.emit(localReservations.toList()); Log.d("ReservationRepo", "emit localReservations after reserveCoach: count=${localReservations.size}") } catch (_: Throwable) {}
                } catch (_: Throwable) { }

                // Invalidate cached my-reservations so subsequent reads refresh from server
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                // Attempt to refresh authoritative reservations from server and sync local cache (best-effort)
                try { refreshLocalReservationsFromServer() } catch (_: Throwable) { /* ignore refresh failures */ }
                Result.success(createdId)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun reserveClass(classId: String, preQna: String?): Result<Int> {
        // Cache-first: if local cache already contains this class id, avoid hitting server
        try {
            val numericIdCheck = classId.toIntOrNull() ?: -(classId.hashCode())
            if (localReservations.any { it.id == numericIdCheck }) {
                return Result.success(numericIdCheck)
            }
        } catch (_: Throwable) { /* ignore */ }

        return try {
            if (preQna != null) {
                Log.d("ReservationRepo", "reserveClass: preQna provided but ignored by API: $preQna")
            }
            val res = api.reserveClass(classId)
            if (res.isSuccess && res.result != null) {
                val createdId = res.result
                try {
                    synchronized(cacheLock) {
                        // remove existing placeholder with same id for this owner if present
                        val owner = com.livon.app.data.session.SessionManager.getTokenSync()
                        localReservations.removeAll { it.id == createdId && (it.ownerToken ?: "") == (owner ?: "") }
                        localReservations.add(LocalReservation(
                            id = createdId,
                            type = ReservationType.GROUP,
                            coachId = "",
                            startAt = LocalDateTime.now().format(fmt),
                            endAt = LocalDateTime.now().plusHours(1).format(fmt),
                            classTitle = null,
                            coachName = null,
                            preQna = preQna,
                            ownerToken = owner
                        ))
                    }
                    try { localReservationsFlow.emit(localReservations.toList()); Log.d("ReservationRepo", "emit localReservations after reserveClass: count=${localReservations.size}") } catch (_: Throwable) {}
                } catch (_: Throwable) { }

                // Invalidate cache and refresh authoritative reservations from server
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                try { refreshLocalReservationsFromServer() } catch (_: Throwable) { /* ignore */ }
                Result.success(createdId)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            try {
                if (t is retrofit2.HttpException) {
                    val errorBody = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                    Log.e("ReservationRepo", "reserveClass failed: http ${t.code()} ${t.message()} body=$errorBody", t)
                    if (errorBody != null && (errorBody.contains("uk_participant_user_consultation") || errorBody.contains("Duplicate entry"))) {
                        Log.w("ReservationRepo", "reserveClass: duplicate participant detected -> attempting server verification body=$errorBody")
                        // Try to confirm server-side participant via getMyReservations; only add local cache if server confirms
                        try {
                            val numericId = classId.toIntOrNull() ?: -(classId.hashCode())
                            // Invalidate cached my-reservations so we fetch fresh
                            synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                            val myRes = try { api.getMyReservations(status = "upcoming", type = null) } catch (t: Throwable) { null }
                            if (myRes != null && myRes.isSuccess && myRes.result != null) {
                                val found = myRes.result.items.firstOrNull { it.consultationId == numericId }
                                if (found != null) {
                                    // Sync entire server list into local cache rather than adding single placeholder
                                    try { refreshLocalReservationsFromServer() } catch (_: Throwable) { /* ignore */ }
                                    return Result.success(found.consultationId)
                                }
                            }
                        } catch (_: Throwable) { /* ignore verification errors */ }
                        // Could not confirm on server -> do not add unconfirmed placeholder, return ALREADY_RESERVED failure
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
                // remove from local cache if present for this owner only
                val ownerRem = com.livon.app.data.session.SessionManager.getTokenSync()
                localReservations.removeAll { it.id == consultationId && (it.ownerToken ?: "") == (ownerRem ?: "") }
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                // invalidate cached my-reservations
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.message ?: "Unknown"))
            }
        } catch (t: Throwable) {
            Log.e("ReservationRepo", "cancelIndividual failed, attempting recovery check", t)
            // Recovery: server might have processed the deletion but response parsing failed
            return try {
                // Recovery: check local cache (no network). If the item is not present in localReservations
                // then treat the cancellation as successful. This avoids calling `getMyReservations` network API.
                try {
                    val existsOnLocal = localReservations.any { it.id == consultationId }
                    if (!existsOnLocal) {
                        // Item no longer present locally -> treat as success
                        Log.d("ReservationRepo", "cancelIndividual: recovery -> item not found locally; treating as success id=$consultationId")
                        localReservations.removeAll { it.id == consultationId }
                        localReservationsFlow.emit(localReservations.toList())
                        Result.success(Unit)
                    } else {
                        // If present locally, we cannot be sure the server succeeded; keep failure
                        Result.failure(Exception(t))
                    }
                } catch (t2: Throwable) {
                    Log.e("ReservationRepo", "cancelIndividual: local recovery check failed", t2)
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
                // remove from local cache if present for this owner only
                val ownerRem = com.livon.app.data.session.SessionManager.getTokenSync()
                localReservations.removeAll { it.id == consultationId && (it.ownerToken ?: "") == (ownerRem ?: "") }
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                // invalidate cached my-reservations
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                Result.success(Unit)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Log.e("ReservationRepo", "cancelGroupParticipation failed, attempting recovery check", t)
            // Recovery: attempt to verify via my-reservations
            return try {
                // Recovery: check local cache (no network). If the item is not present in localReservations
                // then treat the cancellation as successful. This avoids calling `getMyReservations` network API.
                try {
                    val existsOnLocal = localReservations.any { it.id == consultationId }
                    if (!existsOnLocal) {
                        localReservations.removeAll { it.id == consultationId }
                        Log.d("ReservationRepo", "cancelGroupParticipation: recovery -> item not found locally; treating as success id=$consultationId")
                        // Emit updated reservations list
                        localReservationsFlow.emit(localReservations.toList())
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(t))
                    }
                } catch (t2: Throwable) {
                    Log.e("ReservationRepo", "cancelGroupParticipation: local recovery check failed", t2)
                    Result.failure(Exception(t))
                }
            } catch (t2: Throwable) {
                Log.e("ReservationRepo", "cancelGroupParticipation: recovery check failed", t2)
                Result.failure(Exception(t))
            }
        }
    }

    // New: fetch reservations from server
    override suspend fun getMyReservations(status: String, type: String?): Result<com.livon.app.data.remote.api.ReservationListResponse> {
        // Use network-backed GET with a short TTL cache to avoid frequent duplicate calls.
        try {
            val nowMillis = System.currentTimeMillis()
            val ttl = 30_000L
            synchronized(cacheLock) {
                val cached = cachedMyReservations
                val ts = cachedAt
                if (cached != null && ts + ttl >= nowMillis && status == cachedStatus && type == cachedType) {
                    return Result.success(cached)
                }
            }

            val apiRes = try { api.getMyReservations(status = status, type = type) } catch (t: Throwable) { return Result.failure(t) }
            if (apiRes.isSuccess && apiRes.result != null) {
                synchronized(cacheLock) {
                    cachedMyReservations = apiRes.result
                    cachedAt = System.currentTimeMillis()
                    cachedStatus = status
                    cachedType = type
                }
                return Result.success(apiRes.result)
            } else {
                return Result.failure(Exception(apiRes.message ?: "Unknown"))
            }
        } catch (t: Throwable) {
            return Result.failure(t)
        }
    }

    // Synchronize localReservations from server authoritative data and emit to localReservationsFlow
    private suspend fun refreshLocalReservationsFromServer() {
        try {
            val apiRes = api.getMyReservations(status = "upcoming", type = null)
            if (apiRes.isSuccess && apiRes.result != null) {
                val items = apiRes.result.items
                // rebuild localReservations from server items
                val snapshot = mutableListOf<LocalReservation>()
                synchronized(cacheLock) {
                    // preserve any existing local placeholders not present on server
                    val existingLocal = localReservations.toList()
                    localReservations.clear()
                    val serverIds = mutableSetOf<Int>()
                    for (it in items) {
                        try {
                            val id = it.consultationId
                            serverIds.add(id)
                            val type = if ((it.type ?: "GROUP") == "ONE") ReservationType.PERSONAL else ReservationType.GROUP
                            val startIso = it.startAt ?: LocalDateTime.now().format(fmt)
                            val endIso = it.endAt ?: LocalDateTime.now().plusHours(1).format(fmt)
                            val coachId = it.coach?.userId ?: ""
                            val coachName = it.coach?.nickname
                            val owner = com.livon.app.data.session.SessionManager.getTokenSync()
                            val lr = LocalReservation(id = id, type = type, coachId = coachId, startAt = startIso, endAt = endIso, classTitle = it.title, coachName = coachName, ownerToken = owner)
                            localReservations.add(lr)
                            snapshot.add(lr)
                        } catch (_: Throwable) { /* ignore individual item parse failures */ }
                    }
                    // re-add any existing local placeholders that server did not return (likely recent creates)
                    for (ex in existingLocal) {
                        if (!serverIds.contains(ex.id)) {
                            localReservations.add(ex)
                            snapshot.add(ex)
                        }
                    }
                }
                // Emit snapshot outside synchronized block
                try { localReservationsFlow.emit(snapshot.toList()) } catch (_: Throwable) { }
            }
        } catch (_: Throwable) { /* ignore refresh errors */ }
    }

    companion object {
        // Simple in-memory cache to persist created reservations during app session.
        // This is intentionally minimal and for UX until the backend provides an "upcoming" endpoint.
        // Each entry stores server-created id (if available), type, coachId and ISO datetimes.
        val localReservations: MutableList<LocalReservation> = mutableListOf()
        // Flow that emits a snapshot list whenever localReservations changes.
        val localReservationsFlow: MutableStateFlow<List<LocalReservation>> = MutableStateFlow(localReservations.toList())

        // --- Caching for getMyReservations ---
        // Cached result of the last getMyReservations call, null if not cached or cache expired.
        @Volatile
        var cachedMyReservations: com.livon.app.data.remote.api.ReservationListResponse? = null
        // Timestamp of the last cache update
        @Volatile
        var cachedAt: Long = 0
        // Cached status and type parameters for the last getMyReservations call
        @Volatile
        var cachedStatus: String? = null
        @Volatile
        var cachedType: String? = null
        // lock object for synchronizing cache access
        private val cacheLock = Any()
    }

    // Persist localReservations into SharedPreferences as JSON
    fun persistLocalReservations(context: Context) {
        try {
            val owner = com.livon.app.data.session.SessionManager.getTokenSync()
            val keySuffix = owner?.hashCode()?.toString() ?: "anon"
            val prefs = context.getSharedPreferences("reservation_prefs", Context.MODE_PRIVATE)
            val listType = Types.newParameterizedType(List::class.java, LocalReservation::class.java)
            val adapter = RetrofitProvider.moshi.adapter<List<LocalReservation>>(listType)
            // Persist only this owner's reservations
            val toPersist = localReservations.filter { (it.ownerToken ?: "") == (owner ?: "") }
            val json = adapter.toJson(toPersist)
            prefs.edit().putString("local_reservations_json_v1_$keySuffix", json).apply()
        } catch (t: Throwable) {
            android.util.Log.w("ReservationRepo", "persistLocalReservations failed", t)
        }
    }

    // Load persisted reservations (if any) for current owner into localReservations and emit
    suspend fun loadPersistedReservations(context: Context) {
        try {
            val owner = com.livon.app.data.session.SessionManager.getTokenSync()
            val keySuffix = owner?.hashCode()?.toString() ?: "anon"
            val prefs = context.getSharedPreferences("reservation_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("local_reservations_json_v1_$keySuffix", null)
            if (!json.isNullOrBlank()) {
                val listType = Types.newParameterizedType(List::class.java, LocalReservation::class.java)
                val adapter = RetrofitProvider.moshi.adapter<List<LocalReservation>>(listType)
                val parsed = try { adapter.fromJson(json) } catch (t: Throwable) { null }
                if (parsed != null) {
                    synchronized(cacheLock) {
                        // remove any existing entries for this owner and replace with parsed
                        localReservations.removeAll { (it.ownerToken ?: "") == (owner ?: "") }
                        localReservations.addAll(parsed)
                    }
                    try { localReservationsFlow.emit(localReservations.toList()) } catch (_: Throwable) {}
                }
            }
        } catch (t: Throwable) {
            android.util.Log.w("ReservationRepo", "loadPersistedReservations failed", t)
        }
    }

    // Convenience: refresh from server and persist
    suspend fun syncFromServerAndPersist(context: Context) {
        try {
            refreshLocalReservationsFromServer()
            persistLocalReservations(context)
        } catch (t: Throwable) {
            android.util.Log.w("ReservationRepo", "syncFromServerAndPersist failed", t)
        }
    }

    // Remove persisted reservations entry for a specific owner token
    fun clearPersistedReservationsForOwner(context: Context, ownerToken: String?) {
        try {
            val keySuffix = ownerToken?.hashCode()?.toString() ?: "anon"
            val prefs = context.getSharedPreferences("reservation_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("local_reservations_json_v1_$keySuffix").apply()
        } catch (t: Throwable) {
            android.util.Log.w("ReservationRepo", "clearPersistedReservationsForOwner failed", t)
        }
    }

    // Clear in-memory local reservations for the given owner token and emit updated flow
    suspend fun clearLocalReservationsForOwner(ownerToken: String?) {
        try {
            withContext(Dispatchers.IO) {
                synchronized(cacheLock) {
                    localReservations.removeAll { (it.ownerToken ?: "") == (ownerToken ?: "") }
                }
                try { localReservationsFlow.emit(localReservations.toList()) } catch (_: Throwable) {}
            }
        } catch (t: Throwable) {
            android.util.Log.w("ReservationRepo", "clearLocalReservationsForOwner failed", t)
        }
    }
}
