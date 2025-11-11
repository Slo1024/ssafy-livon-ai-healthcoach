package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.ReservationRepository
import com.livon.app.feature.member.reservation.ui.ReservationUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.livon.app.data.repository.ReservationType
import android.util.Log

// We'll use the reservation repository for create actions (reserveCoach / reserveClass).
class ReservationViewModel(
    private val repo: ReservationRepository
) : ViewModel() {

    data class ReservationsUiState(
        val items: List<ReservationUi> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    data class ReservationActionState(
        val isLoading: Boolean = false,
        val success: Boolean? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState

    private val _actionState = MutableStateFlow(ReservationActionState())
    val actionState: StateFlow<ReservationActionState> = _actionState

    /**
     * 홈 화면의 "다가오는 예약"은 백엔드의 upcoming 엔드포인트가 아닌
     * 앱 내 로컬 데이터(또는 다른 뷰모델)를 통해 제공되어야 합니다.
     * 여기서는 기본적으로 빈 리스트를 반환하도록 하고, 필요 시
     * 리포지토리에 실제 fetch 함수를 추가해 호출하도록 바꿔주세요.
     */
    fun loadUpcoming() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // First try server-side fetch
                val serverRes = try {
                    repo.getMyReservations(status = "upcoming", type = null)
                } catch (t: Throwable) {
                    Result.failure(t)
                }

                val mappedFromServer = if (serverRes.isSuccess) {
                    val body = serverRes.getOrNull()
                    body?.items?.mapNotNull { dto ->
                        try {
                            val start = java.time.LocalDateTime.parse(dto.startAt ?: java.time.LocalDateTime.now().toString())
                            val end = java.time.LocalDateTime.parse(dto.endAt ?: start.plusHours(1).toString())

                            ReservationUi(
                                id = dto.consultationId.toString(),
                                date = start.toLocalDate(),
                                className = dto.title ?: (if ((dto.type ?: "ONE") == "ONE") "개인 상담" else "그룹 클래스"),
                                coachName = dto.coach?.nickname ?: dto.coach?.userId ?: "",
                                coachRole = dto.coach?.job ?: "",
                                coachIntro = dto.coach?.introduce ?: "",
                                timeText = formatTimeText(start, end),
                                classIntro = dto.description ?: "",
                                imageResId = null,
                                isLive = false,
                                sessionTypeLabel = if ((dto.type ?: "ONE") == "ONE") "개인 상담" else "그룹 상담",
                                hasAiReport = dto.aiSummary != null
                            )
                        } catch (t: Throwable) {
                            null
                        }
                    } ?: emptyList()
                } else emptyList()

                // Merge server results with local cache (local created reservations not yet present on server)
                val local = try { com.livon.app.data.repository.ReservationRepositoryImpl.localReservations.toList() } catch (t: Throwable) { emptyList() }
                val mappedLocal = local.map { lr ->
                    val start = try { java.time.LocalDateTime.parse(lr.startAt) } catch (t: Throwable) { java.time.LocalDateTime.now() }
                    val end = try { java.time.LocalDateTime.parse(lr.endAt) } catch (t: Throwable) { start.plusHours(1) }
                    ReservationUi(
                        id = lr.id.toString(),
                        date = start.toLocalDate(),
                        className = when (lr.type) {
                            ReservationType.PERSONAL -> "개인 상담"
                            ReservationType.GROUP -> "그룹 클래스"
                        },
                        coachName = lr.coachId,
                        coachRole = "",
                        coachIntro = "",
                        timeText = formatTimeText(start, end),
                        classIntro = "",
                        imageResId = null,
                        // set sessionTypeLabel so cancellation logic can decide personal vs group
                        sessionTypeLabel = when (lr.type) {
                            ReservationType.PERSONAL -> "개인 상담"
                            ReservationType.GROUP -> "그룹 상담"
                        }
                    )
                }

                // Prefer server items and append any local-only items that don't collide by id
                val idsFromServer = mappedFromServer.map { it.id }.toSet()
                val combined = mappedFromServer + mappedLocal.filter { it.id !in idsFromServer }

                _uiState.value = ReservationsUiState(items = combined, isLoading = false)
            } catch (t: Throwable) {
                _uiState.value = ReservationsUiState(items = emptyList(), isLoading = false, errorMessage = t.message)
            }
        }
    }

    private fun formatTimeText(start: java.time.LocalDateTime, end: java.time.LocalDateTime): String {
        // 예: "오전 9:00 ~ 10:00" 또는 "오후 3:00 ~ 4:00"
        fun labelAndHour(t: java.time.LocalDateTime): Pair<String, Int> {
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

    /**
     * 예약 생성: 서버에 요청 후 성공하면 로컬 UI 리스트에도 즉시 반영합니다.
     * startAt / endAt은 View (혹은 ViewModel을 호출하는 코드)에서 검증하여
     * 서버 규칙(정시, 1시간 차이, 09~17 범위)을 만족하도록 전달해야 합니다.
     */
    fun reserveCoach(coachId: String, startAt: java.time.LocalDateTime, endAt: java.time.LocalDateTime, preQnA: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                val res = repo.reserveCoach(coachId, startAt, endAt, preQnA)
                if (res.isSuccess) {
                    // 성공하면 action 상태 업데이트
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)

                    // 서버의 최신 예약 목록으로 갱신 (서버가 권장 소스이므로 재조회)
                    // 이로써 ReservationStatusScreen에 반영되도록 보장합니다.
                    loadUpcoming()

                } else {
                    val ex = res.exceptionOrNull()
                    // HTTP 409 검사
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

    /**
     * 클래스 예약(그룹) API 호출: repo.reserveClass를 호출하고 actionState/uiState 갱신
     */
    @Suppress("unused")
    fun reserveClass(classId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                val res = repo.reserveClass(classId)
                if (res.isSuccess) {
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)

                    // 서버의 최신 예약 목록을 재조회하여 UI에 반영
                    loadUpcoming()

                } else {
                    val ex = res.exceptionOrNull()
                    val msg = when (ex) {
                        is retrofit2.HttpException -> if (ex.code() == 409) "이미 예약된 시간입니다" else ex.message()
                        else -> ex?.message ?: "알 수 없는 오류"
                    }

                    if (msg.contains("이미 예약")) {
                        loadUpcoming()
                        _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)
                    } else {
                        _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = msg)
                    }
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

    // cancel APIs
    fun cancelIndividual(consultationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ReservationVM", "cancelIndividual: start id=$consultationId")
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                val res = repo.cancelIndividual(consultationId)
                if (res.isSuccess) {
                    Log.d("ReservationVM", "cancelIndividual: success id=$consultationId")
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)
                    // refresh server list
                    loadUpcoming()
                } else {
                    Log.d("ReservationVM", "cancelIndividual: failure id=$consultationId, ex=${res.exceptionOrNull()?.message}")
                    _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = res.exceptionOrNull()?.message)
                }
            } catch (t: Throwable) {
                Log.e("ReservationVM", "cancelIndividual: exception", t)
                _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
            }
        }
    }

    fun cancelGroupParticipation(consultationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ReservationVM", "cancelGroupParticipation: start id=$consultationId")
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                val res = repo.cancelGroupParticipation(consultationId)
                if (res.isSuccess) {
                    Log.d("ReservationVM", "cancelGroupParticipation: success id=$consultationId")
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)
                    loadUpcoming()
                } else {
                    Log.d("ReservationVM", "cancelGroupParticipation: failure id=$consultationId, ex=${res.exceptionOrNull()?.message}")
                    _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = res.exceptionOrNull()?.message)
                }
            } catch (t: Throwable) {
                Log.e("ReservationVM", "cancelGroupParticipation: exception", t)
                _actionState.value = ReservationActionState(isLoading = false, success = false, errorMessage = t.message)
            }
        }
    }
}
