package com.s406.livon.domain.coach.controller;

import com.s406.livon.domain.coach.dto.request.GroupConsultationCreateRequestDto;
import com.s406.livon.domain.coach.dto.request.IndivualConsultationReservationRequestDto;
import com.s406.livon.domain.coach.service.IndividualConsultationService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/individual-consultations")
@RequiredArgsConstructor
@Tag(name = "IndividualConsultation", description = "1:1 상담 API")
public class IndividualConsultationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final IndividualConsultationService individualConsultationService;

    //상담 예약하기 API
    @PostMapping
    @Operation(summary = "1:1 상담 예약 API", description = "일반 사용자가 코치와의 1:1 상담을 신청합니다.")
    public ResponseEntity<ApiResponse<Long>> reserveIndividualConsultation(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody IndivualConsultationReservationRequestDto requestDto) {

        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        Long consultationId = individualConsultationService.reserveConsultation(userId, requestDto);

        return ResponseEntity.ok()
                .body(ApiResponse.of(SuccessStatus.INSERT_SUCCESS, consultationId));
    }

    //상담 취소하기 API(일반 사용자)

    //상담 취소하기 API(코치)
}
