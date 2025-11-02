package com.livon.app.data.remote.dto

data class CoachDto(
    val id: Long,
    val name: String,
    val job: String?,
    val introduce: String?,
    val profileImage: String?,
    val isCorporate: Boolean,
    val certificates: List<String>?
)

data class ConsultationDto(
    val id: Long,
    val coachId: Long,
    val capacity: Int,
    val startAt: String,
    val endAt: String,
    val status: String,
    val type: String,
    val sessionId: String?
)
