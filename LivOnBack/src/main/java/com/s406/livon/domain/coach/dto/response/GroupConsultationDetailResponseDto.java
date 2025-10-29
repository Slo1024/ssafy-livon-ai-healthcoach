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
            String introduce
    ) {}

    public static GroupConsultationDetailResponseDto from(
            GroupConsultation gc,
            Long currentParticipants) {

        int availableSeats = Math.max(0,
                gc.getConsultation().getCapacity() - currentParticipants.intValue());

        var coach = gc.getConsultation().getCoach();
        var coachInfo = coach.getCoachInfo();  // LAZY 로딩이지만 FETCH JOIN으로 이미 로딩됨

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
                        .id(coach.getId())
                        .nickname(coach.getNickname())
                        .profileImage(coach.getProfileImage())
                        .job(coachInfo != null ? coachInfo.getJob() : null)
                        .introduce(coachInfo != null ? coachInfo.getIntroduce() : null)
                        .build())
                .build();
    }
}