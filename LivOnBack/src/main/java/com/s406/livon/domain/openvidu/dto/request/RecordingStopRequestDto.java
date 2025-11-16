package com.s406.livon.domain.openvidu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RecordingStopRequestDto(
        @NotNull Long consultationId,
        @NotBlank String egressId
) {
}
