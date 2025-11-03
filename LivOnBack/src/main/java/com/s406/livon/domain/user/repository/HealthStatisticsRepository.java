package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.HealthStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthStatisticsRepository extends JpaRepository<HealthStatistics, Long> {
}