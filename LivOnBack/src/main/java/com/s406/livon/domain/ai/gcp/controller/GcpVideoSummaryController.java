package com.s406.livon.domain.ai.gcp.controller;

import com.s406.livon.domain.ai.gcp.dto.request.VideoSummaryRequestDto;
import com.s406.livon.domain.ai.gcp.dto.response.VideoSummaryResponseDto;
import com.s406.livon.domain.ai.gcp.service.GcpVideoSummaryService;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GCP Vertex AI를 사용한 영상 요약 컨트롤러
 */
@RestController
@RequestMapping("/gcp/video-summary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GCP Video Summary", description = "GCP Vertex AI를 사용한 영상 요약 API")
public class GcpVideoSummaryController {

    private final GcpVideoSummaryService gcpVideoSummaryService;

    /**
     * 1대1 코칭 영상을 분석하고 요약을 생성
     * 
     * @param requestDto 영상 요약 요청 정보
     * @return 생성된 요약
     */
    @PostMapping
    @Operation(summary = "영상 요약 생성", description = "1대1 코칭 영상을 AI로 분석하여 요약을 생성합니다.")
    public ResponseEntity<?> generateSummary(
            @RequestBody VideoSummaryRequestDto requestDto) {
        
        VideoSummaryResponseDto response = gcpVideoSummaryService.generateVideoSummary(requestDto);
        
        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.INSERT_SUCCESS, response));
    }

    /**
     * 저장된 영상 요약을 조회합니다.
     * 
     * @param consultationId 상담 ID
     * @return 저장된 요약
     */
    @GetMapping("/{consultationId}")
    @Operation(summary = "영상 요약 조회", description = "저장된 영상 요약을 조회합니다.")
    public ResponseEntity<?> getSummary(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable Long consultationId) {
        
        VideoSummaryResponseDto response = gcpVideoSummaryService.getSummary(consultationId);
        
        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }
}
