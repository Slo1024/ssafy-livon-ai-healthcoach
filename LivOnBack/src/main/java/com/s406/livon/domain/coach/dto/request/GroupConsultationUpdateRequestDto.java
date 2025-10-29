package com.s406.livon.domain.coach.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record GroupConsultationUpdateRequestDto(
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,
        
        String description,
        
        String imageUrl,
        
        LocalDateTime startAt,
        
        LocalDateTime endAt,
        
        @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다")
        @Max(value = 100, message = "최대 인원은 100명 이하여야 합니다")
        Integer capacity
) {
    // 모든 필드 Optional (수정하고 싶은 것만 보내기)
}