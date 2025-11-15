package com.s406.livon.domain.openvidu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RecordingStartRequestDto(
        @NotNull Long consultationId,
        String layout,
        String filename
) {
}
