package com.s406.livon.domain.coach.service;

import com.s406.livon.domain.coach.dto.request.IndivualConsultationReservationRequestDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.global.error.handler.CoachHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IndividualConsultationService {

    private final GroupConsultationService groupConsultationService;
    private final GroupConsultationRepository groupConsultationRepository;


    public Long reserveConsultation(UUID userId, IndivualConsultationReservationRequestDto requestDto) {

        //1. 코치 검증
        UUID coachId = requestDto.coachId();
        User coach = groupConsultationService.validateCoach(coachId);

        // 2. 시간 겹침 검증 (DB 레벨)
        if (groupConsultationRepository.existsTimeConflict(
                coachId, requestDto.startAt(), requestDto.endAt())) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_TIME_CONFLICT);
        }

        // 3. Consultation 생성
        Consultation consultation = Consultation.builder()
                .coach(coach)
                .capacity(1)
                .startAt(requestDto.startAt())
                .endAt(requestDto.endAt())
                .type(Consultation.Type.ONE)
                .sessionId("")  // WebRTC 세션 생성 시 업데이트
                .status(Consultation.Status.OPEN)
                .build();

        // 4. IndividualConsultation 생성

        

        return;
    }
}
