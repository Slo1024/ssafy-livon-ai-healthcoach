package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.ReservationRepository
import com.livon.app.feature.member.reservation.ui.ReservationUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.time.Duration
import java.time.LocalDateTime

// We'll use the reservation repository for create actions (reserveCoach / reserveClass).
class ReservationViewModel(
    private val repo: ReservationRepository
) : ViewModel() {

    init {
        // If repository implementation provides a localReservationsFlow, observe it so that
        // any instance that created or updated local reservations triggers a UI refresh
        // in this ViewModel (useful when reserve actions happen in a different VM instance).
        try {
            if (repo is com.livon.app.data.repository.ReservationRepositoryImpl) {
                viewModelScope.launch {
                    com.livon.app.data.repository.ReservationRepositoryImpl.localReservationsFlow.collect {
                        try {
                            // refresh upcoming list when local cache changes
                            loadReservationsByStatus("upcoming")
                        } catch (_: Throwable) { }
                    }
                }
            }
        } catch (_: Throwable) { }
    }

    data class ReservationsUiState(
        val items: List<ReservationUi> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    data class ReservationActionState(
        val isLoading: Boolean = false,
        val success: Boolean? = null,
        val errorMessage: String? = null,
        val createdReservationId: Int? = null
    )

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState

    private val _actionState = MutableStateFlow(ReservationActionState())
    val actionState: StateFlow<ReservationActionState> = _actionState

    /**
     * 다가오는 예약 목록을 서버에서 불러옵니다.
     */
    fun loadUpcoming() {
        // status만 "upcoming"으로 하여 공통 로직 호출
        loadReservationsByStatus("upcoming")
    }

    /**
     * 지난 예약 목록을 서버에서 불러옵니다.
     */
    fun loadPast() {
        // status만 "past"로 하여 공통 로직 호출
        loadReservationsByStatus("past")
    }

    /**
     * status (upcoming/past)에 따라 예약 목록을 불러오는 공통 로직
     */
    private fun loadReservationsByStatus(status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val serverRes = try {
                    repo.getMyReservations(status = status, type = null)
                } catch (t: Throwable) {
                    Log.w("ReservationVM", "getMyReservations(status=$status) call failed", t)
                    Result.failure(t)
                }

                val mappedFromServer = if (serverRes.isSuccess) {
                    val body = serverRes.getOrNull()
                    try {
                        body?.items?.mapNotNull { dto ->
                            try {
                                if ((dto.status ?: "OPEN") == "CANCELLED") return@mapNotNull null

                                val start = LocalDateTime.parse(dto.startAt ?: LocalDateTime.now().toString())
                                val end = LocalDateTime.parse(dto.endAt ?: start.plusHours(1).toString())

                                val now = LocalDateTime.now()
                                val minutesUntilStart = Duration.between(now, start).toMinutes()
                                // isLive는 upcoming 예약에만 의미가 있음
                                val isLive = status == "upcoming" && minutesUntilStart <= 10 && minutesUntilStart >= -60

                                ReservationUi(
                                    id = dto.consultationId.toString(),
                                    date = start.toLocalDate(),
                                    className = dto.title ?: (if ((dto.type ?: "ONE") == "ONE") "개인 상담" else "그룹 클래스"),
                                    coachName = dto.coach?.nickname ?: dto.coach?.userId ?: "",
                                    coachRole = dto.coach?.job ?: "",
                                    coachIntro = dto.coach?.introduce ?: "",
                                    coachWorkplace = dto.coach?.organizations ?: "",
                                    timeText = formatTimeText(start, end),
                                    classIntro = dto.description ?: "",
                                    imageResId = null,
                                    classImageUrl = dto.imageUrl,
                                    isLive = isLive,
                                    startAtIso = dto.startAt,
                                    sessionId = dto.sessionId,
                                    sessionTypeLabel = if ((dto.type ?: "ONE") == "ONE") "개인 상담" else "그룹 상담",
                                    hasAiReport = dto.aiSummary != null,
                                    aiSummary = dto.aiSummary,
                                    coachId = dto.coach?.userId,
                                    coachProfileImageUrl = dto.coach?.profileImage,
                                    qnas = dto.preQna?.split("\n")?.filter { it.isNotBlank() } ?: emptyList(),
                                    isPersonal = ((dto.type ?: "ONE") == "ONE")
                                )
                            } catch (t: Throwable) {
                                Log.w("ReservationVM", "Failed to map reservation item", t)
                                null
                            }
                        } ?: emptyList()
                    } catch (t: Throwable) {
                        Log.e("ReservationVM", "Failed to parse reservations from server", t)
                        emptyList()
                    }
                } else emptyList()

                // Merge server results with in-memory cache so locally-added reservations are shown as well
                // [수정] 서버에서 가져온 ID 목록을 기준으로 로컬 캐시 병합 (서버에 없는 = 취소된 예약은 제외)
                val finalList = mappedFromServer.toMutableList()
                // 서버에서 가져온 모든 ID 집합 (취소되지 않은 활성 예약만 포함)
                val serverIds = finalList.map { it.id }.toMutableSet()
                
                // 서버 응답의 원본 items에서도 모든 ID를 수집 (취소된 것 포함하여 필터링용)
                // [수정] 서버 응답의 모든 항목 ID를 수집하여 로컬 캐시 필터링에 사용
                val allServerItemIds = try {
                    val body = serverRes.getOrNull()
                    body?.items?.mapNotNull { dto -> 
                        try { 
                            // CANCELLED 상태 여부와 관계없이 모든 ID 수집
                            dto.consultationId.toString() 
                        } catch (_: Throwable) { null }
                    }?.toSet() ?: emptySet()
                } catch (_: Throwable) { emptySet() }
                
                try {
                    if (repo is com.livon.app.data.repository.ReservationRepositoryImpl) {
                        val ownerToken = com.livon.app.data.session.SessionManager.getTokenSync()
                        val local = com.livon.app.data.repository.ReservationRepositoryImpl.localReservations.filter { (it.ownerToken ?: "") == (ownerToken ?: "") }
                        
                        // [핵심 수정] 서버 응답에 있는 ID만 포함 (서버에 없는 = 취소된 예약은 제외)
                        // 서버 응답에 있지만 CANCELLED 상태인 경우는 이미 mappedFromServer에서 제외됨
                        // 따라서 allServerItemIds에 있으면서 서버 데이터에도 있는 항목만 포함
                        val localItems = local
                            .filter { lr -> 
                                // 서버 응답에 있는 ID인 경우에만 포함
                                // (서버에 없으면 취소된 것으로 간주하여 제외)
                                allServerItemIds.contains(lr.id.toString())
                            }
                            .mapNotNull { lr ->
                            try {
                                // 서버에서 이미 가져온 항목은 스킵 (서버 데이터가 우선)
                                if (serverIds.contains(lr.id.toString())) return@mapNotNull null
                                
                                val start = LocalDateTime.parse(lr.startAt)
                                val end = LocalDateTime.parse(lr.endAt)
                                ReservationUi(
                                    id = lr.id.toString(),
                                    date = start.toLocalDate(),
                                    className = lr.classTitle ?: if (lr.type == com.livon.app.data.repository.ReservationType.PERSONAL) "개인 상담" else "그룹 클래스",
                                    coachName = lr.coachName ?: "",
                                    coachRole = "",
                                    coachIntro = "",
                                    timeText = formatTimeText(start, end),
                                    classIntro = "",
                                    imageResId = null,
                                    classImageUrl = null,
                                    isLive = false,
                                    startAtIso = lr.startAt,
                                    sessionId = null,
                                    sessionTypeLabel = if (lr.type == com.livon.app.data.repository.ReservationType.PERSONAL) "개인 상담" else "그룹 상담",
                                    hasAiReport = false,
                                    aiSummary = null,
                                    qnas = lr.preQna?.split("\n")?.filter { it.isNotBlank() } ?: emptyList(),
                                    coachId = lr.coachId,
                                    coachProfileImageUrl = null,
                                    isPersonal = (lr.type == com.livon.app.data.repository.ReservationType.PERSONAL)
                                )
                            } catch (_: Throwable) { null }
                        }
                        // Append only those local items whose id is not already present in server results
                        localItems.forEach { li ->
                            if (!serverIds.contains(li.id)) {
                                finalList.add(li)
                                serverIds.add(li.id)
                            }
                        }
                    }
                } catch (_: Throwable) { /* ignore merging errors */ }

                try {
                    val ids = finalList.map { it.id }
                    Log.d("ReservationVM", "Mapped reservations count=${finalList.size}, ids=${ids.joinToString()}")
                } catch (_: Throwable) {}

                // Filter finalList according to requested status:
                // - upcoming: include items whose startAtIso is in the future or items marked isLive
                // - past: include items whose startAtIso is in the past
                val now = LocalDateTime.now()
                val filteredList = finalList.filter { item ->
                     try {
                         val startIso = item.startAtIso
                         val start = if (!startIso.isNullOrBlank()) LocalDateTime.parse(startIso) else null
                         if (status == "upcoming") {
                             // include if start is null (fallback), or start >= now, or item isLive
                             (start == null) || item.isLive || !start.isBefore(now)
                         } else {
                             // past: include only if start exists and is before now
                             (start != null && start.isBefore(now))
                         }
                     } catch (_: Throwable) {
                         // on parse error, conservatively include in upcoming to avoid hiding
                         status == "upcoming"
                     }
                 }

                // Sort the filtered list by proximity to now:
                // - upcoming: nearest future (or live) first (ascending start time)
                // - past: most recent past first (descending start time)
                val sorted = try {
                    val comparator = Comparator<ReservationUi> { a, b ->
                        fun parseStart(i: ReservationUi): LocalDateTime? {
                            return try { i.startAtIso?.let { LocalDateTime.parse(it) } } catch (_: Throwable) { null }
                        }
                        val sa = parseStart(a)
                        val sb = parseStart(b)
                        when (status) {
                            "upcoming" -> {
                                // null starts go to the end
                                if (sa == null && sb == null) 0
                                else if (sa == null) 1
                                else if (sb == null) -1
                                else sa.compareTo(sb)
                            }
                            else -> {
                                // past: nulls go to the start (but they were filtered out), compare descending
                                if (sa == null && sb == null) 0
                                else if (sa == null) 1
                                else if (sb == null) -1
                                else sb.compareTo(sa)
                            }
                        }
                    }
                    filteredList.sortedWith(comparator)
                } catch (t: Throwable) {
                    filteredList
                }

                _uiState.value = ReservationsUiState(items = sorted, isLoading = false)
             } catch (t: Throwable) {
                 _uiState.value = ReservationsUiState(items = emptyList(), isLoading = false, errorMessage = t.message)
             }
         }
     }


    private fun formatTimeText(start: LocalDateTime, end: LocalDateTime): String {
        fun labelAndHour(t: LocalDateTime): Pair<String, Int> {
            val hour = t.hour
            val label = if (hour < 12) "오전" else "오후"
            var h12 = hour % 12
            if (h12 == 0) h12 = 12
            return Pair(label, h12)
        }

        val (labelStart, hStart) = labelAndHour(start)
        val (_, hEnd) = labelAndHour(end)
        return "$labelStart ${hStart}:00 ~ ${hEnd}:00"
    }

    /* 수정: 예약 생성 - qnas 파라미터를 받아 서버로 전송합니다. */
    fun reserveCoach(coachId: String, startAt: LocalDateTime, endAt: LocalDateTime, qnas: List<String>, coachName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                // 질문 목록을 하나의 문자열로 변환, 비어있으면 null
                val preQnaString = qnas.joinToString("\n") { it.trim() }.takeIf { it.isNotBlank() }

                // Repository에 preQnaString 전달
                val res = try {
                    // If repository supports coachName, pass it along when available
                    if (repo is com.livon.app.data.repository.ReservationRepositoryImpl) {
                        repo.reserveCoach(coachId, startAt, endAt, preQnaString, coachName = coachName)
                    } else {
                        repo.reserveCoach(coachId, startAt, endAt, preQnaString)
                    }
                } catch (t: Throwable) {
                    Result.failure<Int>(t)
                }

                if (res.isSuccess) {
                    val createdId = res.getOrNull()
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null, createdReservationId = createdId)
                    loadUpcoming()
                } else {
                    val ex = res.exceptionOrNull()
                    val msg = when (ex) {
                        is retrofit2.HttpException -> if (ex.code() == 409) "이미 예약된 시간입니다" else ex.message()
                        else -> ex?.message ?: "알 수 없는 오류"
                    }
                    _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = msg)
                }
            } catch (t: Throwable) {
                val msg = when (t) {
                    is retrofit2.HttpException -> if (t.code() == 409) "이미 예약된 시간입니다" else t.message()
                    else -> t.message
                }
                _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = msg)
            }
        }
    }

    /* 수정: 클래스 예약 - qnas 파라미터를 받아 서버로 전송합니다. */
    @Suppress("unused")
    fun reserveClass(classId: String, qnas: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                // 질문 목록을 하나의 문자열로 변환, 비어있으면 null
                val preQnaString = qnas.joinToString("\n") { it.trim() }.takeIf { it.isNotBlank() }

                // Repository에 preQnaString 전달
                val res = repo.reserveClass(classId, preQnaString)

                if (res.isSuccess) {
                    val createdId = res.getOrNull()
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null, createdReservationId = createdId)
                    loadUpcoming()
                } else {
                    val ex = res.exceptionOrNull()
                    fun isAlreadyReserved(error: Throwable?): Boolean {
                        if (error == null) return false
                        // Repository may return a sentinel message beginning with ALREADY_RESERVED:
                        val msg = error.message ?: ""
                        if (msg.startsWith("ALREADY_RESERVED:")) return true
                        // If it's an HttpException with 409 conflict, treat as already reserved
                        if (error is retrofit2.HttpException && error.code() == 409) return true
                        // Try to inspect error body for known DB unique-constraint strings
                        val errorBody = try { (error as? retrofit2.HttpException)?.response()?.errorBody()?.string() ?: "" } catch (_: Throwable) { "" }
                        val combinedText = errorBody + msg
                        return combinedText.contains("이미 예약") || combinedText.contains("Duplicate entry") || combinedText.contains("uk_participant_user_consultation")
                    }

                    if (isAlreadyReserved(ex)) {
                        // Refresh upcoming list so UI shows the reservation, but do NOT mark as success
                        // to prevent composables (which auto-navigate on success) from opening another modal/route.
                        loadUpcoming()
                        _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = "이미 예약된 시간입니다")
                    } else {
                        val msg = ex?.message ?: "알 수 없는 오류"
                        try { Log.e("ReservationVM", "reserveClass failed: $msg", ex) } catch (_: Throwable) {}
                        _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = msg)
                    }
                 }
             } catch (t: Throwable) {
                 _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
             }
         }
    }

    // cancel APIs (기존 코드 유지)
    fun cancelIndividual(consultationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ReservationVM", "cancelIndividual: start id=$consultationId")
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            // optimistic UI: remove item locally immediately and keep backup to restore on failure
            val prevItems = _uiState.value.items
            _uiState.value = _uiState.value.copy(items = prevItems.filterNot { it.id == consultationId.toString() })
            try {
                // 2. [DB] Repository를 통해 서버 API를 호출한다.
                val res = repo.cancelIndividual(consultationId)

                // 3. [DB] API 호출 결과를 확인한다.
                if (res.isSuccess) {
                    // 이 시점에서 서버 DB에서는 데이터가 *이미* 성공적으로 삭제되었습니다.
                    Log.d("ReservationVM", "cancelIndividual: success id=$consultationId")
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)

                    // 4. [UI] 이제 화면을 어떻게 할지 결정한다.
                    // loadUpcoming() // <- 이 코드는 삭제된 데이터를 포함한 '전체 목록'을 다시 가져와 화면을 갱신하는 역할 *뿐*입니다.
                    // DB 상태에 영향을 주지 않습니다.

                } else {
                    // 5. [DB] API 호출이 실패했다 (DB에서 데이터가 삭제되지 않았다).
                    // 6. [UI] 따라서 화면을 원래대로 복원한다.

                    Log.d("ReservationVM", "cancelIndividual: failure id=$consultationId, ex=${res.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(items = prevItems)
                     _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = res.exceptionOrNull()?.message)
                 }
             } catch (t: Throwable) {
                 Log.e("ReservationVM", "cancelIndividual: exception", t)
                 // restore previous list on exception
                 _uiState.value = _uiState.value.copy(items = prevItems)
                 _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
             }
         }
     }

    fun cancelGroupParticipation(consultationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ReservationVM", "cancelGroupParticipation: start id=$consultationId")
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            // optimistic UI: remove item locally immediately and keep backup to restore on failure
            val prevItems = _uiState.value.items
            _uiState.value = _uiState.value.copy(items = prevItems.filterNot { it.id == consultationId.toString() })
            try {
                val token = com.livon.app.data.session.SessionManager.getTokenSync()
                Log.d("ReservationVM", "cancelGroupParticipation: calling repo with id=$consultationId tokenPresent=${!token.isNullOrBlank()}")
                 val res = repo.cancelGroupParticipation(consultationId)
                 if (res.isSuccess) {
                     Log.d("ReservationVM", "cancelGroupParticipation: success id=$consultationId")
                     _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)
                 } else {
                     Log.d("ReservationVM", "cancelGroupParticipation: failure id=$consultationId, ex=${res.exceptionOrNull()?.message}")
                     // restore previous list on failure
                     _uiState.value = _uiState.value.copy(items = prevItems)
                     _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = res.exceptionOrNull()?.message)
                 }
             } catch (t: Throwable) {
                 Log.e("ReservationVM", "cancelGroupParticipation: exception", t)
                 // restore previous list on exception
                 _uiState.value = _uiState.value.copy(items = prevItems)
                 _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
             }
         }
     }
}
