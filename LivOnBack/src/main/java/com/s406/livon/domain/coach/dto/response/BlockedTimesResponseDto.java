package com.s406.livon.domain.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedTimesResponseDto {
    private String date;
    private List<String> blockedTimes;

    public static BlockedTimesResponseDto toDTO(String date, List<String> blockedTimes){
        return BlockedTimesResponseDto.builder()
                .date(date)
                .blockedTimes(blockedTimes)
                .build();
    }
}
