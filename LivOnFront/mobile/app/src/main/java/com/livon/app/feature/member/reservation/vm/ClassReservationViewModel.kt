package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.GroupConsultationRepository
import com.livon.app.feature.member.reservation.ui.SampleClassInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ClassReservationUiState(
    val items: List<SampleClassInfo> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class ClassReservationViewModel(private val repo: GroupConsultationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ClassReservationUiState())
    val uiState: StateFlow<ClassReservationUiState> = _uiState

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val res = repo.fetchClasses()
                if (res.isSuccess) {
                    _uiState.value = ClassReservationUiState(items = res.getOrNull() ?: emptyList(), loading = false)
                } else {
                    _uiState.value = _uiState.value.copy(loading = false, error = res.exceptionOrNull()?.message)
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(loading = false, error = t.message)
            }
        }
    }
}

