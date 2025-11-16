package com.s406.livon.domain.ai.gcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영상 업로드 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadRequestDto {

    /**
     * 상담 ID
     */
    private Long consultationId;
}

