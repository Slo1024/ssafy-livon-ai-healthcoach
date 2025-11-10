package com.s406.livon.domain.consultation.dto.response;

import com.s406.livon.domain.coach.dto.response.CoachDetailResponseDto;
import com.s406.livon.domain.coach.entity.Consultation;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MyReservationResponseDto(
        Long consultationId,
        Consultation.Type type,
        Consultation.Status status,  // 취소 여부 확인용
        LocalDateTime startAt,
        LocalDateTime endAt,
        String sessionId,

        // 1:1 상담일 경우
        CoachDetailResponseDto coach,
        String preQna,
        String aiSummary,  // past일 때만 값이 있음

        // 1:N 상담일 경우
        String title,
        String description,
        String imageUrl,
        Integer capacity,
        Integer currentParticipants
) {
}