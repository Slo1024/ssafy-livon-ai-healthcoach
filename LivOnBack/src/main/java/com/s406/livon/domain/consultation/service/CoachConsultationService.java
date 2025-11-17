package com.s406.livon.domain.consultation.service;

import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.GroupConsultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.coach.entity.Participant;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.consultation.dto.response.CoachConsultationResponseDto;
import com.s406.livon.domain.consultation.dto.response.CoachConsultationResponseDto.ParticipantInfoDto;
import com.s406.livon.domain.consultation.dto.response.ParticipantInfoResponseDto;
import com.s406.livon.domain.consultation.repository.ConsultationRepository;
import com.s406.livon.domain.ai.gms.service.AiAnalysisService;
import com.s406.livon.domain.ai.gms.dto.response.AiSummaryResponseDto;
import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.error.handler.ConsultationHandler;
import com.s406.livon.global.web.response.PaginatedResponse;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
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
    private final HealthSurveyRepository healthSurveyRepository;
    private final AiAnalysisService aiAnalysisService;

    @Transactional(readOnly = true)
    public PaginatedResponse<CoachConsultationResponseDto> getCoachConsultations(
            UUID coachId,
            String status,
            String type,
            Pageable pageable
    ) {
        log.info("=== Step 1: 검증 시작 ===");
        // status 검증
        if (!status.equals("upcoming") && !status.equals("past")) {
            throw new ConsultationHandler(ErrorStatus._BAD_REQUEST);
        }

        // type 검증
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

        log.info("=== Step 2: Consultation 조회 시작 ===");
        Page<Consultation> consultations = fetchConsultationsWithDetails(
                coachId, now, isPast, consultationType, pageable
        );
        log.info("=== Step 2 완료: {} 건 조회 ===", consultations.getTotalElements());

        log.info("=== Step 3: ID 목록 추출 ===");
        List<Long> consultationIds = consultations.getContent().stream()
                .map(Consultation::getId)
                .collect(Collectors.toList());
        log.info("=== Step 3 완료: ID 목록 = {} ===", consultationIds);

        log.info("=== Step 4: 관련 데이터 일괄 조회 시작 ===");
        Map<Long, List<Participant>> participantsMap = fetchParticipantsMap(consultationIds);
        log.info("=== Step 4-1 완료: Participants ===");

        Map<Long, IndividualConsultation> individualConsultationMap = fetchIndividualConsultationMap(consultationIds);
        log.info("=== Step 4-2 완료: IndividualConsultation ===");

        Map<Long, GroupConsultation> groupConsultationMap = fetchGroupConsultationMap(consultationIds);
        log.info("=== Step 4-3 완료: GroupConsultation ===");

        log.info("=== Step 5: DTO 변환 시작 ===");
        PaginatedResponse<CoachConsultationResponseDto> result = PaginatedResponse.of(consultations, consultation ->
                toCoachConsultationResponse(
                        consultation,
                        participantsMap.getOrDefault(consultation.getId(), List.of()),
                        individualConsultationMap.get(consultation.getId()),
                        groupConsultationMap.get(consultation.getId())
                )
        );
        log.info("=== Step 5 완료: DTO 변환 완료 ===");

        return result;
    }

    /**
     * Consultation 조회 (코치 정보와 자격증 fetch join 포함)
     */
    private Page<Consultation> fetchConsultationsWithDetails(
            UUID coachId,
            LocalDateTime now,
            boolean isPast,
            Consultation.Type consultationType,
            Pageable pageable
    ) {
        if (consultationType != null) {
            return isPast
                    ? consultationRepository.findCoachPastConsultationsWithDetails(coachId, now, consultationType, pageable)
                    : consultationRepository.findCoachUpcomingConsultationsWithDetails(coachId, now, consultationType, pageable);
        } else {
            return isPast
                    ? consultationRepository.findCoachPastConsultationsWithDetails(coachId, now, pageable)
                    : consultationRepository.findCoachUpcomingConsultationsWithDetails(coachId, now, pageable);
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
                .capacity(consultation.getCapacity())
                .currentParticipants(participants.size())
                .participants(participantInfos);  // 참여자 목록 추가 (빈 리스트라도 포함)

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

    @Transactional(readOnly = true)
    public List<ParticipantInfoResponseDto> getParticipantInfo(Long consultationId, UUID coachId) {
        // 1. consultation 조회 및 검증
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CONSULTATION_NOT_FOUND));

        // 2. 코치 권한 확인
        if (!consultation.getCoach().getId().equals(coachId)) {
            throw new GeneralException(ErrorStatus.CONSULTATION_NOT_COACH_AUTHORITY);
        }

        // 3. 상담 상태 확인 (OPEN인 경우만 조회 가능)
        if (consultation.getStatus() != Consultation.Status.OPEN) {
            throw new GeneralException(ErrorStatus.CONSULTATION_ALREADY_CLOSED);
        }

        // 4. 참여자 정보 조회
        List<Participant> participants = participantRepository.findParticipantByConsultationId(consultationId);
        if (participants.isEmpty()) {
            throw new GeneralException(ErrorStatus.CONSULTATION_NOT_FOUND);
        }

        // 5. 각 참여자에 대해 정보 조회 및 DTO 생성
        return participants.stream()
                .map(participant -> {
                    User user = participant.getUser();

                    // 건강 설문 데이터 조회
                    HealthSurvey healthSurvey = healthSurveyRepository.findByUserId(user.getId())
                            .orElse(null);

                    // AI 분석 요약 조회 - 별도 메서드로 분리
                    String aiSummary = getAiSummaryWithFallback(user.getId());

                    // 응답 DTO 생성
                    return buildParticipantInfoResponse(user, healthSurvey, aiSummary);
                })
                .collect(Collectors.toList());
    }

    /**
     * AI 요약 조회 (예외 발생 시 null 반환)
     */
    public String getAiSummaryWithFallback(UUID userId) {
        try {
            AiSummaryResponseDto aiSummaryResponse = aiAnalysisService.getSummary(userId);
            return aiSummaryResponse.getSummary();
        } catch (Exception e) {
            log.warn("AI 분석 요약 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 참여자 정보 응답 DTO 생성
     */
    private ParticipantInfoResponseDto buildParticipantInfoResponse(User participant, HealthSurvey healthSurvey, String aiSummary) {
        // 연령대 계산
        String ageGroup = calculateAgeGroup(participant.getBirthdate());

        // 건강 데이터 빌드 (null인 경우 빈 값으로 처리)
        ParticipantInfoResponseDto.HealthData healthData = ParticipantInfoResponseDto.HealthData.builder()
                        .height(healthSurvey != null ? healthSurvey.getHeight() : null)
                        .weight(healthSurvey != null ? healthSurvey.getWeight() : null)
                        .steps(healthSurvey != null ? healthSurvey.getSteps() : null)
                        .sleepTime(healthSurvey != null ? healthSurvey.getSleepTime() : null)
                        .activityLevel(healthSurvey != null ? healthSurvey.getActivityLevel() : null)
                        .sleepQuality(healthSurvey != null ? healthSurvey.getSleepQuality() : null)
                        .stressLevel(healthSurvey != null ? healthSurvey.getStressLevel() : null)
                        .build();

        // 회원 정보 빌드
        ParticipantInfoResponseDto.MemberInfo memberInfo = ParticipantInfoResponseDto.MemberInfo.builder()
                        .nickname(participant.getNickname())
                        .gender(participant.getGender().name())
                        .ageGroup(ageGroup)
                        .healthData(healthData)
                        .build();

        return ParticipantInfoResponseDto.builder()
                        .memberInfo(memberInfo)
                        .aiSummary(aiSummary)
                        .build();
    }

    /**
     * 생년월일로부터 연령대 계산
     */
    private String calculateAgeGroup(LocalDate birthDate) {
        if (birthDate == null) {
            return "정보 없음";
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        int decade = (age / 10) * 10;
        int remainder = age % 10;

        String period;
        if (remainder <= 3) {
            period = "초반";
        } else if (remainder <= 6) {
            period = "중반";
        } else {
            period = "후반";
        }

        return decade + "대 " + period;
    }
}
