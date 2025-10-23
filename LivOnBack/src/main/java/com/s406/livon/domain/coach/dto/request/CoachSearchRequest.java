package com.s406.livon.domain.coach.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코치 목록 조회 요청 파라미터 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachSearchRequest {
    
    private String job;  // 코치 직업 필터
    private OrganizationType organizationType;  // 조직 필터
    
    public enum OrganizationType {
        ALL,      // 전체
        SAME_ORG  // 같은 조직
    }
}