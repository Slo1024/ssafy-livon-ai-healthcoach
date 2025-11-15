package com.s406.livon.domain.coach.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 모바일 등에서 즉시 사용할 1:1 상담 방 생성 요청 DTO.
 */
public record InstantConsultationCreateRequestDto(

        @Min(value = 15, message = "상담 시간은 최소 15분 이상이어야 합니다.")
        @Max(value = 180, message = "상담 시간은 최대 180분까지 설정할 수 있습니다.")
        Integer durationMinutes,

        @Min(value = 1, message = "정원은 최소 1명 이상이어야 합니다.")
        Integer capacity,

        String preQnA
) {

    public int durationOrDefault() {
        return durationMinutes == null ? 60 : durationMinutes;
    }

    public int capacityOrDefault() {
        return capacity == null ? 1 : capacity;
    }
}
