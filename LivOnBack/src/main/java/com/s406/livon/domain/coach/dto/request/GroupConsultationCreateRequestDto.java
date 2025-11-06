package com.s406.livon.domain.coach.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public record GroupConsultationCreateRequestDto(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "설명은 필수입니다")
        String description,

        @NotNull(message = "시작 시간은 필수입니다")
        @Future(message = "시작 시간은 미래여야 합니다")
        LocalDateTime startAt,

        @NotNull(message = "종료 시간은 필수입니다")
        LocalDateTime endAt,

        @NotNull(message = "최대 인원은 필수입니다")
        @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다")
        @Max(value = 100, message = "최대 인원은 100명 이하여야 합니다")
        Integer capacity
) {
    // 시작/종료 시간 검증
    public GroupConsultationCreateRequestDto {
        if (endAt != null && startAt != null && !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다");
        }
    }
}