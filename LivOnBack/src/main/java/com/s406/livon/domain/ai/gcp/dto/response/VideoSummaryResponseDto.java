package com.s406.livon.domain.ai.gcp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영상 요약 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSummaryResponseDto {

    /**
     * 상담 ID
     */
    private Long consultationId;

    /**
     * AI가 생성한 영상 요약
     */
    private String summary;
}

