package com.s406.livon.domain.consultation.service;

import com.s406.livon.domain.coach.dto.response.CoachDetailResponseDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.GroupConsultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.domain.consultation.dto.response.MyReservationResponseDto;
import com.s406.livon.domain.consultation.repository.ConsultationRepository;
import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.global.error.handler.ConsultationHandler;
import com.s406.livon.global.web.response.PaginatedResponse;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final IndividualConsultationRepository individualConsultationRepository;
    private final GroupConsultationRepository groupConsultationRepository;

    public PaginatedResponse<MyReservationResponseDto> getMyReservations(
            UUID userId,
            String status,
            String type,
            Pageable pageable
    ) {
        // status 검증
        if (!status.equals("upcoming") && !status.equals("past")) {
            throw new ConsultationHandler(ErrorStatus._BAD_REQUEST);
        }

        // type 검증 (선택 사항)
        Consultation.Type consultationType = null;
        if (type != null) {
            try {
                consultationType = Consultation.Type.valueOf(type.toUpperCase());
                // BREAK 타입은 조회 불가
                if (consultationType == Consultation.Type.BREAK) {
                    throw new ConsultationHandler(ErrorStatus._BAD_REQUEST);
                }
            } catch (IllegalArgumentException e) {
                throw new ConsultationHandler(ErrorStatus._BAD_REQUEST);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isPast = status.equals("past");

        Page<Consultation> consultations;

        if (consultationType != null) {
            // type 필터링 O
            consultations = isPast
                    ? consultationRepository.findPastReservations(userId, now, consultationType, pageable)
                    : consultationRepository.findUpcomingReservations(userId, now, consultationType, pageable);
        } else {
            // type 필터링 X (전체 조회)
            consultations = isPast
                    ? consultationRepository.findPastReservations(userId, now, pageable)
                    : consultationRepository.findUpcomingReservations(userId, now, pageable);
        }

        // N+1 방지: 한번에 IndividualConsultation, GroupConsultation 조회
        List<Long> consultationIds = consultations.getContent().stream()
                .map(Consultation::getId)
                .collect(Collectors.toList());

        Map<Long, IndividualConsultation> individualConsultationMap =
                individualConsultationRepository.findByConsultationIdIn(consultationIds).stream()
                        .collect(Collectors.toMap(ic -> ic.getConsultation().getId(), ic -> ic));

        Map<Long, GroupConsultation> groupConsultationMap =
                groupConsultationRepository.findByConsultationIdIn(consultationIds).stream()
                        .collect(Collectors.toMap(gc -> gc.getConsultation().getId(), gc -> gc));

        // DTO 변환
        return PaginatedResponse.of(consultations, consultation ->
                toMyReservationResponse(consultation, individualConsultationMap, groupConsultationMap));
    }

    private MyReservationResponseDto toMyReservationResponse(
            Consultation consultation,
            Map<Long, IndividualConsultation> individualConsultationMap,
            Map<Long, GroupConsultation> groupConsultationMap
    ) {
        if (consultation.getType() == Consultation.Type.ONE) {
            IndividualConsultation individualConsultation = individualConsultationMap.get(consultation.getId());
            return buildOneToOneResponse(consultation, individualConsultation);
        } else {
            GroupConsultation groupConsultation = groupConsultationMap.get(consultation.getId());
            return buildGroupResponse(consultation, groupConsultation);
        }
    }

    private MyReservationResponseDto buildOneToOneResponse(
            Consultation consultation,
            IndividualConsultation individualConsultation
    ) {
        User coach = consultation.getCoach();
        CoachDetailResponseDto coachDetail = buildCoachDetailResponse(coach);

        String preQna = null;
        String aiSummary = null;

        if (individualConsultation != null) {
            preQna = individualConsultation.getPreQnA();
            aiSummary = individualConsultation.getAiSummary();
        }

        return MyReservationResponseDto.builder()
                .consultationId(consultation.getId())
                .type(consultation.getType())
                .startAt(consultation.getStartAt())
                .endAt(consultation.getEndAt())
                .sessionId(consultation.getSessionId())
                .status(consultation.getStatus())
                .coach(coachDetail)
                .preQna(preQna)
                .aiSummary(aiSummary)
                .build();
    }

    private MyReservationResponseDto buildGroupResponse(
            Consultation consultation,
            GroupConsultation groupConsultation
    ) {
        String title = null;
        String description = null;
        String imageUrl = null;

        if (groupConsultation != null) {
            title = groupConsultation.getTitle();
            description = groupConsultation.getDescription();
            imageUrl = groupConsultation.getImageUrl();
        }

        // 코치 정보 추가
        User coach = consultation.getCoach();
        CoachDetailResponseDto coachDetail = buildCoachDetailResponse(coach);

        // 참가자 수 조회
        int currentParticipants = consultationRepository.countParticipantsByConsultationId(consultation.getId());

        return MyReservationResponseDto.builder()
                .consultationId(consultation.getId())
                .type(consultation.getType())
                .startAt(consultation.getStartAt())
                .endAt(consultation.getEndAt())
                .sessionId(consultation.getSessionId())
                .status(consultation.getStatus())
                .coach(coachDetail)  // 코치 정보 추가
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .capacity(consultation.getCapacity())
                .currentParticipants(currentParticipants)
                .build();
    }

    private CoachDetailResponseDto buildCoachDetailResponse(User coach) {
        CoachInfo coachInfo = coach.getCoachInfo();

        return CoachDetailResponseDto.builder()
                .userId(coach.getId())
                .nickname(coach.getNickname())
                .profileImage(coach.getProfileImage())
                .job(coachInfo != null ? coachInfo.getJob() : null)
                .introduce(coachInfo != null ? coachInfo.getIntroduce() : null)
                .organizations(coach.getOrganizations() != null ? coach.getOrganizations().getName() : null)
                .build();
    }
}