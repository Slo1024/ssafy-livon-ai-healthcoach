package com.livon.app.feature.shared.streaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.data.repository.ReservationRepositoryImpl
import com.livon.app.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuickRoomUiState(
    val isCreating: Boolean = false,
    val createdConsultationId: Long? = null,
    val participantName: String? = null,
    val errorMessage: String? = null
)

class QuickRoomViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickRoomUiState())
    val uiState: StateFlow<QuickRoomUiState> = _uiState

    fun createInstantRoom(
        participantName: String,
        durationMinutes: Int = 60,
        capacity: Int = 1,
        preQna: String? = null
    ) {
        if (participantName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "참가자 이름을 입력해 주세요.")
            return
        }
        viewModelScope.launch {
            _uiState.value = QuickRoomUiState(isCreating = true)
            val result = reservationRepository.createInstantConsultation(durationMinutes, capacity, preQna)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    QuickRoomUiState(
                        createdConsultationId = response.consultationId,
                        participantName = participantName
                    )
                },
                onFailure = { throwable ->
                    QuickRoomUiState(
                        errorMessage = throwable.message ?: "방 생성 중 문제가 발생했습니다."
                    )
                }
            )
        }
    }

    fun consumeNavigationEvent() {
        _uiState.value = _uiState.value.copy(
                createdConsultationId = null,
                participantName = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
