package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HealthSurveyRepository extends JpaRepository<HealthSurvey, UUID> {

    @Query("SELECT ROUND(AVG(h.steps), 2) FROM HealthSurvey h WHERE h.steps IS NOT NULL")
    Double calculateAverageSteps();

    @Query("SELECT ROUND(AVG(h.sleepTime), 2) FROM HealthSurvey h WHERE h.sleepTime IS NOT NULL")
    Double calculateAverageSleepTime();

    @Query("SELECT ROUND(AVG(h.height), 2) FROM HealthSurvey h WHERE h.height IS NOT NULL")
    Double calculateAverageHeight();

    @Query("SELECT ROUND(AVG(h.weight), 2) FROM HealthSurvey h WHERE h.weight IS NOT NULL")
    Double calculateAverageWeight();

    @Query("""
        SELECT 
            AVG(hs.steps),
            AVG(hs.sleepTime),
            AVG(hs.height),
            AVG(hs.weight)
        FROM HealthSurvey hs
        """)
    List<Object[]> calculateAllAverages();
}