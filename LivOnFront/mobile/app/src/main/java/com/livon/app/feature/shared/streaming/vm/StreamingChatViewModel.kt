package com.livon.app.feature.shared.streaming.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livon.app.BuildConfig
import com.livon.app.data.remote.socket.ChatStompManager
import com.livon.app.data.repository.ChatRepositoryImpl
import com.livon.app.domain.model.ChatMessage
import com.livon.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.time.Instant


data class StreamingChatUiState(
    val isLoading: Boolean = false,
    val isLoadingPast: Boolean = false,
    val error: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLastPage: Boolean = false,
    val showPastLoadingIndicator: Boolean = false
)

class StreamingChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingChatUiState())
    val uiState: StateFlow<StreamingChatUiState> = _uiState

    fun loadChatMessages(
        chatRoomId: Int = 43,
        accessToken: String? = BuildConfig.WEBSOCKET_TOKEN,
        isInitialLoad: Boolean = true
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.isLoading || currentState.isLoadingPast || (!isInitialLoad && currentState.isLastPage)) {
                Log.d("ChatVM", "로드 요청 거부: 로딩 중이거나 마지막 페이지임")
                return@launch
            }

            val paginationCursor = if (isInitialLoad) null else currentState.messages.firstOrNull()?.sentAt

            Log.d("StreamingChatViewModel", "채팅 메시지 로드 시작: chatRoomId=$chatRoomId")
            _uiState.update {
                it.copy(
                    isLoading = isInitialLoad,
                    isLoadingPast = !isInitialLoad,
                    showPastLoadingIndicator = !isInitialLoad && paginationCursor != null,
                    error = null
                )
            }

        repository.getChatMessages(chatRoomId, paginationCursor, accessToken)
                .onSuccess { newMessages ->
                    _uiState.update { currentState ->
                        val existingMessages = currentState.messages
                    val combinedMessages =
                        if (isInitialLoad) newMessages
                        else newMessages + existingMessages
                        val dedupedMessages = combinedMessages
                            .distinctBy { it.id }
                    val sortedMessages = dedupedMessages
                        .sortedBy { it.sentAt }

                        currentState.copy(
                            isLoading = false,
                            isLoadingPast = false,
                            showPastLoadingIndicator = false,
                            messages = sortedMessages,
                            isLastPage = newMessages.isEmpty()
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
        chatRoomId: Int = 43,
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
                id = UUID.randomUUID().toString(),
                chatRoomId = chatRoomId,
                userId = "me",
                content = message,
                sentAt = Instant.now().toString(),
                role = "MEMBER",
                messageType = "TEXT"
            )
            _uiState.update { state ->
                val updated = (state.messages + newMessage)
                    .distinctBy { it.id }
                    .sortedBy { it.sentAt }
                state.copy(messages = updated)
            }
        }
    }

}

