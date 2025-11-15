package com.s406.livon.domain.consultation.dto.response;

import lombok.Builder;

/**
 * 코치가 상담 참여자 정보를 조회할 때 사용하는 응답 DTO
 */
@Builder
public record ParticipantInfoResponseDto(
        MemberInfo memberInfo,
        String aiSummary
) {
    
    @Builder
    public record MemberInfo(
            String nickname,
            String gender,
            String ageGroup,
            HealthData healthData
    ) {}
    
    @Builder
    public record HealthData(
            Double height,
            Double weight,
            Integer steps,
            Integer sleepTime, // 분 단위
            String activityLevel,
            String sleepQuality,
            String stressLevel
    ) {}
}
