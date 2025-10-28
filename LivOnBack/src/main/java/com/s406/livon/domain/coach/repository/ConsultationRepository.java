package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 상담 예약 레포지토리
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    
    /**
     * 특정 코치의 특정 날짜 예약 조회
     */
    @Query("SELECT c FROM Consultation c " +
           "WHERE c.userId = :coachId " +
           "AND c.startAt >= :startOfDay " +
           "AND c.startAt < :endOfDay")
    List<Consultation> findByCoachIdAndDate(@Param("coachId") UUID coachId,
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 특정 코치의 특정 날짜 예약을 막아놓은 시간대 조회
     */
    @Query("SELECT c FROM Consultation c " +
            "WHERE c.userId = :coachId " +
            "AND c.startAt >= :startOfDay " +
            "AND c.startAt < :endOfDay " +
            "AND c.type = 'BREAK'")
    List<Consultation> findBlockedTimesByCoachIdAndDate(@Param("coachId") UUID coachId,
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);
}