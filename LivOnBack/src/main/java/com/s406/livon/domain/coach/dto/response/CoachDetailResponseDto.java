package com.s406.livon.domain.coach.dto.response;

import com.s406.livon.domain.user.entity.Organizations;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 코치 상세 정보 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachDetailResponseDto {
    
    private UUID userId;
    private String nickname;
    private String job;
    private String introduce;
    private String profileImage;
    private List<String> certificates;
    private String organizations;
    
    public static CoachDetailResponseDto toDTO(UUID userId, String nickname, String job,
                                            String introduce, String profileImage, String organizations, List<String> certificates) {
        return CoachDetailResponseDto.builder()
                .userId(userId)
                .nickname(nickname)
                .job(job)
                .introduce(introduce)
                .profileImage(profileImage)
                .organizations(organizations)
                .certificates(certificates)
                .build();
    }
}