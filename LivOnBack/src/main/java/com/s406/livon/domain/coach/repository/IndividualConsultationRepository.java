package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 1:1 상담 예약 레포지토리
 */
@Repository
public interface IndividualConsultationRepository extends JpaRepository<IndividualConsultation, Long> {
    // 여러 개 한번에 조회 (N+1 방지)
    @Query("SELECT ic FROM IndividualConsultation ic WHERE ic.consultation.id IN :consultationIds")
    List<IndividualConsultation> findByConsultationIdIn(@Param("consultationIds") List<Long> consultationIds);
}
