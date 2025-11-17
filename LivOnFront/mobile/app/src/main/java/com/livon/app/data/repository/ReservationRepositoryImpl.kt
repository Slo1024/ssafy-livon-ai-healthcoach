package com.livon.app.data.repository

import com.livon.app.core.network.RetrofitProvider
import com.livon.app.data.remote.api.InstantConsultationRequest
import com.livon.app.data.remote.api.InstantConsultationResponse
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
                
                // [수정] 예약 생성 직후 서버 동기화 지연을 고려하여 약간의 지연 후 서버에서 최신 데이터 가져오기
                // 서버가 새 예약을 반영하는데 시간이 걸릴 수 있으므로 짧은 지연 후 재조회
                try {
                    kotlinx.coroutines.delay(800) // 서버 동기화 대기
                    refreshLocalReservationsFromServer()
                } catch (_: Throwable) { /* ignore refresh failures */ }
                
                Result.success(createdId)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun reserveClass(classId: String, preQna: String?): Result<Int> {
        // [수정] 서버가 Source of Truth이므로 항상 서버 API를 호출합니다.
        // 백엔드에서 중복 체크를 처리하므로 클라이언트는 서버 응답 코드에만 의존합니다.
        
        Log.d("ReservationRepo", "reserveClass: calling API for classId=$classId")
        
        return try {
            if (preQna != null) {
                Log.d("ReservationRepo", "reserveClass: preQna provided but ignored by API: $preQna")
            }
            val res = api.reserveClass(classId)
            Log.d("ReservationRepo", "reserveClass API response: isSuccess=${res.isSuccess}, result=${res.result}, message=${res.message}")
            
            if (res.isSuccess && res.result != null) {
                val createdId = res.result
                Log.d("ReservationRepo", "reserveClass: success, createdId=$createdId")
                
                // [수정] 서버에서 예약이 성공적으로 생성되었으므로 로컬 캐시에 추가 (Optimistic UI)
                try {
                    synchronized(cacheLock) {
                        val owner = com.livon.app.data.session.SessionManager.getTokenSync()
                        // 기존 항목이 있다면 제거 (같은 ID, 같은 소유자)
                        localReservations.removeAll { it.id == createdId && (it.ownerToken ?: "") == (owner ?: "") }
                        // 새 예약 추가
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
                    try { 
                        localReservationsFlow.emit(localReservations.toList())
                        Log.d("ReservationRepo", "emit localReservations after reserveClass: count=${localReservations.size}, createdId=$createdId")
                    } catch (_: Throwable) {}
                } catch (_: Throwable) { }

                // 캐시 무효화 - 서버에서 최신 데이터를 가져오도록
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                
                // [수정] 예약 생성 직후 서버 동기화를 위해 짧은 지연 후 서버에서 최신 데이터 가져오기
                try {
                    kotlinx.coroutines.delay(500) // 서버 동기화 대기 (짧은 지연)
                    refreshLocalReservationsFromServer()
                } catch (_: Throwable) { /* ignore */ }
                
                Result.success(createdId)
            } else {
                // 서버 응답이 실패인 경우
                val errorMsg = res.message ?: "Unknown error"
                Log.e("ReservationRepo", "reserveClass: API returned failure: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (t: Throwable) {
            // [수정] HTTP 에러는 서버 응답 코드에 따라 처리 (백엔드가 중복 체크 처리)
            if (t is retrofit2.HttpException) {
                val errorBody = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                val httpCode = t.code()
                Log.e("ReservationRepo", "reserveClass failed: http $httpCode ${t.message()} body=$errorBody", t)
                
                // HTTP 상태 코드에 따라 처리 (서버가 중복 체크를 처리하므로 클라이언트는 코드만 확인)
                when (httpCode) {
                    409 -> {
                        // Conflict - 서버가 이미 예약되었다고 응답
                        Log.w("ReservationRepo", "reserveClass: 409 Conflict - already reserved")
                        Result.failure(Exception("이미 예약된 클래스입니다."))
                    }
                    400 -> {
                        // Bad Request - 잘못된 요청
                        Log.w("ReservationRepo", "reserveClass: 400 Bad Request")
                        Result.failure(Exception(errorBody ?: "잘못된 요청입니다."))
                    }
                    500 -> {
                        // Server Error
                        Log.w("ReservationRepo", "reserveClass: 500 Server Error")
                        Result.failure(Exception(errorBody ?: "서버 오류가 발생했습니다."))
                    }
                    else -> {
                        Result.failure(Exception(errorBody ?: t.message()))
                    }
                }
            } else {
                Log.e("ReservationRepo", "reserveClass failed", t)
                Result.failure(t)
            }
        }
    }

    override suspend fun createInstantConsultation(
        durationMinutes: Int,
        capacity: Int,
        preQna: String?
    ): Result<InstantConsultationResponse> {
        return try {
            val req = InstantConsultationRequest(
                durationMinutes = durationMinutes,
                capacity = capacity,
                preQnA = preQna
            )
            val res = api.createInstantConsultation(req)
            if (res.isSuccess && res.result != null) {
                Result.success(res.result)
            } else {
                Result.failure(Exception(res.message ?: "Unknown"))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // New cancel implementations
    override suspend fun cancelIndividual(consultationId: Int): Result<Unit> {
        return try {
            Log.d("ReservationRepo", "cancelIndividual: calling API for id=$consultationId")
            val res = try { api.cancelIndividual(consultationId) } catch (t: Throwable) {
                // If it's an HttpException, include response body in exception
                throw t
            }
            Log.d("ReservationRepo", "cancelIndividual: api returned isSuccess=${res.isSuccess}, message=${res.message}")
            if (res.isSuccess) {
                // remove from local cache if present for this owner only
                val ownerRem = com.livon.app.data.session.SessionManager.getTokenSync()
                localReservations.removeAll { it.id == consultationId && (it.ownerToken ?: "") == (ownerRem ?: "") }
                // Try to refresh authoritative reservations from server to keep in-sync
                try { refreshLocalReservationsFromServer() } catch (_: Throwable) { }
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                // invalidate cached my-reservations
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.message ?: "Unknown"))
            }
        } catch (t: Throwable) {
            // If this was an HTTP error, try to extract body for diagnostics
            if (t is retrofit2.HttpException) {
                val body = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                Log.e("ReservationRepo", "cancelIndividual http error ${t.code()} body=$body", t)
                return Result.failure(Exception(body ?: t.message()))
            }
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
            val res = try { api.cancelGroupParticipation(consultationId) } catch (t: Throwable) { throw t }
            Log.d("ReservationRepo", "cancelGroupParticipation: api returned isSuccess=${res.isSuccess}, message=${res.message}")
            if (res.isSuccess) {
                // remove from local cache if present for this owner only
                val ownerRem = com.livon.app.data.session.SessionManager.getTokenSync()
                localReservations.removeAll { it.id == consultationId && (it.ownerToken ?: "") == (ownerRem ?: "") }
                // Try to refresh authoritative reservations from server
                try { refreshLocalReservationsFromServer() } catch (_: Throwable) { }
                // Emit updated reservations list
                localReservationsFlow.emit(localReservations.toList())
                // invalidate cached my-reservations
                synchronized(cacheLock) { cachedMyReservations = null; cachedAt = 0 }
                Result.success(Unit)
            } else Result.failure(Exception(res.message ?: "Unknown"))
        } catch (t: Throwable) {
            if (t is retrofit2.HttpException) {
                val body = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                Log.e("ReservationRepo", "cancelGroupParticipation http error ${t.code()} body=$body", t)
                return Result.failure(Exception(body ?: t.message()))
            }
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
                // [수정] 취소된 예약 제외 (CANCELLED 상태 필터링)
                val activeItems = items.filter { (it.status ?: "OPEN") != "CANCELLED" }
                
                // rebuild localReservations from server items
                val snapshot = mutableListOf<LocalReservation>()
                synchronized(cacheLock) {
                    // preserve any existing local placeholders not present on server
                    val existingLocal = localReservations.toList()
                    val serverIds = mutableSetOf<Int>()
                    
                    // [수정] 취소되지 않은 항목만 로컬 캐시에 추가
                    for (it in activeItems) {
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
                            // 기존 항목과 동일한 ID가 있으면 업데이트, 없으면 추가
                            localReservations.removeAll { it.id == id && (it.ownerToken ?: "") == (owner ?: "") }
                            localReservations.add(lr)
                            snapshot.add(lr)
                        } catch (_: Throwable) { /* ignore individual item parse failures */ }
                    }
                    // [수정] 서버에 없는 기존 로컬 항목 처리
                    // 서버에 없는 항목은 취소되었거나 최근 생성되었을 수 있음
                    // 최근 생성된 항목은 서버 동기화 지연으로 아직 반영되지 않았을 수 있으므로 보존
                    val now = LocalDateTime.now()
                    val owner = com.livon.app.data.session.SessionManager.getTokenSync()
                    for (ex in existingLocal) {
                        if (!serverIds.contains(ex.id) && (ex.ownerToken ?: "") == (owner ?: "")) {
                            try {
                                // 서버에 없는 로컬 항목은 미래 예약이면 보존 (서버 동기화 지연 가능)
                                // 과거 예약은 취소된 것으로 간주하여 제외
                                val start = LocalDateTime.parse(ex.startAt)
                                val isFuture = start.isAfter(now)
                                if (isFuture) {
                                    // 미래 예약은 보존 (서버 동기화 지연 가능)
                                    if (!snapshot.any { it.id == ex.id }) {
                                        localReservations.removeAll { it.id == ex.id && (it.ownerToken ?: "") == (owner ?: "") }
                                        localReservations.add(ex)
                                        snapshot.add(ex)
                                        android.util.Log.d("ReservationRepo", "refreshLocalReservationsFromServer: preserving future local item id=${ex.id} (not on server yet, likely recent creation)")
                                    }
                                } else {
                                    // 서버에 없고 과거 예약은 제외 (취소된 것으로 간주)
                                    localReservations.removeAll { it.id == ex.id && (it.ownerToken ?: "") == (owner ?: "") }
                                    android.util.Log.d("ReservationRepo", "refreshLocalReservationsFromServer: excluding past local item id=${ex.id} (not on server, likely cancelled)")
                                }
                            } catch (_: Throwable) {
                                // 파싱 실패 시 보존 (안전을 위해)
                                if (!snapshot.any { it.id == ex.id }) {
                                    localReservations.removeAll { it.id == ex.id && (it.ownerToken ?: "") == (owner ?: "") }
                                    localReservations.add(ex)
                                    snapshot.add(ex)
                                }
                            }
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
