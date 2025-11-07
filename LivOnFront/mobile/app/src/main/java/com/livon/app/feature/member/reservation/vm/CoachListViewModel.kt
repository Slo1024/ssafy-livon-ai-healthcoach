package com.livon.app.feature.member.reservation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.domain.repository.CoachRepository
import com.livon.app.feature.member.reservation.model.CoachUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CoachListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val coaches: List<CoachUIModel> = emptyList()
)

class CoachListViewModel(private val repo: CoachRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CoachListUiState())
    val uiState: StateFlow<CoachListUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = try { repo.fetchCoaches() } catch (t: Throwable) { Result.failure(t) }
            if (res.isSuccess) {
                _uiState.update { it.copy(isLoading = false, coaches = res.getOrDefault(emptyList())) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = res.exceptionOrNull()?.message) }
            }
        }
    }
}

