package com.livon.app.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class ChatMessageDto(
    val id: String,
    val chatRoomId: Int,
    val userId: String,
    val content: String,
    val sentAt: String,
    val role: String, // null 가능
    val messageType: String
)

@Serializable
data class ChatMessageResponseDto(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ChatMessageDto>
)


