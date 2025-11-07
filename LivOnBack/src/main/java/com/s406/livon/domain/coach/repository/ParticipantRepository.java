package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.Participant;
import com.s406.livon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p.user FROM Participant p WHERE p.consultation.id = :consultationId") // 'p.user' (User)를 선택
    List<User> findByConsultationId(long consultationId);
    /**
     * 특정 상담의 참가자 수 조회
     */
    long countByConsultationId(Long consultationId);
    
    /**
     * 특정 사용자가 특정 상담에 예약했는지 확인
     */
    boolean existsByUserIdAndConsultationId(UUID userId, Long consultationId);
    
    /**
     * 특정 사용자의 특정 상담 예약 조회
     */
    Optional<Participant> findByUserIdAndConsultationId(UUID userId, Long consultationId);
    
    /**
     * 특정 상담의 모든 참가자 삭제 (상담 취소 시)
     */
    void deleteByConsultationId(Long consultationId);
}