package com.livon.app.feature.member.schedule.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.data.repository.ConsultationVideoRepositoryImpl
import com.livon.app.domain.repository.ConsultationVideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiResultUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val summary: String = ""
)

class AiResultViewModel(
    private val repository: ConsultationVideoRepository = ConsultationVideoRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiResultUiState())
    val uiState: StateFlow<AiResultUiState> = _uiState

    fun loadSummary(consultationId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getSummary(consultationId)
                .onSuccess { summary ->
                    Log.d("AiResultViewModel", "loadSummary: success for consultationId=$consultationId")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("AiResultViewModel", "loadSummary: failure for consultationId=$consultationId", e)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "영상 요약을 불러올 수 없습니다.",
                            summary = ""
                        )
                    }
                }
        }
    }
}

