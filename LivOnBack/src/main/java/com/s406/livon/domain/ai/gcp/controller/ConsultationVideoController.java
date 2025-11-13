package com.s406.livon.domain.ai.gcp.controller;

import com.s406.livon.domain.ai.gcp.dto.response.VideoSummaryResponseDto;
import com.s406.livon.domain.ai.gcp.service.ConsultationVideoService;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 상담 영상 통합 컨트롤러
 * 영상 업로드와 요약 생성을 통합적으로 처리합니다.
 */
@RestController
@RequestMapping("/consultations/video")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultation Video", description = "상담 영상 업로드 및 요약 통합 API")
public class ConsultationVideoController {

    private final ConsultationVideoService consultationVideoService;

    /**
     * 영상을 업로드하고 자동으로 AI 요약을 생성합니다.
     * 
     * @param consultationId 상담 ID
     * @param videoFile 영상 파일
     * @param preQnA 사전 QnA (선택사항)
     * @return 영상 요약 결과
     */
    @PostMapping(value = "/{consultationId}/upload-and-summarize", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "영상 업로드 및 요약 생성", 
        description = "1대1 코칭 영상을 업로드하고 AI 요약을 자동으로 생성합니다."
    )
    public ResponseEntity<?> uploadAndSummarize(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable Long consultationId,
            
            @Parameter(description = "영상 파일", required = true)
            @RequestParam("file") MultipartFile videoFile,
            
            @Parameter(description = "사전 QnA")
            @RequestParam(value = "preQnA", required = false) String preQnA) throws IOException {
        
        log.info("Received upload and summarize request for consultation ID: {}", consultationId);

        VideoSummaryResponseDto response = consultationVideoService.uploadAndSummarize(
                consultationId, videoFile, preQnA);

        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.INSERT_SUCCESS, response));
    }

    /**
     * 이미 업로드된 영상에 대해 요약을 재생성합니다.
     * 
     * @param consultationId 상담 ID
     * @param preQnA 사전 QnA (선택사항)
     * @return 영상 요약 결과
     */
    @PostMapping("/{consultationId}/regenerate-summary")
    @Operation(
        summary = "요약 재생성", 
        description = "이미 업로드된 영상에 대해 AI 요약을 재생성합니다."
    )
    public ResponseEntity<?> regenerateSummary(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable Long consultationId,
            
            @Parameter(description = "사전 QnA")
            @RequestParam(value = "preQnA", required = false) String preQnA) {
        
        log.info("Received regenerate summary request for consultation ID: {}", consultationId);

        VideoSummaryResponseDto response = consultationVideoService.regenerateSummary(
                consultationId, preQnA);

        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.UPDATE_SUCCESS, response));
    }

    /**
     * 상담 영상을 삭제합니다.
     * 
     * @param consultationId 상담 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{consultationId}")
    @Operation(
        summary = "영상 삭제", 
        description = "상담 영상을 GCS에서 삭제하고 DB 레코드를 업데이트합니다."
    )
    public ResponseEntity<?> deleteVideo(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable Long consultationId) {
        
        log.info("Received delete video request for consultation ID: {}", consultationId);

        consultationVideoService.deleteConsultationVideo(consultationId);

        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.DELETE_SUCCESS, "영상이 성공적으로 삭제되었습니다."));
    }
}
