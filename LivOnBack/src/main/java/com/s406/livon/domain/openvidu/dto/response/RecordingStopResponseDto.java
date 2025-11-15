package com.s406.livon.domain.openvidu.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record RecordingStopResponseDto(
        String egressId,
        Long consultationId,
        String status,
        long startedAt,
        long endedAt,
        List<RecordingFileResultDto> files
) {
}
