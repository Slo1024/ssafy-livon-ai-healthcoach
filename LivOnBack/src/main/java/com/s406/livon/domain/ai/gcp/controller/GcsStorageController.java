package com.s406.livon.domain.ai.gcp.controller;

import com.s406.livon.domain.ai.gcp.dto.response.VideoUploadResponseDto;
import com.s406.livon.domain.ai.gcp.service.GcsStorageService;
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
 * Google Cloud Storage 컨트롤러
 * 영상 파일 업로드 및 관리를 담당합니다.
 */
@RestController
@RequestMapping("/gcp/storage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GCS Storage", description = "Google Cloud Storage 영상 업로드 API")
public class GcsStorageController {

    private final GcsStorageService gcsStorageService;

    /**
     * 영상 파일을 GCS에 업로드합니다.
     * 
     * @param consultationId 상담 ID
     * @param file 업로드할 영상 파일
     * @return 업로드 결과 (GCS URI 포함)
     */
    @PostMapping(value = "/upload/{consultationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "영상 파일 업로드", description = "코칭 영상 파일을 GCS에 업로드합니다.")
    public ResponseEntity<?> uploadVideo(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable Long consultationId,
            
            @Parameter(description = "영상 파일", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // GCS에 업로드
        String gcsUri = gcsStorageService.uploadVideo(file, consultationId);
        String publicUrl = gcsStorageService.getPublicUrl(gcsUri);

        VideoUploadResponseDto response = VideoUploadResponseDto.builder()
                .consultationId(consultationId)
                .gcsUri(gcsUri)
                .publicUrl(publicUrl)
                .build();

        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.INSERT_SUCCESS, response));
    }

    /**
     * GCS에서 영상 파일을 삭제합니다.
     * 
     * @param gcsUri GCS URI (gs://bucket/object)
     * @return 삭제 결과
     */
    @DeleteMapping
    @Operation(summary = "영상 파일 삭제", description = "GCS에서 영상 파일을 삭제합니다.")
    public ResponseEntity<?> deleteVideo(
            @Parameter(description = "GCS URI", required = true)
            @RequestParam String gcsUri) {

        gcsStorageService.deleteVideo(gcsUri);

        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.DELETE_SUCCESS, "영상이 성공적으로 삭제되었습니다."));
    }

    /**
     * GCS 파일 존재 여부를 확인합니다.
     * 
     * @param gcsUri GCS URI
     * @return 존재 여부
     */
    @GetMapping("/exists")
    @Operation(summary = "영상 파일 존재 확인", description = "GCS에 영상 파일이 존재하는지 확인합니다.")
    public ResponseEntity<?> checkVideoExists(
            @Parameter(description = "GCS URI", required = true)
            @RequestParam String gcsUri) {

        boolean exists = gcsStorageService.exists(gcsUri);
        
        return ResponseEntity.ok().body(
                ApiResponse.of(SuccessStatus.SELECT_SUCCESS, exists));
    }
}
