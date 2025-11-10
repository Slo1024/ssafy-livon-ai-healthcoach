package com.s406.livon.domain.consultation.controller;

import com.s406.livon.domain.consultation.dto.response.MyReservationResponseDto;
import com.s406.livon.domain.consultation.service.ConsultationService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ConsultationService consultationService;

    /**
     * 내 예약 내역 조회
     * @param status upcoming(앞으로 진행할 예약) 또는 past(지난 예약) - 필수
     * @param type ONE(1:1) 또는 GROUP(1:N) - 선택
     * @param page 페이지 번호 (default: 0)
     * @param size 페이지 크기 (default: 10)
     */
    @GetMapping("/my-reservations")
    public ApiResponse<PaginatedResponse<MyReservationResponseDto>> getMyReservations(
            @RequestHeader("Authorization") String token,
            @RequestParam @NotNull String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        Pageable pageable = PageRequest.of(page, size);
        
        PaginatedResponse<MyReservationResponseDto> result =
            consultationService.getMyReservations(userId, status, type, pageable);
        
        return ApiResponse.onSuccess(result);
    }
}