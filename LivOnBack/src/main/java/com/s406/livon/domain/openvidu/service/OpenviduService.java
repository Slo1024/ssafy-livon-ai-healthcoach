package com.s406.livon.domain.openvidu.service;

import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.consultation.repository.ConsultationRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.handler.ConsultationHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OpenviduService {

    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    /**
     * LiveKit 토큰 생성
     * - Consultation 권한 검증
     * - 코치 또는 예약 참가자만 접근 가능
     */
    public String createToken(UUID userId, Long consultationId, String participantName) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ConsultationHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 상담 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationHandler(ErrorStatus._BAD_REQUEST));

        // 3. 권한 검증
        validateAccess(user, consultation);

        // 4. 세션 ID (room name) 가져오기
        String roomName = consultation.getSessionId();
        if (roomName == null || roomName.isBlank()) {
            throw new ConsultationHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }

        // 5. 참가자 이름 결정 (파라미터로 받은 이름 또는 사용자 닉네임)
        String finalParticipantName = (participantName != null && !participantName.isBlank())
                ? participantName
                : user.getNickname();

        // 6. LiveKit AccessToken 생성
        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(finalParticipantName);
        token.setIdentity(user.getId().toString()); // UUID를 identity로 사용
        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        log.info("LiveKit 토큰 생성 - userId: {}, consultationId: {}, roomName: {}, participantName: {}",
                userId, consultationId, roomName, finalParticipantName);

        return token.toJwt();
    }

    /**
     * 상담 접근 권한 검증
     * - 코치: 해당 상담의 코치여야 함
     * - 일반 사용자: 해당 상담의 참가자여야 함 (1:1 또는 GROUP)
     */
    private void validateAccess(User user, Consultation consultation) {
        // 코치인 경우: 본인이 생성한 상담인지 확인
        if (consultation.getCoach().getId().equals(user.getId())) {
            log.info("코치 권한으로 상담 접근 - userId: {}, consultationId: {}", user.getId(), consultation.getId());
            return;
        }

        // 일반 사용자인 경우: 참가자 여부 확인
        boolean isParticipant = false;

        if (consultation.getType() == Consultation.Type.ONE) {
            // 1:1 상담: IndividualConsultation의 participants를 통해 확인
            // IndividualConsultation 엔티티가 있다면 해당 레포지토리에서 조회
            // 예: isParticipant = individualConsultationRepository.existsByConsultationIdAndUserId(...)
            
            // 현재는 Participants 테이블로 확인한다고 가정 (아래 참조)
            isParticipant = checkParticipantInConsultation(user.getId(), consultation.getId());
            
        } else if (consultation.getType() == Consultation.Type.GROUP) {
            // GROUP 상담: Participants 테이블에서 확인
            isParticipant = checkParticipantInConsultation(user.getId(), consultation.getId());
        }

        if (!isParticipant) {
            log.warn("상담 접근 권한 없음 - userId: {}, consultationId: {}", user.getId(), consultation.getId());
            throw new ConsultationHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }

        log.info("참가자 권한으로 상담 접근 - userId: {}, consultationId: {}", user.getId(), consultation.getId());
    }

    /**
     * Participants 테이블에서 참가자 확인
     * (실제로는 ParticipantsRepository를 주입받아 사용)
     */
    private boolean checkParticipantInConsultation(UUID userId, Long consultationId) {
        // TODO: ParticipantsRepository 구현 후 실제 조회로 변경
        // return participantsRepository.existsByUserIdAndConsultationId(userId, consultationId);
        
        // 임시로 true 반환 (개발 단계)
        log.warn("참가자 검증 로직 미구현 - 임시로 true 반환");
        return true;
    }
}