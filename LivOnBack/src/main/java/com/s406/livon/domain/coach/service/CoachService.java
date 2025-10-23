package com.s406.livon.domain.coach.service;

import com.s406.livon.domain.coach.dto.request.CoachSearchRequest;
import com.s406.livon.domain.coach.dto.response.AvailableTimesResponse;
import com.s406.livon.domain.coach.dto.response.CoachDetailResponse;
import com.s406.livon.domain.coach.dto.response.CoachListResponse;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.CoachRepository;
import com.s406.livon.domain.coach.repository.ConsultationRepository;
import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.CoachCertificatesRepository;
import com.s406.livon.domain.user.repository.CoachInfoRepository;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 코치 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachInfoRepository coachInfoRepository;
    private final CoachCertificatesRepository coachCertificateRepository;
    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;

    // 기본 근무 시간대 (9시~18시, 1시간 단위)
    private static final List<String> DEFAULT_TIME_SLOTS = List.of(
            "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "13:00-14:00", "14:00-15:00", "15:00-16:00",
            "16:00-17:00", "17:00-18:00"
    );

    /**
     * 코치 목록 조회
     */
    public Page<CoachListResponse> getCoachList(UUID currentUserId,
                                                CoachSearchRequest request,
                                                int page,
                                                int size) {
        // 현재 사용자 정보 조회 (조직 정보 필요)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 페이징 설정 (닉네임 기준 오름차순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("nickname").ascending());

        Page<User> coaches;

        // 조직 필터에 따른 조회
        if (request.getOrganizationType() == CoachSearchRequest.OrganizationType.SAME_ORG) {
            coaches = coachRepository.findCoachesByOrganization(
                    Role.COACH,  // Role enum 전달
                    currentUser.getOrganizations(),
                    request.getJob(),
                    pageable
            );
        } else {
            coaches = coachRepository.findCoaches(
                    Role.COACH,  // Role enum 전달
                    request.getJob(),
                    pageable
            );
        }

        // DTO 변환
        return coaches.map(coach -> {
            CoachInfo coachInfo = coachInfoRepository.findByUserId(coach.getId())
                    .orElse(null);

            return CoachListResponse.of(
                    coach.getId(),
                    coach.getNickname(),
                    coachInfo != null ? coachInfo.getJob() : null,
                    coachInfo != null ? coachInfo.getIntroduce() : null,
                    coach.getProfileImage()
            );
        });
    }

    /**
     * 코치 상세 정보 조회
     */
    public CoachDetailResponse getCoachDetail(UUID coachId) {
        // 코치 조회
        User coach = userRepository.findByIdAndRole(coachId, Role.COACH)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 코치가 아닌 경우 예외 처리
        if (!coach.isCoach()) {
            throw new GeneralException(ErrorStatus.USER_NOT_AUTHORITY);
        }

        // 코치 정보 조회
        CoachInfo coachInfo = coachInfoRepository.findByUserId(coachId)
                .orElse(null);

        // 자격증 목록 조회
        List<String> certificates = coachCertificateRepository
                .findCertificateNamesByUserId(coachId);

        return CoachDetailResponse.of(
                coach.getId(),
                coach.getNickname(),
                coachInfo != null ? coachInfo.getJob() : null,
                coachInfo != null ? coachInfo.getIntroduce() : null,
                coach.getProfileImage(),
                coachInfo != null ? coachInfo.getProfessional() : null,
                certificates
        );
    }

    /**
     * 코치 예약 가능 시간대 조회
     */
    public AvailableTimesResponse getAvailableTimes(UUID coachId, String dateStr) {
        // 날짜 유효성 검증
        LocalDate requestDate = validateAndParseDate(dateStr);

        // 코치 존재 여부 확인
        User coach = userRepository.findByIdAndRole(coachId, Role.COACH)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (!coach.isCoach()) {
            throw new GeneralException(ErrorStatus.USER_NOT_AUTHORITY);
        }

        // 해당 날짜의 예약 조회
        LocalDateTime startOfDay = requestDate.atStartOfDay();
        LocalDateTime endOfDay = requestDate.atTime(LocalTime.MAX);

        List<Consultation> consultations = consultationRepository
                .findByCoachIdAndDate(coachId, startOfDay, endOfDay);

        // 예약된 시간대 추출
        List<String> bookedTimeSlots = consultations.stream()
                .map(this::convertToTimeSlot)
                .collect(Collectors.toList());

        // 예약 가능한 시간대 계산 (기본 시간대 - 예약된 시간대)
        List<String> availableTimes = DEFAULT_TIME_SLOTS.stream()
                .filter(slot -> !bookedTimeSlots.contains(slot))
                .collect(Collectors.toList());

        return AvailableTimesResponse.of(coachId, dateStr, availableTimes);
    }

    /**
     * 날짜 유효성 검증 및 파싱
     */
    private LocalDate validateAndParseDate(String dateStr) {
        LocalDate requestDate;

        try {
            requestDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(30);

        // 과거 날짜 체크
        if (requestDate.isBefore(today)) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        // 30일 이후 날짜 체크
        if (requestDate.isAfter(maxDate)) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        return requestDate;
    }

    /**
     * Consultation을 시간대 문자열로 변환
     */
    private String convertToTimeSlot(Consultation consultation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String startTime = consultation.getStartAt().format(formatter);
        String endTime = consultation.getEndAt().format(formatter);
        return startTime + "-" + endTime;
    }
}