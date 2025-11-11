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

    boolean existsByConsultationIdAndUserId(Long consultationId, UUID userId);

    /**
     * 사용자와 상담 ID로 참가자 조회
     */
    @Query("SELECT p FROM Participant p " +
            "JOIN FETCH p.consultation c " +
            "WHERE p.user.id = :userId " +
            "AND p.consultation.id = :consultationId")
    Optional<Participant> findByUserIdAndConsultationId(
            @Param("userId") Long userId,
            @Param("consultationId") Long consultationId
    );

    // N+1 방지: 여러 consultation의 참가자를 한 번에 조회
    @Query("SELECT p FROM Participant p " +
            "JOIN FETCH p.user u " +
            "WHERE p.consultation.id IN :consultationIds")
    List<Participant> findByConsultationIdInWithUser(@Param("consultationIds") List<Long> consultationIds);

    // 특정 consultation의 참가자 목록 조회
    @Query("SELECT p FROM Participant p " +
            "JOIN FETCH p.user u " +
            "WHERE p.consultation.id = :consultationId")
    List<Participant> findByConsultationIdWithUser(@Param("consultationId") Long consultationId);
}