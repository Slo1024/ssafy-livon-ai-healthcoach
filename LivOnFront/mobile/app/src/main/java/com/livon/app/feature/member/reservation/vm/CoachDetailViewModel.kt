package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.CoachRepository
import com.livon.app.feature.member.reservation.model.CoachUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CoachDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val coach: CoachUIModel? = null
)

class CoachDetailViewModel(private val repo: CoachRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CoachDetailUiState())
    val uiState: StateFlow<CoachDetailUiState> = _uiState

    fun load(coachId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = try { repo.fetchCoachDetail(coachId) } catch (t: Throwable) { Result.failure(t) }
            if (res.isSuccess) {
                _uiState.update { it.copy(isLoading = false, coach = res.getOrNull()) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = res.exceptionOrNull()?.message) }
            }
        }
    }
}

