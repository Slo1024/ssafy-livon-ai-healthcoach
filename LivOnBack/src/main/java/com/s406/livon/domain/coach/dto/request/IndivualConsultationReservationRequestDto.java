package com.s406.livon.domain.coach.dto.request;

import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record IndivualConsultationReservationRequestDto(

        @NotNull
        UUID coachId,

        @NotNull(message = "시작 시간은 필수입니다")
        LocalDateTime startAt,

        @NotNull(message = "종료 시간은 필수입니다")
        LocalDateTime endAt,

        @Nullable
        String preQnA
) {
}
