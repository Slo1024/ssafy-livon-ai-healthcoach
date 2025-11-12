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
import org.json.JSONObject
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
    private val repository: ChatRepository = ChatRepositoryImpl(),
    private val chatRoomId: Int = 43
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingChatUiState())
    val uiState: StateFlow<StreamingChatUiState> = _uiState

    init {
        // 1) STOMP 연결 (viewModelScope을 넘겨서 SharedFlow emit이 안전하게 동작하도록 함)
        viewModelScope.launch {
            try {
                ChatStompManager.connect(
                    token = BuildConfig.WEBSOCKET_TOKEN,
                    roomId = chatRoomId.toLong(),
                    scope = viewModelScope
                )
            } catch (e: Exception) {
                Log.e("StreamingChatViewModel", "STOMP connect 실패: ${e.message}", e)
            }
        }

        // 2) 수신된 메시지를 collect 하여 UI 상태 갱신
        viewModelScope.launch {
            ChatStompManager.incomingMessages.collect { payload ->
                Log.d("StreamingChatViewModel", "수신 payload: $payload")
                // JSON 파싱은 서버 응답 형식에 맞게 조정하세요.
                val message = try {
                    val json = JSONObject(payload)
                    // role이 없거나 빈 문자열이면 "MEMBER"로 기본값 설정
                    val roleString = json.optString("role", "").takeIf { it.isNotBlank() } ?: "MEMBER"
                    ChatMessage(
                        id = json.optString("id", UUID.randomUUID().toString()),
                        chatRoomId = json.optInt("roomId", chatRoomId),
                        userId = json.optString("senderId", json.optString("userId", "unknown")),
                        content = json.optString("message", ""),
                        sentAt = json.optString("sentAt", Instant.now().toString()),
                        role = roleString,
                        messageType = json.optString("type", "TEXT")
                    )
                } catch (e: Exception) {
                    Log.e("StreamingChatViewModel", "수신 메시지 파싱 실패: ${e.message}")
                    null
                }

                if (message != null) {
                    _uiState.update { state ->
                        val updated = (state.messages + message)
                            .distinctBy { it.id }
                            .sortedBy { it.sentAt }
                        state.copy(messages = updated)
                    }
                }
            }
        }
    }

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
                // 서버에서 받은 메시지만 표시되도록 로컬 메시지 추가 제거
                // WebSocket을 통해 서버에서 받은 메시지가 init 블록의 incomingMessages.collect에서 처리됨
            } catch (e: Exception) {
                Log.e("StreamingChatViewModel", "STOMP 메시지 발행 실패: ${e.message}", e)
                return@launch
            }
        }
    }

}

