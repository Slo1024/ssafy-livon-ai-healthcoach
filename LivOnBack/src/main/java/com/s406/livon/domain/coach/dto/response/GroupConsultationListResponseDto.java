// GroupConsultationListResponse.java
package com.s406.livon.domain.coach.dto.response;

import com.s406.livon.domain.coach.entity.GroupConsultation;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record GroupConsultationListResponseDto(
        Long id,
        String title,
        String imageUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer capacity,
        Long currentParticipants,
        Integer availableSeats,
        boolean isFull,
        String coachName,
        String coachProfileImage
) {
    public static GroupConsultationListResponseDto from(
            GroupConsultation gc, 
            Long currentParticipants) {
        
        int availableSeats = Math.max(0, 
                gc.getConsultation().getCapacity() - currentParticipants.intValue());
        
        return GroupConsultationListResponseDto.builder()
                .id(gc.getId())
                .title(gc.getTitle())
                .imageUrl(gc.getImageUrl())
                .startAt(gc.getConsultation().getStartAt())
                .endAt(gc.getConsultation().getEndAt())
                .capacity(gc.getConsultation().getCapacity())
                .currentParticipants(currentParticipants)
                .availableSeats(availableSeats)
                .isFull(availableSeats == 0)
                .coachName(gc.getConsultation().getCoach().getNickname())
                .coachProfileImage(gc.getConsultation().getCoach().getProfileImage())
                .build();
    }
}