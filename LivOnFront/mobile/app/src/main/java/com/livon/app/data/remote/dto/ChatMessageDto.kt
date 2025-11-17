package com.livon.app.data.remote.dto

import com.squareup.moshi.Json


data class ChatMessageDto(
    @Json(name = "chatMessageId")
    val id: String,
    @Json(name = "roomId")
    val chatRoomId: Int,
    @Json(name = "senderId")
    val userId: String,
    @Json(name = "message")
    val content: String,
    @Json(name = "sentAt")
    val sentAt: String,
    @Json(name = "senderRole")
    val role: String,
    @Json(name = "messageType")
    val messageType: String,
    @Json(name = "senderNickname")
    val senderNickname: String? = null,
    @Json(name = "senderImageUrl")
    val senderImageUrl: String? = null
)

data class ChatMessageResponseDto(
    @Json(name = "isSuccess")
    val isSuccess: Boolean,
    @Json(name = "code")
    val code: String,
    @Json(name = "message")
    val message: String,
    @Json(name = "result")
    val result: List<ChatMessageDto>
)

data class ChatRoomInfoDto(
    @Json(name = "chatRoomId")
    val chatRoomId: Long,
    @Json(name = "consultationId")
    val consultationId: Long,
    @Json(name = "chatRoomStatus")
    val chatRoomStatus: String
)

data class ChatRoomInfoResponseDto(
    @Json(name = "isSuccess")
    val isSuccess: Boolean,
    @Json(name = "code")
    val code: String,
    @Json(name = "message")
    val message: String,
    @Json(name = "result")
    val result: ChatRoomInfoDto
)


