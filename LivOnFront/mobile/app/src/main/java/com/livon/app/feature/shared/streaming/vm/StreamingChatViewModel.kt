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
    private val consultationId: Long,
    private val jwtToken: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingChatUiState())
    val uiState: StateFlow<StreamingChatUiState> = _uiState

    init {
        // 1) STOMP 연결 (웹소켓 접속)
        viewModelScope.launch {
            try {
                ChatStompManager.connect(
                    token = jwtToken,
                    roomId = consultationId,
                    scope = viewModelScope
                )
                
                // 2) 연결 완료 대기
                ChatStompManager.subscriptionReady.collect { isReady ->
                    if (isReady) {
                        Log.d("StreamingChatViewModel", "웹소켓 연결 완료, POST 요청 시작")
                        
                        // 3) POST /api/v1/goods/chat?consultationId=방번호 요청 (구독 전에 먼저 실행)
                        Log.d("StreamingChatViewModel", "채팅방 정보 조회 시작: consultationId=$consultationId")
                        repository.getChatRoomInfo(consultationId, jwtToken)
                            .onSuccess { chatRoomInfo ->
                                Log.d("StreamingChatViewModel", "채팅방 정보 조회 성공: chatRoomId=${chatRoomInfo.chatRoomId}, consultationId=${chatRoomInfo.consultationId}, status=${chatRoomInfo.chatRoomStatus}")
                                
                                // 4) POST 요청 성공 후 구독 시작
                                Log.d("StreamingChatViewModel", "구독 시작")
                                ChatStompManager.subscribe(consultationId, viewModelScope)
                            }
                            .onFailure { e ->
                                Log.e("StreamingChatViewModel", "채팅방 정보 조회 실패: ${e.message}", e)
                                // 실패해도 구독은 시도
                                Log.d("StreamingChatViewModel", "구독 시작 (POST 실패했지만 시도)")
                                ChatStompManager.subscribe(consultationId, viewModelScope)
                            }
                        return@collect // 첫 번째 ready 신호만 처리
                    }
                }
            } catch (e: Exception) {
                Log.e("StreamingChatViewModel", "STOMP connect 실패: ${e.message}", e)
            }
        }

        // 5) 수신된 메시지를 collect 하여 UI 상태 갱신
        viewModelScope.launch {
            ChatStompManager.incomingMessages.collect { payload ->
                Log.d("StreamingChatViewModel", "수신 payload: $payload")
                // JSON 파싱은 서버 응답 형식에 맞게 조정하세요.
                val message = try {
                    val json = JSONObject(payload)
                    ChatMessage(
                        id = json.optString("id", UUID.randomUUID().toString()),
                        chatRoomId = json.optInt("roomId", consultationId.toInt()),
                        userId = json.optString("senderId", json.optString("userId", "unknown")),
                        content = json.optString("message", ""),
                        sentAt = json.optString("sentAt", Instant.now().toString()),
                        role = json.optString("role", "COACH"),
                        messageType = json.optString("type", "TEXT")
                    )
                } catch (e: Exception) {
                    Log.e("StreamingChatViewModel", "수신 메시지 파싱 실패: ${e.message}")
                    null
                }

                if (message != null) {
                    _uiState.update { state ->
                        // 중복 체크: 같은 내용의 메시지가 최근 3초 이내에 있으면 제외
                        val now = Instant.now()
                        val recentMessages = state.messages.filter { msg ->
                            try {
                                val msgTime = Instant.parse(msg.sentAt)
                                now.epochSecond - msgTime.epochSecond <= 3 // 3초 이내
                            } catch (e: Exception) {
                                false
                            }
                        }
                        
                        // 같은 내용의 메시지가 최근에 있으면 추가하지 않음
                        val isDuplicate = recentMessages.any { msg ->
                            msg.content == message.content && 
                            msg.chatRoomId == message.chatRoomId
                        }
                        
                        if (isDuplicate) {
                            Log.d("StreamingChatViewModel", "중복 메시지 감지, 추가하지 않음: ${message.content}")
                            state // 변경 없음
                        } else {
                            val updated = (state.messages + message)
                                .distinctBy { it.id }
                                .sortedBy { it.sentAt }
                            state.copy(messages = updated)
                        }
                    }
                }
            }
        }
    }

    fun loadChatMessages(
        chatRoomId: Int = consultationId.toInt(),
        accessToken: String? = jwtToken,
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
        accessToken: String = jwtToken,
        chatRoomId: Int = consultationId.toInt(),
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

