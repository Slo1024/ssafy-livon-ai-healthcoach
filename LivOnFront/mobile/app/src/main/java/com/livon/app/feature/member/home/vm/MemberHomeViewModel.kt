package com.livon.app.feature.member.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.data.repository.DummyHomeRepository
import com.livon.app.domain.model.Upcoming
import com.livon.app.domain.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemberHomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val upcoming: List<Upcoming> = emptyList()
)

class MemberHomeViewModel(
    private val repo: HomeRepository = DummyHomeRepository() // 임시 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberHomeUiState())
    val uiState: StateFlow<MemberHomeUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.fetchUpcoming(limit = 5) }
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, upcoming = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "불러오기 실패") } }
        }
    }
}
