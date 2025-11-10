package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.ReservationRepository
import com.livon.app.feature.member.reservation.ui.ReservationUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

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
                // 현재 백엔드에 upcoming API가 없으므로 빈 리스트를 사용합니다.
                // 추후 server API가 준비되면 repo 또는 api를 통해 실제 데이터를 가져오도록 변경하세요.
                val mapped = emptyList<ReservationUi>()
                _uiState.value = ReservationsUiState(items = mapped, isLoading = false)
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

                    // 서버가 생성한 예약 id(정수) 가져오기
                    val createdId: Int? = res.getOrNull()

                    val date: LocalDate = startAt.toLocalDate()
                    val timeText: String = formatTimeText(startAt, endAt)

                    // ReservationUi 생성 (필수 필드만 채움)
                    val newUi = ReservationUi(
                        id = createdId?.toString() ?: "local-${System.currentTimeMillis()}",
                        date = date,
                        className = "개인 상담",
                        coachName = coachId,
                        coachRole = "",
                        coachIntro = "",
                        timeText = timeText,
                        classIntro = "",
                        imageResId = null
                    )

                    // 기존 리스트에 추가하여 UI에 즉시 반영
                    _uiState.value = _uiState.value.copy(items = _uiState.value.items + newUi)
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
    fun reserveClass(classId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ReservationActionState(isLoading = true, success = null, errorMessage = null)
            try {
                val res = repo.reserveClass(classId)
                if (res.isSuccess) {
                    _actionState.value = ReservationActionState(isLoading = false, success = true, errorMessage = null)

                    val createdId: Int? = res.getOrNull()

                    // 단순히 UI에 반영할 ReservationUi 생성
                    val newUi = ReservationUi(
                        id = createdId?.toString() ?: "local-${System.currentTimeMillis()}",
                        date = LocalDate.now(),
                        className = "클래스 예약",
                        coachName = "",
                        coachRole = "",
                        coachIntro = "",
                        timeText = "",
                        classIntro = "",
                        imageResId = null
                    )

                    _uiState.value = _uiState.value.copy(items = _uiState.value.items + newUi)
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
}
