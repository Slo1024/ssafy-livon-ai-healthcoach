package com.s406.livon.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.s406.livon.domain.user.entity.HealthSurvey;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON에서 제외
public class HealthSurveyResponseDto {
    private Double weight;
    private Double height;
    private Integer steps;
    private Integer sleepTime;
    private String disease;
    private String sleepQuality;
    private String medicationsInfo;
    private String painArea;
    private String stressLevel;
    private String smokingStatus;
    private Integer avgSleepHours;
    private String activityLevel;
    private String caffeineIntakeLevel;

    public static HealthSurveyResponseDto toDTO(HealthSurvey healthSurvey) {
        if (healthSurvey == null) {
            // 모든 필드가 null인 빈 객체 -> JSON에서는 {}
            return new HealthSurveyResponseDto();
        }

        return HealthSurveyResponseDto.builder()
                .weight(healthSurvey.getWeight())          // null이면 그대로 null
                .height(healthSurvey.getHeight())
                .steps(healthSurvey.getSteps())
                .sleepTime(healthSurvey.getSleepTime())
                .disease(healthSurvey.getDisease())
                .sleepQuality(healthSurvey.getSleepQuality())
                .medicationsInfo(healthSurvey.getMedicationsInfo())
                .painArea(healthSurvey.getPainArea())
                .stressLevel(healthSurvey.getStressLevel())
                .smokingStatus(healthSurvey.getSmokingStatus())
                .avgSleepHours(healthSurvey.getAvgSleepHours())
                .activityLevel(healthSurvey.getActivityLevel())
                .caffeineIntakeLevel(healthSurvey.getCaffeineIntakeLevel())
                .build();
    }
}
