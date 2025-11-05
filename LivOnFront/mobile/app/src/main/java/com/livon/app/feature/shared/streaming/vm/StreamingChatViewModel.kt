package com.livon.app.feature.shared.streaming.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.data.repository.ChatRepositoryImpl
import com.livon.app.domain.repository.ChatRepository
import com.livon.app.feature.shared.streaming.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 채팅 화면 UI State
 */
data class StreamingChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<ChatMessage> = emptyList()
)

/**
 * 채팅 화면 ViewModel
 */
class StreamingChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingChatUiState())
    val uiState: StateFlow<StreamingChatUiState> = _uiState

    fun loadChatMessages(
        chatRoomId: Int = 0,
        lastSentAt: String? = "",
        accessToken: String? = ""
    ) {
        viewModelScope.launch {
            Log.d("StreamingChatViewModel", "채팅 메시지 로드 시작: chatRoomId=$chatRoomId")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getChatMessages(chatRoomId, lastSentAt, accessToken)
                .onSuccess { messages ->
                    Log.d("StreamingChatViewModel", "채팅 메시지 로드 성공: ${messages.size}개")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages
                        )
                    }
                }
                .onFailure { e ->
                    val errorMsg = e.message ?: "채팅 메시지 조회 실패"
                    Log.e("StreamingChatViewModel", "채팅 메시지 로드 실패: $errorMsg", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    }
                }
        }
    }
}

