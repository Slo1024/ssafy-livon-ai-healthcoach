package com.s406.livon.domain.ai.repository;

import com.s406.livon.domain.ai.entity.AiAnalysis;
import com.s406.livon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, UUID> {

    Optional<AiAnalysis> findByUser(User user);
}
