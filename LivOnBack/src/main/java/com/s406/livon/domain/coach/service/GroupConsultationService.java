package com.s406.livon.domain.coach.service;

import com.s406.livon.domain.coach.dto.request.GroupConsultationCreateRequestDto;
import com.s406.livon.domain.coach.dto.request.GroupConsultationUpdateRequestDto;
import com.s406.livon.domain.coach.dto.response.GroupConsultationDetailResponseDto;
import com.s406.livon.domain.coach.dto.response.GroupConsultationListResponseDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.GroupConsultation;
import com.s406.livon.domain.coach.entity.Participant;
import com.s406.livon.domain.coach.repository.ConsultationRepository;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.aop.DistributedLock;
import com.s406.livon.global.error.handler.CoachHandler;
import com.s406.livon.global.s3.S3Service;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import com.s406.livon.global.web.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 그룹 상담(클래스) 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupConsultationService {
    
    private final GroupConsultationRepository groupConsultationRepository;
    private final ConsultationRepository consultationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    
    /**
     * 클래스 생성
     * 
     * @param coachId 코치 ID
     * @param request 클래스 생성 요청
     * @return 생성된 클래스 ID
     */
    @Transactional
    public Long createGroupConsultation(UUID coachId, GroupConsultationCreateRequestDto request, MultipartFile classImage) {
        // 1. 코치 권한 확인
        User coach = validateCoach(coachId);

        // 2. 시간 겹침 검증 (DB 레벨)
        if (groupConsultationRepository.existsTimeConflict(
                coachId, request.startAt(), request.endAt())) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_TIME_CONFLICT);
        }

        // 3. Consultation 생성
        Consultation consultation = Consultation.builder()
                .coach(coach)
                .capacity(request.capacity())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .type(Consultation.Type.GROUP)
                .sessionId("")  // WebRTC 세션 생성 시 업데이트
                .status(Consultation.Status.OPEN)
                .build();
        
        Consultation savedConsultation = consultationRepository.save(consultation);
        
        // 4. 클래스 이미지 저장
        String Url = "";
        if (classImage != null && !classImage.isEmpty()) {
            System.out.println("프로필 이미지 저장");
            Url = s3Service.uploadProfileImage(classImage);
        }
        System.out.println("Url = "+ Url);
        
        // 5. GroupConsultation 생성
        GroupConsultation groupConsultation = GroupConsultation.builder()
                .consultation(savedConsultation)
                .title(request.title())
                .description(request.description())
                .imageUrl(Url)
                .build();
        
        GroupConsultation saved = groupConsultationRepository.save(groupConsultation);
        
        return saved.getId();
    }
    
    /**
     * 클래스 목록 조회 (조직별/전체)
     * 
     * @param userId 요청한 사용자 ID
     * @param sameOrganization 같은 소속만 조회 여부
     * @param pageable 페이징 정보
     * @return 클래스 목록
     */
    public PaginatedResponse<GroupConsultationListResponseDto> getGroupConsultations(
            UUID userId,
            boolean sameOrganization, 
            Pageable pageable) {
        
        Page<Object[]> result;
        
        if (sameOrganization) {
            // 같은 소속 코치의 클래스만 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CoachHandler(ErrorStatus.USER_NOT_FOUND));
            
            Long orgId = user.getOrganizations().getId();
            result = groupConsultationRepository.findByOrganizationWithParticipantCount(pageable, orgId);
        } else {
            // 전체 클래스 조회
            result = groupConsultationRepository.findAllWithParticipantCount(pageable);
        }
        
        // Object[] -> DTO 변환
        Page<GroupConsultationListResponseDto> dtoPage = result.map(objects -> {
            GroupConsultation gc = (GroupConsultation) objects[0];
            Long currentParticipants = (Long) objects[1];
            
            return GroupConsultationListResponseDto.from(gc, currentParticipants);
        });
        
        return PaginatedResponse.of(dtoPage);
    }
    
    /**
     * 클래스 상세 조회
     * 
     * @param id 클래스 ID
     * @return 클래스 상세 정보
     */
    public GroupConsultationDetailResponseDto getGroupConsultation(Long id) {
        List<Object[]> results = groupConsultationRepository.findByIdWithParticipantCount(id);

        if (results.isEmpty()) {
            throw new CoachHandler(ErrorStatus._BAD_REQUEST);
        }

        Object[] result = results.get(0);  // 첫 번째 행 가져오기
        GroupConsultation gc = (GroupConsultation) result[0];
        Long currentParticipants = (Long) result[1];

        return GroupConsultationDetailResponseDto.from(gc, currentParticipants);
    }

    /**
     * 클래스 수정
     *
     * @param coachId 코치 ID
     * @param id 클래스 ID
     * @param request 수정 요청
     */
    @Transactional
    public void updateGroupConsultation(UUID coachId, Long id, GroupConsultationUpdateRequestDto request) {
        // 1. 클래스 조회
        GroupConsultation groupConsultation = groupConsultationRepository.findById(id)
                .orElseThrow(() -> new CoachHandler(ErrorStatus._BAD_REQUEST));

        // 2. 권한 확인 (본인이 만든 클래스인지)
        validateOwnership(coachId, groupConsultation);

        // 3. 예약자가 있는지 확인
        long participantCount = participantRepository.countByConsultationId(id);
        if (participantCount > 0) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_HAS_PARTICIPANTS);
        }

        // 4. GroupConsultation 수정 (optional 필드만)
        if (request.title() != null || request.description() != null || request.imageUrl() != null) {
            groupConsultation.updateDetails(
                    request.title() != null ? request.title() : groupConsultation.getTitle(),
                    request.description() != null ? request.description() : groupConsultation.getDescription(),
                    request.imageUrl() != null ? request.imageUrl() : groupConsultation.getImageUrl()
            );
        }

        // 5. Consultation 수정 (optional 필드만)
        Consultation consultation = groupConsultation.getConsultation();
        if (request.startAt() != null || request.endAt() != null || request.capacity() != null) {
            consultation.updateDetails(
                    request.startAt() != null ? request.startAt() : consultation.getStartAt(),
                    request.endAt() != null ? request.endAt() : consultation.getEndAt(),
                    request.capacity() != null ? request.capacity() : consultation.getCapacity()
            );
        }

        // 6. 변경 사항 저장 (JPA dirty checking으로 자동 저장됨)
    }
    
    /**
     * 클래스 삭제 (Soft Delete)
     * 
     * @param coachId 코치 ID
     * @param id 클래스 ID
     */
    @Transactional
    public void deleteGroupConsultation(UUID coachId, Long id) {
        // 1. 클래스 조회
        GroupConsultation groupConsultation = groupConsultationRepository.findById(id)
                .orElseThrow(() -> new CoachHandler(ErrorStatus._BAD_REQUEST));
        
        // 2. 권한 확인 (본인이 만든 클래스인지)
        validateOwnership(coachId, groupConsultation);
        
        // 3. Soft Delete: status를 CANCELLED로 변경
        Consultation consultation = groupConsultation.getConsultation();
        consultation.cancel();
    }

    /**
     * 클래스 예약하기
     */
    @DistributedLock(
            key = "'consultation:' + #classId",
            waitTime = 3,
            leaseTime = 5,
            timeUnit = TimeUnit.SECONDS
    )
    public Long reserveGroupConsultation(UUID userId, Long classId) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 클래스 존재 여부 확인 및 조회
        Consultation consultation = consultationRepository.findById(classId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.CONSULTATION_NOT_FOUND));

        // 3. 클래스 타입 검증 (GROUP 타입인지 확인)
        if (consultation.getType() != Consultation.Type.GROUP) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_TYPE_MISMATCH);
        }

        // 4. 클래스 상태 검증 (OPEN 상태인지 확인)
        if (consultation.getStatus() != Consultation.Status.OPEN) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_ALREADY_CLOSED);
        }

        // 5. 현재 참가 인원 확인 및 정원 검증
        Long currentParticipants = participantRepository.countByConsultationId(classId);
        if (currentParticipants >= consultation.getCapacity()) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_CAPACITY_FULL);
        }

        // 6. 중복 예약 방지 (이미 예약한 사용자인지 확인)
        if (participantRepository.existsByUserIdAndConsultationId(userId, classId)) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_ALREADY_RESERVED);
        }

        // 7. Participant 생성 및 저장
        Participant participant = Participant.of(user, consultation);
        participantRepository.save(participant);

        return consultation.getId();
    }

    /**
     * 클래스 참여 취소
     *
     * @param userId 취소하려는 사용자 ID
     * @param consultationId 취소하려는 클래스 ID
     */
    @Transactional
    public void cancelParticipation(UUID userId, Long consultationId) {
        // 1. 참가자 정보 조회
        Participant participant = participantRepository
                .findByUserIdAndConsultationId(userId, consultationId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.PARTICIPANT_NOT_FOUND));

        Consultation consultation = participant.getConsultation();

        // 2. 이미 취소된 클래스인지 확인
        if (consultation.getStatus() == Consultation.Status.CANCELLED) {
            throw new CoachHandler(ErrorStatus.PARTICIPANT_ALREADY_CANCELLED);
        }

        // 3. 당일 취소 불가 검증
        LocalDate today = LocalDate.now();
        LocalDate consultationDate = consultation.getStartAt().toLocalDate();

        if (consultationDate.equals(today)) {
            throw new CoachHandler(ErrorStatus.PARTICIPANT_CANCEL_SAME_DAY);
        }

        // 4. 이미 시작된 클래스인지 확인
        if (consultation.getStartAt().isBefore(LocalDateTime.now())) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_ALREADY_STARTED);
        }

        // 5. 참가자 삭제
        participantRepository.delete(participant);

        log.info("클래스 참여 취소 완료 - userId: {}, consultationId: {}", userId, consultationId);
    }
    
    // === Private Helper Methods ===
    
    /**
     * 코치 권한 검증
     */
    protected User validateCoach(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.USER_NOT_FOUND));
        
        if (!user.getRoles().contains(Role.COACH)) {
            throw new CoachHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        
        return user;
    }
    
    /**
     * 소유권 검증 (본인이 만든 클래스인지)
     */
    private void validateOwnership(UUID coachId, GroupConsultation groupConsultation) {
        if (!groupConsultation.getConsultation().getCoach().getId().equals(coachId)) {
            throw new CoachHandler(ErrorStatus.USER_NOT_MATCH);
        }
    }
}