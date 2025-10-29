package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.CoachCertificates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 코치 자격증 레포지토리
 */
@Repository
public interface CoachCertificatesRepository extends JpaRepository<CoachCertificates, UUID> {

    @Query("""
        select cc.certificatesName
        from CoachInfo ci
        join ci.coachCertificatesList cc
        where ci.id = :coachInfoId
    """)
    List<String> findCertificateNamesByCoachInfoId(@Param("coachInfoId") UUID coachInfoId);
}