// GroupConsultationDetailResponse.java
package com.s406.livon.domain.coach.dto.response;

import com.s406.livon.domain.coach.entity.GroupConsultation;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record GroupConsultationDetailResponseDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer capacity,
        Long currentParticipants,
        Integer availableSeats,
        boolean isFull,
        CoachInfo coach
) {
    @Builder
    public record CoachInfo(
            UUID id,
            String nickname,
            String profileImage,
            String job,
            String introduce,
            String professional
    ) {}
    
    public static GroupConsultationDetailResponseDto from(
            GroupConsultation gc, 
            Long currentParticipants) {
        
        int availableSeats = Math.max(0, 
                gc.getConsultation().getCapacity() - currentParticipants.intValue());
        
        return GroupConsultationDetailResponseDto.builder()
                .id(gc.getId())
                .title(gc.getTitle())
                .description(gc.getDescription())
                .imageUrl(gc.getImageUrl())
                .startAt(gc.getConsultation().getStartAt())
                .endAt(gc.getConsultation().getEndAt())
                .capacity(gc.getConsultation().getCapacity())
                .currentParticipants(currentParticipants)
                .availableSeats(availableSeats)
                .isFull(availableSeats == 0)
                .coach(CoachInfo.builder()
                        .id(gc.getConsultation().getCoach().getId())
                        .nickname(gc.getConsultation().getCoach().getNickname())
                        .profileImage(gc.getConsultation().getCoach().getProfileImage())
                        // TODO: CoachInfo 조회 추가
                        .build())
                .build();
    }
}