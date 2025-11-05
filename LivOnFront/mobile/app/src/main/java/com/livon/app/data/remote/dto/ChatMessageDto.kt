package com.livon.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * 채팅 메시지 DTO (API 응답의 result 배열 내부 객체)
 */
@Serializable
data class ChatMessageDto(
    val id: String,
    val chatRoomId: Int,
    val userId: String,
    val content: String,
    val sentAt: String,
    val role: List<String>? = null, // null 가능
    val messageType: String
)

/**
 * 채팅 메시지 조회 응답 DTO
 */
@Serializable
data class ChatMessageResponseDto(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ChatMessageDto>
)


