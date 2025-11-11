package com.s406.livon.domain.consultation.repository;

import com.s406.livon.domain.coach.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    // 지난 예약 조회 (type 필터링 O)
    @Query("SELECT DISTINCT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coach.organizations org " +
            "JOIN Participant p ON p.consultation.id = c.id " +
            "WHERE p.user.id = :userId " +
            "AND c.endAt < :now " +
            "AND c.type = :type " +
            "ORDER BY c.startAt DESC")
    Page<Consultation> findPastReservations(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            @Param("type") Consultation.Type type,
            Pageable pageable
    );

    // 지난 예약 조회 (type 필터링 X)
    @Query("SELECT DISTINCT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coach.organizations org " +
            "JOIN Participant p ON p.consultation.id = c.id " +
            "WHERE p.user.id = :userId " +
            "AND c.endAt < :now " +
            "AND c.type != 'BREAK' " +
            "ORDER BY c.startAt DESC")
    Page<Consultation> findPastReservations(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 앞으로 진행할 예약 조회 (type 필터링 O)
    @Query("SELECT DISTINCT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coach.organizations org " +
            "JOIN Participant p ON p.consultation.id = c.id " +
            "WHERE p.user.id = :userId " +
            "AND c.endAt >= :now " +
            "AND c.type = :type " +
            "ORDER BY c.startAt DESC")
    Page<Consultation> findUpcomingReservations(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            @Param("type") Consultation.Type type,
            Pageable pageable
    );

    // 앞으로 진행할 예약 조회 (type 필터링 X)
    @Query("SELECT DISTINCT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coach.organizations org " +
            "JOIN Participant p ON p.consultation.id = c.id " +
            "WHERE p.user.id = :userId " +
            "AND c.endAt >= :now " +
            "AND c.type != 'BREAK' " +
            "ORDER BY c.startAt DESC")
    Page<Consultation> findUpcomingReservations(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 코치의 지난 상담 조회 (type 필터링 O)
    @Query("SELECT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coachInfo.coachCertificatesList " +
            "LEFT JOIN FETCH coach.organizations org " +
            "WHERE c.coach.id = :coachId " +
            "AND c.endAt < :now " +
            "AND c.type = :type " +
            "ORDER BY c.startAt DESC")
    Page<Consultation> findCoachPastConsultations(
            @Param("coachId") UUID coachId,
            @Param("now") LocalDateTime now,
            @Param("type") Consultation.Type type,
            Pageable pageable
    );

    // 코치의 지난 상담 조회 (type 필터링 X)
    @Query("SELECT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coachInfo.coachCertificatesList " +
            "LEFT JOIN FETCH coach.organizations org " +
            "WHERE c.coach.id = :coachId " +
            "AND c.endAt < :now " +
            "AND c.type != 'BREAK' " +
            "ORDER BY c.startAt DESC")
    Page<Consultation> findCoachPastConsultations(
            @Param("coachId") UUID coachId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 코치의 예정된 상담 조회 (type 필터링 O)
    @Query("SELECT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coachInfo.coachCertificatesList " +
            "LEFT JOIN FETCH coach.organizations org " +
            "WHERE c.coach.id = :coachId " +
            "AND c.endAt >= :now " +
            "AND c.type = :type " +
            "ORDER BY c.startAt ASC")
    Page<Consultation> findCoachUpcomingConsultations(
            @Param("coachId") UUID coachId,
            @Param("now") LocalDateTime now,
            @Param("type") Consultation.Type type,
            Pageable pageable
    );

    // 코치의 예정된 상담 조회 (type 필터링 X)
    @Query("SELECT c FROM Consultation c " +
            "JOIN FETCH c.coach coach " +
            "LEFT JOIN FETCH coach.coachInfo coachInfo " +
            "LEFT JOIN FETCH coachInfo.coachCertificatesList " +
            "LEFT JOIN FETCH coach.organizations org " +
            "WHERE c.coach.id = :coachId " +
            "AND c.endAt >= :now " +
            "AND c.type != 'BREAK' " +
            "ORDER BY c.startAt ASC")
    Page<Consultation> findCoachUpcomingConsultations(
            @Param("coachId") UUID coachId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 참가자 수 조회용 쿼리 (GroupConsultation용)
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.consultation.id = :consultationId")
    int countParticipantsByConsultationId(@Param("consultationId") Long consultationId);
}