package com.livon.app.data.remote.mapper

import com.livon.app.data.remote.dto.*
import com.livon.app.domain.model.*
import java.time.LocalDateTime

fun CoachDto.toDomain() = Coach(
    id = id,
    name = name,
    job = job,
    introduce = introduce,
    profileImage = profileImage,
    isCorporate = isCorporate,
    certificates = certificates ?: emptyList()
)

fun ConsultationDto.toDomain() = Consultation(
    id = id,
    coachId = coachId,
    capacity = capacity,
    startAt = LocalDateTime.parse(startAt),
    endAt = LocalDateTime.parse(endAt),
    status = ConsultationStatus.valueOf(status),
    type = ConsultationType.valueOf(type),
    sessionId = sessionId
)
