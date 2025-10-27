package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 상담 예약 레포지토리
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    
    /**
     * 특정 코치의 특정 날짜 일정 모두 조회(1:1 상담, 1:N 상담, 스스로 막아놓은 시간 모두 포함)
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

    /**
     * 특정 코치의 특정 날짜 예약 시간대 조회(전문가가 막아놓은 시간대는 포함되지 않음)
     */
    @Query("SELECT c FROM Consultation c " +
            "WHERE c.userId = :coachId " +
            "AND c.startAt >= :startOfDay " +
            "AND c.startAt < :endOfDay " +
            "AND c.type != 'BREAK'")
    List<Consultation> findReservationsByCoachIdAndDate(@Param("coachId") UUID coachId,
                                                        @Param("startOfDay") LocalDateTime startOfDay,
                                                        @Param("endOfDay") LocalDateTime endOfDay);

    /**
    * 특정 코치의 특정 날짜의 차단 시간대를 모두 삭제
    */
    @Modifying
    @Transactional
    @Query("DELETE FROM Consultation c " +
            "WHERE c.userId = :coachId " +
            "AND c.startAt >= :startOfDay " +
            "AND c.startAt < :endOfDay " +
            "AND c.type = 'BREAK'")
    void deleteAllBlockedTimesByCoachIdAndDate(@Param("coachId") UUID coachId,
                                               @Param("startOfDay") LocalDateTime startOfDay,
                                               @Param("endOfDay") LocalDateTime endOfDay);
}