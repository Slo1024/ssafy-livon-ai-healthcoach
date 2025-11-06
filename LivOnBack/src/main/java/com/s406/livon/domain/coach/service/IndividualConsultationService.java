package com.s406.livon.domain.coach.service;

import com.s406.livon.domain.coach.dto.request.IndivualConsultationReservationRequestDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.coach.entity.Participant;
import com.s406.livon.domain.coach.repository.ConsultationRepository;
import com.s406.livon.domain.coach.repository.GroupConsultationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.aop.DistributedLock;
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
    private final ConsultationRepository consultationRepository;
    private final IndividualConsultationRepository individualConsultationRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @DistributedLock(key = "ㅋㅋ")
    public Long reserveConsultation(UUID userId, IndivualConsultationReservationRequestDto requestDto) {

        //1. 코치 검증
        UUID coachId = requestDto.coachId();
        User coach = groupConsultationService.validateCoach(coachId);

        // 2. 시간 겹침 검증 (DB 레벨)
        if (groupConsultationRepository.existsTimeConflict(
                coachId, requestDto.startAt(), requestDto.endAt())) {
            throw new CoachHandler(ErrorStatus.CONSULTATION_TIME_CONFLICT);
        }

        // 3. Consultation 생성 및 저장
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

        // 4. participant 생성 및 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoachHandler(ErrorStatus.USER_NOT_FOUND));

        Participant participant = Participant.builder()
                .id(consultation.getId())
                .user(user)
                .build();
        participantRepository.save(participant);

        // 5. IndividualConsultation 생성 및 저장
        IndividualConsultation individualConsultation = IndividualConsultation.builder()
                .preQnA(requestDto.preQnA())
                .build();
        IndividualConsultation saved = individualConsultationRepository.save(individualConsultation);

        // 확인하기 - blocked Times 갱신이 되는가?
        // 아마 blocked Times 조회 API와 Repository를 살펴봐야 할수도

        return saved.getId();
    }
}
