package com.s406.livon.domain.ai.gms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AiSummaryResponseDto {
    private UUID userId;
    private String summary;

    public static AiSummaryResponseDto of(UUID userId, String summary) {
        return AiSummaryResponseDto.builder()
                .userId(userId)
                .summary(summary)
                .build();
    }
}
