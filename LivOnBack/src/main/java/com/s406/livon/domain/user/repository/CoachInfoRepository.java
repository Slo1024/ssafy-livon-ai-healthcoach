package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CoachInfoRepository extends JpaRepository<CoachInfo, UUID> {
    /**
     * 사용자 ID로 코치 정보 조회
     */
    Optional<CoachInfo> findByUserId(UUID userId);
}