package com.s406.livon.domain.coach.controller;

import com.s406.livon.domain.coach.dto.request.GroupConsultationCreateRequestDto;
import com.s406.livon.domain.coach.dto.request.GroupConsultationUpdateRequestDto;
import com.s406.livon.domain.coach.dto.response.GroupConsultationDetailResponseDto;
import com.s406.livon.domain.coach.dto.response.GroupConsultationListResponseDto;
import com.s406.livon.domain.coach.service.GroupConsultationService;
import com.s406.livon.domain.user.dto.request.SignUpDto;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.PaginatedResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 그룹 상담(클래스) 컨트롤러
 */
@RestController
@RequestMapping("/group-consultations")
@RequiredArgsConstructor
@Tag(name = "GroupConsultation", description = "그룹 상담(클래스) API")
public class GroupConsultationController {
    
    private final GroupConsultationService groupConsultationService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 클래스 생성
     *
     * @param token Authorization 헤더
     * @param request 클래스 생성 요청
     * @return 생성된 클래스 ID
     */
    @PostMapping
    @Operation(summary = "클래스 생성 API", description = "코치가 그룹 상담(클래스)을 생성합니다.")
    public ResponseEntity<ApiResponse<Long>> createGroupConsultation(
            @RequestHeader("Authorization") String token,
            @RequestPart("data") GroupConsultationCreateRequestDto request,
            @RequestPart(value = "classImage", required = false) MultipartFile classImage) {
        
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        Long classId = groupConsultationService.createGroupConsultation(coachId, request, classImage);
        
        return ResponseEntity.ok()
                .body(ApiResponse.of(SuccessStatus.INSERT_SUCCESS, classId));
    }
    
    /**
     * 클래스 목록 조회
     *
     * @param token Authorization 헤더
     * @param sameOrganization 같은 소속만 조회 여부 (기본값: false)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 클래스 목록
     */
    @GetMapping
    @Operation(summary = "클래스 목록 조회 API", description = "그룹 상담(클래스) 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PaginatedResponse<GroupConsultationListResponseDto>>> getGroupConsultations(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "같은 소속만 조회 여부")
            @RequestParam(defaultValue = "false") boolean sameOrganization,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {
        
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        // 시작 시간 오름차순 정렬
        Pageable pageable = PageRequest.of(page, size);
        
        PaginatedResponse<GroupConsultationListResponseDto> response =
                groupConsultationService.getGroupConsultations(userId, sameOrganization, pageable);
        
        return ResponseEntity.ok()
                .body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }
    
    /**
     * 클래스 상세 조회
     *
     * @param id 클래스 ID
     * @return 클래스 상세 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "클래스 상세 조회 API", description = "특정 그룹 상담(클래스)의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<GroupConsultationDetailResponseDto>> getGroupConsultation(
            @Parameter(description = "클래스 ID")
            @PathVariable Long id) {
        
        GroupConsultationDetailResponseDto response =
                groupConsultationService.getGroupConsultation(id);
        
        return ResponseEntity.ok()
                .body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }
    
    /**
     * 클래스 수정
     *
     * @param token Authorization 헤더
     * @param id 클래스 ID
     * @param request 수정 요청
     * @return 성공 응답
     */
    @PutMapping("/{id}")
    @Operation(summary = "클래스 수정 API", description = "코치가 자신의 클래스를 수정합니다. 예약자가 있는 경우 수정 불가합니다.")
    public ResponseEntity<ApiResponse<Void>> updateGroupConsultation(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "클래스 ID")
            @PathVariable Long id,
            @Valid @RequestBody GroupConsultationUpdateRequestDto request) {
        
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        groupConsultationService.updateGroupConsultation(coachId, id, request);
        
        return ResponseEntity.ok()
                .body(ApiResponse.of(SuccessStatus.UPDATE_SUCCESS, null));
    }
    
    /**
     * 클래스 삭제 (취소)
     *
     * @param token Authorization 헤더
     * @param id 클래스 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "클래스 삭제 API", description = "코치가 자신의 클래스를 삭제(취소)합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGroupConsultation(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "클래스 ID")
            @PathVariable Long id) {
        
        UUID coachId = jwtTokenProvider.getUserId(token.substring(7));
        groupConsultationService.deleteGroupConsultation(coachId, id);
        
        return ResponseEntity.ok()
                .body(ApiResponse.of(SuccessStatus.DELETE_SUCCESS, null));
    }
}
