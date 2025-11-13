package com.livon.app.domain.repository

import com.livon.app.domain.model.ChatMessage

/**
 * 채팅 메시지 Repository 인터페이스
 */
interface ChatRepository {
    suspend fun getChatMessages(
        chatRoomId: Int,
        lastSentAt: String? = null,
        accessToken: String? = null
    ): Result<List<ChatMessage>>
    
    suspend fun getChatRoomInfo(
        consultationId: Long,
        accessToken: String? = null
    ): Result<com.livon.app.data.remote.dto.ChatRoomInfoDto>
}

