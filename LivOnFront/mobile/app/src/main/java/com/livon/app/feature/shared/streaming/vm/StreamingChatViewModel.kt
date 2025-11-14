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
    
    private var isChatRoomInfoRequested = false // POST 요청이 이미 실행되었는지 확인하는 플래그

    init {
        // 1) STOMP 연결 (웹소켓 접속)
        viewModelScope.launch {
            try {
                ChatStompManager.connect(
                    token = jwtToken,
                    roomId = consultationId,
                    scope = viewModelScope
                )
                
                // 2) 연결 완료 대기 (한 번만 처리)
                ChatStompManager.subscriptionReady.collect { isReady ->
                    if (isReady && !isChatRoomInfoRequested) {
                        isChatRoomInfoRequested = true // 플래그 설정하여 중복 실행 방지
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
                    // role이 없거나 빈 문자열이면 "MEMBER"로 기본값 설정
                    val roleString = json.optString("role", "").takeIf { it.isNotBlank() } ?: "MEMBER"
                    ChatMessage(
                        id = json.optString("id", UUID.randomUUID().toString()),
                        chatRoomId = json.optInt("roomId", consultationId.toInt()),
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
                        // 1) ID로 중복 체크 (가장 확실한 방법)
                        val existsById = state.messages.any { it.id == message.id }
                        if (existsById) {
                            Log.d("StreamingChatViewModel", "중복 메시지 감지 (ID): ${message.id}")
                            return@update state // 변경 없음
                        }
                        
                        // 2) 내용 + 시간 + userId로 중복 체크 (같은 사용자가 같은 내용을 같은 시간에 보낸 경우)
                        val isDuplicate = state.messages.any { msg ->
                            try {
                                val msgTime = Instant.parse(msg.sentAt)
                                val newTime = Instant.parse(message.sentAt)
                                val timeDiff = kotlin.math.abs(msgTime.epochSecond - newTime.epochSecond)
                                
                                // 같은 내용, 같은 사용자, 5초 이내
                                msg.content == message.content && 
                                msg.userId == message.userId &&
                                msg.chatRoomId == message.chatRoomId &&
                                timeDiff <= 5
                            } catch (e: Exception) {
                                false
                            }
                        }
                        
                        if (isDuplicate) {
                            Log.d("StreamingChatViewModel", "중복 메시지 감지 (내용+시간+사용자): ${message.content}")
                            return@update state // 변경 없음
                        }
                        
                        // 중복이 아니면 추가
                        val updated = (state.messages + message)
                            .distinctBy { it.id } // ID 중복 제거 (안전장치)
                            .sortedBy { it.sentAt }
                        state.copy(messages = updated)
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
                        
                        // 중복 제거: ID로 먼저 제거
                        val dedupedById = combinedMessages.distinctBy { it.id }
                        
                        // 추가 중복 제거: 같은 내용 + 같은 사용자 + 같은 시간(5초 이내)
                        val finalMessages = mutableListOf<ChatMessage>()
                        
                        for (msg in dedupedById) {
                            val isDuplicate = finalMessages.any { existing ->
                                try {
                                    val existingTime = Instant.parse(existing.sentAt)
                                    val msgTime = Instant.parse(msg.sentAt)
                                    val timeDiff = kotlin.math.abs(existingTime.epochSecond - msgTime.epochSecond)
                                    
                                    existing.content == msg.content &&
                                    existing.userId == msg.userId &&
                                    existing.chatRoomId == msg.chatRoomId &&
                                    timeDiff <= 5
                                } catch (e: Exception) {
                                    false
                                }
                            }
                            
                            if (!isDuplicate) {
                                finalMessages.add(msg)
                            }
                        }
                        
                        val sortedMessages = finalMessages.sortedBy { it.sentAt }

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

