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
                val finalList = mappedFromServer.toMutableList()
                try {
                    if (repo is com.livon.app.data.repository.ReservationRepositoryImpl) {
                        val ownerToken = com.livon.app.data.session.SessionManager.getTokenSync()
                        val local = com.livon.app.data.repository.ReservationRepositoryImpl.localReservations.filter { (it.ownerToken ?: "") == (ownerToken ?: "") }
                        val existingIds = finalList.map { it.id }.toMutableSet()
                        val localItems = local.mapNotNull { lr ->
                            try {
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
                        // Append only those local items whose id is not already present
                        localItems.forEach { li ->
                            if (!existingIds.contains(li.id)) {
                                finalList.add(li)
                                existingIds.add(li.id)
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
            val prev = _uiState.value.items
            _uiState.value = _uiState.value.copy(items = prev.filterNot { it.id == consultationId.toString() })
            try {
                val token = com.livon.app.data.session.SessionManager.getTokenSync()
                Log.d("ReservationVM", "cancelIndividual: calling repo with id=$consultationId tokenPresent=${!token.isNullOrBlank()}")
                 val res = repo.cancelIndividual(consultationId)
                 if (res.isSuccess) {
                     Log.d("ReservationVM", "cancelIndividual: success id=$consultationId")
                     _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)
                     loadUpcoming()
                 } else {
                     Log.d("ReservationVM", "cancelIndividual: failure id=$consultationId, ex=${res.exceptionOrNull()?.message}")
                     // restore previous list on failure
                     _uiState.value = _uiState.value.copy(items = prev)
                     _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = res.exceptionOrNull()?.message)
                 }
             } catch (t: Throwable) {
                 Log.e("ReservationVM", "cancelIndividual: exception", t)
                 // restore previous list on exception
                 _uiState.value = _uiState.value.copy(items = prev)
                 _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
             }
         }
     }

    fun cancelGroupParticipation(consultationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ReservationVM", "cancelGroupParticipation: start id=$consultationId")
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            // optimistic UI: remove item locally immediately and keep backup to restore on failure
            val prev = _uiState.value.items
            _uiState.value = _uiState.value.copy(items = prev.filterNot { it.id == consultationId.toString() })
            try {
                val token = com.livon.app.data.session.SessionManager.getTokenSync()
                Log.d("ReservationVM", "cancelGroupParticipation: calling repo with id=$consultationId tokenPresent=${!token.isNullOrBlank()}")
                 val res = repo.cancelGroupParticipation(consultationId)
                 if (res.isSuccess) {
                     Log.d("ReservationVM", "cancelGroupParticipation: success id=$consultationId")
                     _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)
                     loadUpcoming()
                 } else {
                     Log.d("ReservationVM", "cancelGroupParticipation: failure id=$consultationId, ex=${res.exceptionOrNull()?.message}")
                     // restore previous list on failure
                     _uiState.value = _uiState.value.copy(items = prev)
                     _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = res.exceptionOrNull()?.message)
                 }
             } catch (t: Throwable) {
                 Log.e("ReservationVM", "cancelGroupParticipation: exception", t)
                 // restore previous list on exception
                 _uiState.value = _uiState.value.copy(items = prev)
                 _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
             }
         }
     }
}
