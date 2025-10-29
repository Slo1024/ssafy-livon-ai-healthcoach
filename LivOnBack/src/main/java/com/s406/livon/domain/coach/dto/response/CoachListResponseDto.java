package com.s406.livon.domain.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 코치 목록 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachListResponseDto {
    
    private UUID userId;
    private String nickname;
    private String job;
    private String introduce;
    private String profileImage;
    
    public static CoachListResponseDto toDTO(UUID userId, String nickname, String job,
                                             String introduce, String profileImage) {
        return CoachListResponseDto.builder()
                .userId(userId)
                .nickname(nickname)
                .job(job)
                .introduce(introduce)
                .profileImage(profileImage)
                .build();
    }
}