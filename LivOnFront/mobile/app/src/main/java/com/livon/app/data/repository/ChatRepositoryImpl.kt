package com.livon.app.data.repository

import android.util.Log
import com.livon.app.data.remote.api.ChatApi
import com.livon.app.data.remote.api.ChatApiImpl
import com.livon.app.data.remote.dto.ChatMessageDto
import com.livon.app.domain.repository.ChatRepository
import com.livon.app.feature.shared.streaming.data.ChatMessage

/**
 * 채팅 메시지 Repository 구현체
 */
class ChatRepositoryImpl(
    private val chatApi: ChatApi = ChatApiImpl()
) : ChatRepository {

    override suspend fun getChatMessages(
        chatRoomId: Int,
        lastSentAt: String?,
        accessToken: String?
    ): Result<List<ChatMessage>> {
        return runCatching {
            Log.d("ChatRepository", "채팅 메시지 조회 시작: chatRoomId=$chatRoomId, lastSentAt=$lastSentAt")
            
            val response = chatApi.getChatMessages(chatRoomId, lastSentAt, accessToken)
            
            if (response.isSuccess) {
                val messages = response.result.map { dto ->
                    dto.toDomainModel()
                }
                Log.d("ChatRepository", "채팅 메시지 조회 성공: ${messages.size}개 메시지")
                messages
            } else {
                val errorMsg = response.message ?: "채팅 메시지 조회 실패"
                Log.e("ChatRepository", "API 응답 실패: $errorMsg")
                throw Exception(errorMsg)
            }
        }.onFailure { e ->
            Log.e("ChatRepository", "채팅 메시지 조회 실패: ${e.message}", e)
        }
    }
}

/**
 * DTO를 Domain Model로 변환하는 확장 함수
 */
private fun ChatMessageDto.toDomainModel(): ChatMessage {
    return ChatMessage(
        id = this.id,
        chatRoomId = this.chatRoomId,
        userId = this.userId,
        content = this.content,
        sentAt = this.sentAt,
        role = this.role ?: emptyList(), // null이면 빈 리스트로 변환
        messageType = this.messageType
    )
}
