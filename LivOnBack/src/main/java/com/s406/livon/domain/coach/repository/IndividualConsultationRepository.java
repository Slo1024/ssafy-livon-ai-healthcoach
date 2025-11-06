package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 1:1 상담 예약 레포지토리
 */
@Repository
public interface IndividualConsultationRepository extends JpaRepository<IndividualConsultation, Long> {
}
