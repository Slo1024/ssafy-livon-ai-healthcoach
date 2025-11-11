package com.s406.livon.domain.consultation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ParticipantInfoDto {
    private UUID userId;
    private String nickname;
    private String profileImage;
    private String email;
}