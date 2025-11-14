package com.s406.livon.domain.openvidu.dto.response;

import lombok.Builder;

@Builder
public record RecordingFileResultDto(
        String filename,
        String location,
        long size,
        long duration
) {
}
