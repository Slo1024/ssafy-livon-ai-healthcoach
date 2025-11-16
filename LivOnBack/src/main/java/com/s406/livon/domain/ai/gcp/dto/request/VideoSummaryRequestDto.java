package com.s406.livon.domain.ai.gcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영상 요약 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSummaryRequestDto {

    /**
     * 상담 ID (individual_consultation.id)
     */
    private Long consultationId;

    /**
     * 영상 URL (GCS 또는 외부 URL)
     */
    private String videoUrl;

    /**
     * 사전 QnA (선택사항)
     */
    private String preQnA;
}

