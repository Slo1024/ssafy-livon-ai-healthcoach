package com.s406.livon.domain.coach.repository;

import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 코치 레포지토리
 */
@Repository
public interface CoachRepository extends JpaRepository<User, UUID> {

    /**
     * 코치 목록 조회 (전체, 직업 필터)
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN CoachInfo ci ON u.id = ci.id " +
            "WHERE :coachRole MEMBER OF u.roles " +
            "AND (:job IS NULL OR ci.job = :job)")
    Page<User> findCoaches(@Param("coachRole") Role coachRole,
                           @Param("job") String job,
                           Pageable pageable);

    /**
     * 코치 목록 조회 (같은 조직, 직업 필터)
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN CoachInfo ci ON u.id = ci.id " +
            "WHERE :coachRole MEMBER OF u.roles " +
            "AND u.organizations.id = :orgId " +
            "AND (:job IS NULL OR ci.job = :job)")
    Page<User> findCoachesByOrganization(@Param("coachRole") Role coachRole,
                                         @Param("orgId") Organizations organizations,
                                         @Param("job") String job,
                                         Pageable pageable);
}