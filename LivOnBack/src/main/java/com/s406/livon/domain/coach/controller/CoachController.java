package com.s406.livon.domain.coach.controller;

import com.s406.livon.domain.coach.dto.request.CoachSearchRequest;
import com.s406.livon.domain.coach.dto.response.AvailableTimesResponse;
import com.s406.livon.domain.coach.dto.response.CoachDetailResponse;
import com.s406.livon.domain.coach.dto.response.CoachListResponse;
import com.s406.livon.domain.coach.service.CoachService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.PaginatedResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 코치 컨트롤러
 */
@RestController
@RequestMapping("/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 코치 목록 조회
     *
     * @param token Bearer 토큰
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param job 코치 직업 필터 (선택)
     * @param organizationType 조직 필터 (ALL, SAME_ORG)
     * @return 페이징된 코치 목록
     */
    @GetMapping
    @Operation(summary = "코치 목록 조회 API", description = "코치 목록을 페이징하여 조회합니다.")
    public ResponseEntity<?> getCoachList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String job,
            @RequestParam(defaultValue = "ALL") CoachSearchRequest.OrganizationType organizationType) {

        UUID currentUserId = jwtTokenProvider.getUserId(token.substring(7));

        CoachSearchRequest request = CoachSearchRequest.builder()
                .job(job)
                .organizationType(organizationType)
                .build();

        Page<CoachListResponse> coaches = coachService.getCoachList(
                currentUserId, request, page, size);

        // 데이터가 없는 경우
        if (coaches.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.noContent());
        }

        PaginatedResponse<CoachListResponse> response = PaginatedResponse.of(coaches);

        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }

    /**
     * 코치 상세 정보 조회
     *
     * @param coachId 조회할 코치 ID
     * @return 코치 상세 정보
     */
    @GetMapping("/{coachId}")
    @Operation(summary = "코치 상세 정보 조회 API", description = "특정 코치의 상세 정보를 조회합니다.")
    public ResponseEntity<?> getCoachDetail(@PathVariable UUID coachId) {

        CoachDetailResponse response = coachService.getCoachDetail(coachId);

        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }

    /**
     * 코치 예약 가능 시간대 조회
     *
     * @param coachId 조회할 코치 ID
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 예약 가능한 시간대 목록
     */
    @GetMapping("/{coachId}/available-times")
    @Operation(summary = "코치 예약 가능 시간대 조회 API", description = "특정 날짜의 코치 예약 가능 시간대를 조회합니다.")
    public ResponseEntity<?> getAvailableTimes(
            @PathVariable UUID coachId,
            @RequestParam String date) {

        AvailableTimesResponse response = coachService.getAvailableTimes(coachId, date);

        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }
}