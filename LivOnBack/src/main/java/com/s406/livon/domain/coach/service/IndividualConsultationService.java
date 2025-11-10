package com.s406.livon.domain.coach.service;

import com.s406.livon.domain.coach.dto.request.IndivualConsultationReservationRequestDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.coach.entity.Participant;
import com.s406.livon.domain.coach.repository.ConsultationReservationRepository;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.aop.DistributedLock;
import com.s406.livon.global.error.handler.CoachHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IndividualConsultationService {

    private final GroupConsultationService groupConsultationService;
    private final GroupConsultationRepository groupConsultationRepository;
    private final ConsultationReservationRepository consultationRepository;
    private final IndividualConsultationRepository individualConsultationRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @DistributedLock(
            key = "'IC:' + #requestDto.coachId + ':' + #requestDto.startAt",
            waitTime = 0,        // ✅ 대기하지 않음 (즉시 실패)
            leaseTime = 3,
            timeUnit = TimeUnit.SECONDS
    )
    public Long reserveConsultation(UUID userId, IndivualConsultationReservationRequestDto requestDto) {

        // 1. 코치 검증
        UUID coachId = requestDto.coachId();
        User coach = groupConsultationService.validateCoach(coachId);

        // 2. 시간 형식 검증 추가하기
        validateReservationTime(requestDto.startAt(), requestDto.endAt());

        // 3. 시간 겹침 검증 (DB 레벨)
        if (groupConsultationRepository.existsTimeConflict(
                coachId, requestDto.startAt(), requestDto.endAt())) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_TIME_CONFLICT);
        }

        // 4. Consultation 생성 및 저장
        Consultation consultation = Consultation.builder()
                .coach(coach)
                .capacity(1)
                .startAt(requestDto.startAt())
                .endAt(requestDto.endAt())
                .type(Consultation.Type.ONE)
                .sessionId("")  // WebRTC 세션 생성 시 업데이트
                .status(Consultation.Status.OPEN)
                .build();
        consultationRepository.save(consultation);

        // 5. participant 생성 및 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.USER_NOT_FOUND));

        Participant participant = Participant.of(user, consultation);
        participantRepository.save(participant);

        // 6. IndividualConsultation 생성 및 저장
        IndividualConsultation individualConsultation = IndividualConsultation.builder()
                .consultation(consultation)
                .preQnA(requestDto.preQnA())
                .build();
        IndividualConsultation saved = individualConsultationRepository.save(individualConsultation);

        return saved.getId();
    }


    /**
     * 개인 상담 예약 시간 형식 검증
     * - 같은 날
     * - 정시 시작/종료(분,초 0)
     * - 정확히 1시간
     * - 09:00–18:00 범위
     * - CoachService.DEFAULT_TIME_SLOTS 에 정확히 존재
     * - 과거 시간 금지
     */
    protected void validateReservationTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }

        // 과거 시간 금지
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new CoachHandler(ErrorStatus.DATE_PAST_DAYS);
        }

        // 같은 날
        if (!startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }

        // 순서 및 정확히 1시간
        if (!startAt.isBefore(endAt)) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }
        if (java.time.Duration.between(startAt, endAt).toHours() != 1) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }

        // 정시(분, 초 0)만 허용
        if (startAt.getMinute() != 0 || startAt.getSecond() != 0
                || endAt.getMinute() != 0 || endAt.getSecond() != 0) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }

        // 허용 시간 범위: 09:00–18:00
        LocalTime s = startAt.toLocalTime();
        LocalTime e = endAt.toLocalTime();
        if (s.isBefore(LocalTime.of(9, 0)) || e.isAfter(LocalTime.of(18, 0))) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }

        // CoachService.DEFAULT_TIME_SLOTS(예: "09:00-10:00")에 정확히 존재해야 함
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        String slot = startAt.format(f) + "-" + endAt.format(f);
        if (!CoachService.DEFAULT_TIME_SLOTS.contains(slot)) {
            throw new CoachHandler(ErrorStatus.DATE_FORM_ERROR);
        }
    }

    /**
     * 1:1 상담 취소
     * - 당일 취소 불가
     * - 이미 시작된 상담 취소 불가
     * - 코치 또는 예약한 사용자만 취소 가능
     */
    @Transactional
    public void cancelOneOnOneConsultation(Long consultationId, UUID userId) {
        // 1. Consultation 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.CONSULTATION_NOT_FOUND));

        // 2. 1:1 상담인지 검증
        if (consultation.getType() != Consultation.Type.ONE) {
            throw new CoachHandler(ErrorStatus._BAD_REQUEST);
        }

        // 3. 이미 취소되었는지 검증
        if (consultation.getStatus() == Consultation.Status.CANCELLED) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_ALREADY_CANCELLED);
        }

        // 4. 이미 종료되었는지 검증
        if (consultation.getStatus() == Consultation.Status.CLOSE) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_ALREADY_CLOSED);
        }

        // 5. 이미 시작되었는지 검증
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(consultation.getStartAt())) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_ALREADY_STARTED);
        }

        // 6. 당일 취소 불가 검증
        LocalDate consultationDate = consultation.getStartAt().toLocalDate();
        LocalDate today = LocalDate.now();
        if (consultationDate.equals(today)) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_CANNOT_CANCEL_SAME_DAY);
        }

        // 7. 권한 검증 (코치 또는 예약한 사용자만 가능)
        boolean isCoach = consultation.getCoach().getId().equals(userId);
        boolean isParticipant = participantRepository
                .existsByConsultationIdAndUserId(consultationId, userId);

        if (!isCoach && !isParticipant) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_NOT_PARTICIPANT);
        }

        // 8. 상담 취소 처리
        consultation.cancel();

        log.info("1:1 상담 취소 완료 - consultationId: {}, userId: {}", consultationId, userId);
    }
}
