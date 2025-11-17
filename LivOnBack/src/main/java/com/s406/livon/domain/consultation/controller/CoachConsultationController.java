package com.s406.livon.domain.consultation.controller;

import com.s406.livon.domain.consultation.dto.response.CoachConsultationResponseDto;
import com.s406.livon.domain.consultation.dto.response.ParticipantInfoResponseDto;
import com.s406.livon.domain.consultation.service.CoachConsultationService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/coaches/consultations")
@RequiredArgsConstructor
@Tag(name = "코치 상담 관리", description = "코치의 상담 조회 API")
public class CoachConsultationController {

    private final CoachConsultationService coachConsultationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "코치 상담 목록 조회", description = "코치가 자신의 상담 목록을 조회합니다.")
    public ApiResponse<PaginatedResponse<CoachConsultationResponseDto>> getCoachConsultations(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "상담 상태 (upcoming: 예정, past: 지난)", example = "upcoming")
            @RequestParam String status,
            @Parameter(description = "상담 타입 (ONE: 1:1, GROUP: 그룹) - 선택사항", example = "ONE")
            @RequestParam(required = false) String type,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        Pageable pageable = PageRequest.of(page, size);

        PaginatedResponse<CoachConsultationResponseDto> response = 
                coachConsultationService.getCoachConsultations(coachId, status, type, pageable);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 코치가 상담 참여자 정보를 조회
     */
    @Operation(
                    summary = "상담 참여자 정보 조회",
                    description = "코치가 자신의 상담에 예약한 회원들의 기본 정보와 건강 데이터를 조회합니다."
    )
    @GetMapping("/{consultationId}/participant-info")
    public ApiResponse<List<ParticipantInfoResponseDto>> getParticipantInfo(
                    @Parameter(description = "상담 ID", required = true)
                    @PathVariable Long consultationId,
                    @Parameter(hidden = true)
                    @RequestHeader("Authorization") String token
    ) {
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        List<ParticipantInfoResponseDto> response = coachConsultationService.getParticipantInfo(consultationId, coachId);

        return ApiResponse.onSuccess(response);
    }
}
