package com.s406.livon.domain.openvidu.dto.response;

import lombok.Builder;

@Builder
public record RecordingStartResponseDto(
        String egressId,
        Long consultationId,
        String sessionId,
        String status,
        String filePath,
        long startedAt
) {
}
