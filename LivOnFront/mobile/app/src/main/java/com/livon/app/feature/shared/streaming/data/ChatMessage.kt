package com.livon.app.feature.shared.streaming.data

/**
 * 채팅 메시지 도메인 모델
 */
data class ChatMessage(
    val id: String,
    val chatRoomId: Int,
    val userId: String,
    val content: String,
    val sentAt: String,
    val role: List<String>,
    val messageType: String
)


