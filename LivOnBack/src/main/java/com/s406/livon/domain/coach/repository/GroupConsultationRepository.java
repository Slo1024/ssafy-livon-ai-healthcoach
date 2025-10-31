package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.GroupConsultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupConsultationRepository extends JpaRepository<GroupConsultation, Long> {

    /**
     * 특정 코치의 특정 시간대에 겹치는 일정이 있는지 확인
     * - BREAK 타입: 항상 확인
     * - ONE/GROUP 타입: OPEN 상태만 확인
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Consultation c " +
            "WHERE c.coach.id = :coachId " +
            "AND c.startAt < :endAt " +
            "AND c.endAt > :startAt " +
            "AND (c.type = 'BREAK' OR c.status = 'OPEN')")
    boolean existsTimeConflict(@Param("coachId") UUID coachId,
                               @Param("startAt") LocalDateTime startAt,
                               @Param("endAt") LocalDateTime endAt);

    /**
     * 같은 소속(기업) 코치의 클래스 목록 조회 (OPEN 상태만)
     */
    @Query(
            value = """
            SELECT gc,
                   COUNT(p.id) AS currentParticipants
            FROM GroupConsultation gc
            JOIN gc.consultation c
            JOIN c.coach coach
            LEFT JOIN Participant p ON p.consultation.id = c.id
            WHERE coach.organizations.id = :orgId
              AND c.status = 'OPEN'
            GROUP BY gc.id, c.id, coach.id
            ORDER BY c.startAt ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT gc.id)
            FROM GroupConsultation gc
            JOIN gc.consultation c
            JOIN c.coach coach
            WHERE coach.organizations.id = :orgId
              AND c.status = 'OPEN'
            """
    )
    Page<Object[]> findByOrganizationWithParticipantCount(
            Pageable pageable,
            @Param("orgId") Long orgId
    );

    /**
     * 전체 클래스 목록 조회 (OPEN 상태만)
     */
    @Query(
            value = """
            SELECT gc,
                   COUNT(p.id) AS currentParticipants
            FROM GroupConsultation gc
            JOIN gc.consultation c
            JOIN c.coach coach
            LEFT JOIN Participant p ON p.consultation.id = c.id
            WHERE c.status = 'OPEN'
            GROUP BY gc.id, c.id, coach.id
            ORDER BY c.startAt ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT gc.id)
            FROM GroupConsultation gc
            JOIN gc.consultation c
            WHERE c.status = 'OPEN'
            """
    )
    Page<Object[]> findAllWithParticipantCount(Pageable pageable);

    /**
     * 특정 클래스 상세 조회 (현재 참가 인원 수 포함)
     */
    @Query("""
    SELECT gc,
           (SELECT COUNT(p.id)
            FROM Participant p 
            WHERE p.consultation.id = gc.consultation.id)
    FROM GroupConsultation gc
    JOIN FETCH gc.consultation c
    JOIN FETCH c.coach coach
    LEFT JOIN FETCH coach.coachInfo
    WHERE gc.id = :id
    """)
    List<Object[]> findByIdWithParticipantCount(@Param("id") Long id);
}
