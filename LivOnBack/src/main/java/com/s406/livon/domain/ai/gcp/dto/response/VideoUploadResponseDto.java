package com.s406.livon.domain.ai.gcp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영상 업로드 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadResponseDto {

    /**
     * 상담 ID
     */
    private Long consultationId;

    /**
     * GCS URI (gs://bucket/object)
     */
    private String gcsUri;

    /**
     * 공개 URL
     */
    private String publicUrl;
}

