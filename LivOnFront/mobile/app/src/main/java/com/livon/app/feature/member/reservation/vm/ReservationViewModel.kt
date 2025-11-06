package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.ReservationRepository
import com.livon.app.feature.member.reservation.ui.ReservationUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservationsUiState(
    val items: List<ReservationUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReservationViewModel(private val repo: ReservationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState

    fun loadUpcoming() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repo.fetchUpcoming()
            if (res.isSuccess) {
                _uiState.value = ReservationsUiState(items = res.getOrNull() ?: emptyList(), isLoading = false)
            } else {
                _uiState.value = ReservationsUiState(items = emptyList(), isLoading = false, errorMessage = res.exceptionOrNull()?.message)
            }
        }
    }
}

