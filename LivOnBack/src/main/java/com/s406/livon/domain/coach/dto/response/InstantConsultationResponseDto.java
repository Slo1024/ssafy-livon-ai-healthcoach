package com.s406.livon.domain.coach.dto.response;

import com.s406.livon.domain.coach.entity.Consultation;
import java.time.LocalDateTime;

public record InstantConsultationResponseDto(
        Long consultationId,
        String sessionId,
        LocalDateTime startAt,
        LocalDateTime endAt
) {

    public static InstantConsultationResponseDto of(Consultation consultation) {
        return new InstantConsultationResponseDto(
                consultation.getId(),
                consultation.getSessionId(),
                consultation.getStartAt(),
                consultation.getEndAt()
        );
    }
}
