package com.livon.app.domain.model

data class ChatMessage(
    val id: String,
    val chatRoomId: Int,
    val userId: String,
    val content: String,
    val sentAt: String,
    val role: String,
    val messageType: String,
    val nickname: String? = null,
    val profileImageUrl: String? = null
)

