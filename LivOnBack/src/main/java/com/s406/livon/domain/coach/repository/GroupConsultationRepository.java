package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.GroupConsultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 그룹 상담(클래스) Repository
 */
public interface GroupConsultationRepository extends JpaRepository<GroupConsultation, Long> {
    
    /**
     * 같은 소속(기업) 코치의 클래스 목록 조회 (OPEN 상태만)
     * 현재 참가 인원 수 포함
     * 
     * @param orgId 조직(기업) ID
     * @param pageable 페이징 정보
     * @return 클래스 목록 (DTO 프로젝션)
     */
    @Query("""
        SELECT gc, 
               COUNT(p.id) as currentParticipants
        FROM GroupConsultation gc
        JOIN FETCH gc.consultation c
        JOIN FETCH c.coach coach
        LEFT JOIN Participant p ON p.consultation.id = gc.id
        WHERE coach.organizations.id = :orgId
          AND c.status = 'OPEN'
        GROUP BY gc.id, c.id, coach.id
        ORDER BY c.startAt ASC
        """)
    Page<Object[]> findByOrganizationWithParticipantCount(
        Pageable pageable,
        @Param("orgId") Long orgId
    );
    
    /**
     * 전체 클래스 목록 조회 (OPEN 상태만)
     * 현재 참가 인원 수 포함
     * 
     * @param pageable 페이징 정보
     * @return 클래스 목록 (DTO 프로젝션)
     */
    @Query("""
        SELECT gc, 
               COUNT(p.id) as currentParticipants
        FROM GroupConsultation gc
        JOIN FETCH gc.consultation c
        JOIN FETCH c.coach coach
        LEFT JOIN Participant p ON p.consultation.id = gc.id
        WHERE c.status = 'OPEN'
        GROUP BY gc.id, c.id, coach.id
        ORDER BY c.startAt ASC
        """)
    Page<Object[]> findAllWithParticipantCount(Pageable pageable);
    
    /**
     * 특정 클래스 상세 조회 (현재 참가 인원 수 포함)
     * 
     * @param id 클래스 ID
     * @return [GroupConsultation, currentParticipants]
     */
    @Query("""
        SELECT gc, 
               COUNT(p.id) as currentParticipants
        FROM GroupConsultation gc
        JOIN FETCH gc.consultation c
        JOIN FETCH c.coach coach
        LEFT JOIN Participant p ON p.consultation.id = gc.id
        WHERE gc.id = :id
        GROUP BY gc.id, c.id, coach.id
        """)
    Object[] findByIdWithParticipantCount(@Param("id") Long id);
}