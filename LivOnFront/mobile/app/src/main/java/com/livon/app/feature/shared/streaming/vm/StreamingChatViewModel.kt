package com.livon.app.feature.shared.streaming.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.data.remote.socket.ChatStompManager
import com.livon.app.data.repository.ChatRepositoryImpl
import com.livon.app.domain.model.ChatMessage
import com.livon.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


data class StreamingChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<ChatMessage> = emptyList()
)

class StreamingChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingChatUiState())
    val uiState: StateFlow<StreamingChatUiState> = _uiState

    fun loadChatMessages(
        chatRoomId: Int = 1,
        lastSentAt: String? = null,
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

    fun sendMessage(
        message: String,
        accessToken: String,
        chatRoomId: Int = 1,
        senderUUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    ) {
        if (message.isBlank()) return

        viewModelScope.launch {
            Log.d("StreamingChatViewModel", "메시지 전송 시도: $message")

            try {
                ChatStompManager.sendMessage(
                    token = accessToken,
                    content = message,
                    roomId = chatRoomId.toLong(),
                    senderUUID = senderUUID
                )
                Log.d("StreamingChatViewModel", "STOMP 메시지 발행 성공")
            } catch (e: Exception) {
                Log.e("StreamingChatViewModel", "STOMP 메시지 발행 실패: ${e.message}", e)
                return@launch
            }

            val newMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                chatRoomId = chatRoomId,
                userId = "me",
                content = message,
                sentAt = System.currentTimeMillis().toString(),
                role = "MEMBER",
                messageType = "TEXT"
            )
            _uiState.update { state ->
                state.copy(messages = state.messages + newMessage)
            }
        }
    }

}

