package com.s406.livon.domain.coach.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedTimesRequestDto {

    @Size(max = 8, message = "차단 가능한 시간은 최대 8개입니다.")
    private List<String> blockedTimes;
}
