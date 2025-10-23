package com.s406.livon.domain.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 코치 예약 가능 시간대 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTimesResponse {
    
    private UUID coachId;
    private String date;
    private List<String> availableTimes;
    
    public static AvailableTimesResponse of(UUID coachId, String date, List<String> availableTimes) {
        return AvailableTimesResponse.builder()
                .coachId(coachId)
                .date(date)
                .availableTimes(availableTimes)
                .build();
    }
}