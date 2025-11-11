package com.s406.livon.domain.consultation.service;

import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.GroupConsultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.coach.entity.Participant;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.consultation.dto.response.CoachConsultationResponseDto;
import com.s406.livon.domain.consultation.dto.response.CoachConsultationResponseDto.CoachInfoDto;
import com.s406.livon.domain.consultation.dto.response.CoachConsultationResponseDto.ParticipantInfoDto;
import com.s406.livon.domain.consultation.repository.ConsultationRepository;
import com.s406.livon.domain.user.entity.CoachCertificates;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.global.error.handler.ConsultationHandler;
import com.s406.livon.global.web.response.PaginatedResponse;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CoachConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ParticipantRepository participantRepository;
    private final IndividualConsultationRepository individualConsultationRepository;
    private final GroupConsultationRepository groupConsultationRepository;

    public PaginatedResponse<CoachConsultationResponseDto> getCoachConsultations(
            UUID coachId,
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
                if (consultationType == Consultation.Type.BREAK) {
                    throw new ConsultationHandler(ErrorStatus._BAD_REQUEST);
                }
            } catch (IllegalArgumentException e) {
                throw new ConsultationHandler(ErrorStatus._BAD_REQUEST);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isPast = status.equals("past");

        // 1. Consultation 조회
        Page<Consultation> consultations = fetchConsultations(
                coachId, now, isPast, consultationType, pageable
        );

        // 2. Consultation ID 목록 추출
        List<Long> consultationIds = consultations.getContent().stream()
                .map(Consultation::getId)
                .collect(Collectors.toList());

        // 3. N+1 방지: 관련 데이터 일괄 조회
        Map<Long, List<Participant>> participantsMap = fetchParticipantsMap(consultationIds);
        Map<Long, IndividualConsultation> individualConsultationMap = fetchIndividualConsultationMap(consultationIds);
        Map<Long, GroupConsultation> groupConsultationMap = fetchGroupConsultationMap(consultationIds);

        // 4. DTO 변환
        return PaginatedResponse.of(consultations, consultation ->
                toCoachConsultationResponse(
                        consultation,
                        participantsMap.getOrDefault(consultation.getId(), List.of()),
                        individualConsultationMap.get(consultation.getId()),
                        groupConsultationMap.get(consultation.getId())
                )
        );
    }

    private Page<Consultation> fetchConsultations(
            UUID coachId,
            LocalDateTime now,
            boolean isPast,
            Consultation.Type consultationType,
            Pageable pageable
    ) {
        if (consultationType != null) {
            return isPast
                    ? consultationRepository.findCoachPastConsultations(coachId, now, consultationType, pageable)
                    : consultationRepository.findCoachUpcomingConsultations(coachId, now, consultationType, pageable);
        } else {
            return isPast
                    ? consultationRepository.findCoachPastConsultations(coachId, now, pageable)
                    : consultationRepository.findCoachUpcomingConsultations(coachId, now, pageable);
        }
    }

    private Map<Long, List<Participant>> fetchParticipantsMap(List<Long> consultationIds) {
        if (consultationIds.isEmpty()) {
            return Map.of();
        }

        return participantRepository.findByConsultationIdInWithUser(consultationIds).stream()
                .collect(Collectors.groupingBy(p -> p.getConsultation().getId()));
    }

    private Map<Long, IndividualConsultation> fetchIndividualConsultationMap(List<Long> consultationIds) {
        if (consultationIds.isEmpty()) {
            return Map.of();
        }

        return individualConsultationRepository.findByConsultationIdIn(consultationIds).stream()
                .collect(Collectors.toMap(ic -> ic.getConsultation().getId(), ic -> ic));
    }

    private Map<Long, GroupConsultation> fetchGroupConsultationMap(List<Long> consultationIds) {
        if (consultationIds.isEmpty()) {
            return Map.of();
        }

        return groupConsultationRepository.findByConsultationIdIn(consultationIds).stream()
                .collect(Collectors.toMap(gc -> gc.getConsultation().getId(), gc -> gc));
    }

    /**
     * Consultation을 CoachConsultationResponseDto로 변환
     */
    private CoachConsultationResponseDto toCoachConsultationResponse(
            Consultation consultation,
            List<Participant> participants,
            IndividualConsultation individualConsultation,
            GroupConsultation groupConsultation
    ) {
        User coach = consultation.getCoach();

        // 코치 정보 생성 (자격증 포함)
        CoachInfoDto coachInfo = buildCoachInfo(coach);

        // 참여자 정보 변환
        List<ParticipantInfoDto> participantInfos = participants.stream()
                .map(p -> ParticipantInfoDto.builder()
                        .userId(p.getUser().getId())
                        .nickname(p.getUser().getNickname())
                        .profileImage(p.getUser().getProfileImage())
                        .email(p.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());

        CoachConsultationResponseDto.CoachConsultationResponseDtoBuilder builder = CoachConsultationResponseDto.builder()
                .consultationId(consultation.getId())
                .type(consultation.getType().name())
                .status(consultation.getStatus().name())
                .startAt(consultation.getStartAt())
                .endAt(consultation.getEndAt())
                .sessionId(consultation.getSessionId())
                .coach(coachInfo)
                .capacity(consultation.getCapacity())
                .currentParticipants(participants.size())
                .participants(participantInfos);  // 참여자 목록 추가

        // GroupConsultation 정보 추가
        if (groupConsultation != null) {
            builder.title(groupConsultation.getTitle())
                    .description(groupConsultation.getDescription())
                    .imageUrl(groupConsultation.getImageUrl());
        }

        // IndividualConsultation 정보 추가
        if (individualConsultation != null) {
            builder.aiSummary(individualConsultation.getAiSummary())
                    .preQna(individualConsultation.getPreQnA());
        }

        return builder.build();
    }

    /**
     * 코치 정보 DTO 생성 (자격증 포함)
     */
    private CoachInfoDto buildCoachInfo(User coach) {
        // 자격증 목록 추출
        List<String> certificates = null;
        if (coach.getCoachInfo() != null && coach.getCoachInfo().getCoachCertificatesList() != null) {
            certificates = coach.getCoachInfo().getCoachCertificatesList().stream()
                    .map(CoachCertificates::getCertificatesName)
                    .collect(Collectors.toList());
        }

        // 조직 정보 추출
        String organizations = null;
        if (coach.getOrganizations() != null) {
            organizations = coach.getOrganizations().getName();
        }

        return CoachInfoDto.builder()
                .userId(coach.getId())
                .nickname(coach.getNickname())
                .job(coach.getCoachInfo() != null ? coach.getCoachInfo().getJob() : null)
                .introduce(coach.getCoachInfo() != null ? coach.getCoachInfo().getIntroduce() : null)
                .profileImage(coach.getProfileImage())
                .certificates(certificates)  // 자격증 목록
                .organizations(organizations)
                .build();
    }
}