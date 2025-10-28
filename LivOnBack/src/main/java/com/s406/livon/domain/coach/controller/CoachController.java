package com.s406.livon.domain.coach.controller;

import com.s406.livon.domain.coach.dto.request.BlockedTimesRequestDto;
import com.s406.livon.domain.coach.dto.response.BlockedTimesResponseDto;
import com.s406.livon.domain.coach.dto.request.CoachSearchRequestDto;
import com.s406.livon.domain.coach.dto.response.AvailableTimesResponseDto;
import com.s406.livon.domain.coach.dto.response.CoachDetailResponseDto;
import com.s406.livon.domain.coach.dto.response.CoachListResponseDto;
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
            @RequestParam(defaultValue = "ALL") CoachSearchRequestDto.OrganizationType organizationType) {

        UUID currentUserId = jwtTokenProvider.getUserId(token.substring(7));

        CoachSearchRequestDto request = CoachSearchRequestDto.builder()
                .job(job)
                .organizationType(organizationType)
                .build();

        Page<CoachListResponseDto> coaches = coachService.getCoachList(
                currentUserId, request, page, size);

        // 데이터가 없는 경우
        if (coaches.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.noContent());
        }

        PaginatedResponse<CoachListResponseDto> response = PaginatedResponse.of(coaches);

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

        CoachDetailResponseDto response = coachService.getCoachDetail(coachId);

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

        AvailableTimesResponseDto response = coachService.getAvailableTimes(coachId, date);

        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }

    /**
     * 코치가 스스로 예약을 막아놓은 시간대 조회
     *
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 예약 가능한 시간대 목록
     */
    @GetMapping("/block-times")
    @Operation(summary = "코치가 막아놓은 시간대 조회 API", description = "특정 날짜의 코치가 막아놓은 시간대를 조회합니다.")
    public ResponseEntity<?> getBlockedTimes(
            @RequestHeader("Authorization") String token,
            @RequestParam String date) {
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        BlockedTimesResponseDto response = coachService.getBlockedTimes(coachId, date);

        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }

    /**
     * 코치가 스스로 예약을 막아놓은 시간대 업데이트
     *
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 새로 갱신된 차단 시간대 목록
     */
    @PutMapping("/block-times")
    @Operation(summary = "코치가 막아놓은 시간대 업데이트 API", description = "특정 날짜의 코치가 막아놓은 시간대를 업데이트합니다.")
    public ResponseEntity<?> updateBlockedTimes(
            @RequestHeader("Authorization") String token,
            @RequestParam String date,
            @RequestBody BlockedTimesRequestDto blockedTimesRequestDto) {
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        BlockedTimesResponseDto response = coachService.updateBlockedTimes(coachId, date, blockedTimesRequestDto);

        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }
}