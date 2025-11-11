package com.s406.livon.domain.consultation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CoachConsultationResponseDto {

    private Long consultationId;
    private String type;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String sessionId;

    // 1:1 상담 추가 정보
    private String preQna;
    private String aiSummary;

    // GROUP 상담 정보
    private String title;
    private String description;
    private String imageUrl;

    // 참여자 정보
    private int capacity;
    private int currentParticipants;
    private List<ParticipantInfoDto> participants;  // 참여자 상세 목록

    @Getter
    @Builder
    public static class ParticipantInfoDto {
        private UUID userId;
        private String nickname;
        private String profileImage;
        private String email;
    }
}