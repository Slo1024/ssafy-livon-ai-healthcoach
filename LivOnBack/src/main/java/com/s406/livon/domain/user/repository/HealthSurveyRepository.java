package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HealthSurveyRepository extends JpaRepository<HealthSurvey, UUID> {

}