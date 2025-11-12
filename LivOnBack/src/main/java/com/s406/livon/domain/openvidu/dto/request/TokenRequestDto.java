package com.s406.livon.domain.openvidu.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenRequestDto {
    private Long consultationId;  // 상담 ID
    private String participantName;  // 참가자 이름 (선택사항, 없으면 User nickname 사용)
}