package com.livon.app.domain.model

import java.time.LocalDateTime

/* ---------- Enums ---------- */
enum class Role { MEMBER, COACH }
enum class Gender { MALE, FEMALE }
enum class ConsultationType { ONE, GROUP, BREAK }
enum class ConsultationStatus { OPEN, CLOSE }

/* ---------- User ---------- */
data class User(
    val id: Long,
    val nickname: String,
    val email: String,
    val profileImage: String?,
    val gender: Gender?,
    val birth: String?,
    val role: Role
)

/* ---------- Coach ---------- */
data class Coach(
    val id: Long,
    val name: String,
    val job: String?,
    val introduce: String?,
    val profileImage: String?,
    val isCorporate: Boolean,
    val certificates: List<String>
)

/* ---------- Consultation ---------- */
data class Consultation(
    val id: Long,
    val coachId: Long,
    val capacity: Int,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val status: ConsultationStatus,
    val type: ConsultationType,
    val sessionId: String?
)

/* ---------- Group Consultation ---------- */
data class GroupConsultation(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?
)

/* ---------- Individual Consultation ---------- */
data class IndividualConsultation(
    val id: Long,
    val aiSummary: String?,
    val preQna: String?
)

/* ---------- Participant ---------- */
data class Participant(
    val id: Long,
    val userId: Long,
    val consultationId: Long
)
