package com.s406.livon.domain.ai.gcp.service;

import com.s406.livon.domain.ai.gcp.dto.request.VideoSummaryRequestDto;
import com.s406.livon.domain.ai.gcp.dto.response.VideoSummaryResponseDto;
import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.consultation.repository.ConsultationRepository;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import com.s406.livon.global.config.properties.MinioProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import java.io.InputStream;
import java.net.URL;

/**
 * 상담 영상 통합 서비스
 * 영상 업로드, 저장, 요약 생성을 통합적으로 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationVideoService {

    private final GcsStorageService gcsStorageService;
    private final GcpVideoSummaryService gcpVideoSummaryService;
    private final ConsultationRepository consultationRepository;
    private final IndividualConsultationRepository individualConsultationRepository;

    private final MinioProperties minioProperties;

    /**
     * 영상을 업로드하고 자동으로 요약을 생성
     * 
     * @param consultationId 상담 ID
     * @param videoFile 영상 파일
     * @param preQnA 사전 QnA (선택사항)
     * @return 영상 요약 결과
     */
    @Transactional
    public VideoSummaryResponseDto uploadAndSummarize(
            Long consultationId, 
            MultipartFile videoFile,
            String preQnA) throws IOException {
        // Consultation, IndividualConsultation 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 상담을 찾을 수 없습니다. ID: " + consultationId));
        IndividualConsultation individualConsultation = individualConsultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "1:1 상담 정보를 찾을 수 없습니다. ID: " + consultationId));

        // 영상을 GCS에 업로드
        String gcsUri = gcsStorageService.uploadVideo(videoFile, consultationId);

        // Consultation에 비디오 URL 저장
        consultation.updateVideoUrl(gcsUri);
        consultationRepository.save(consultation);

        // 사전 QnA가 있다면 IndividualConsultation에 저장
        if (preQnA != null && !preQnA.isBlank()) {
            individualConsultation.updatePreQnA(preQnA);
            individualConsultationRepository.save(individualConsultation);
        }

        // AI 요약 생성 요청
        VideoSummaryRequestDto summaryRequest = VideoSummaryRequestDto.builder()
                .consultationId(consultationId)
                .videoUrl(gcsUri)
                .preQnA(preQnA)
                .build();

        VideoSummaryResponseDto summaryResponse = 
                gcpVideoSummaryService.generateVideoSummary(summaryRequest);

        return summaryResponse;
    }

    @Transactional
    public void uploadAndSummarizeFromUrl(Long consultationId, String videoUrl, String preQnA) {
        log.info("uploadAndSummarizeFromUrl: MinIO URL로부터 요약 처리 시작. consultationId={}", consultationId);

        String bucketName = minioProperties.getBucket();

        try {
            // 1. [수정] videoUrl 파싱은 오직 '파일 경로(Object Key)'를 얻기 위해서만 사용합니다.
            URL url = new URL(videoUrl); // videoUrl은 "http://127.0.0.1:9100/..."
            String fullPath = url.getPath(); // "/openvidu-appdata/..."

            // 2. Object Key(파일 경로) 추출
            int bucketPathIndex = fullPath.indexOf("/" + bucketName + "/");
            if (bucketPathIndex == -1) {
                log.error("MinIO URL 경로에 버킷 이름({})을 찾을 수 없습니다. URL: {}", bucketName, videoUrl);
                throw new IllegalArgumentException("MinIO URL에서 버킷 경로를 찾을 수 없습니다.");
            }
            String objectKey = fullPath.substring(bucketPathIndex + bucketName.length() + 2); // +2는 양쪽 슬래시
            String filename = objectKey.substring(objectKey.lastIndexOf('/') + 1);

            // 3. [핵심] MinIO 클라이언트는 'public' 주소로 생성
            // Egress가 반환한 '127.0.0.1' (잘못된 호스트)를 무시합니다.
            // 로컬 테스트 시 성공했던 주소를 사용합니다.
            String correctMinioEndpoint = "http://ov.s406.site:9000";

            log.debug("MinIO GetObject. Endpoint: {} (Ignoring URL host), Bucket: {}, Key: {}",
                    correctMinioEndpoint, bucketName, objectKey);

            MinioClient tempClient = MinioClient.builder()
                    .endpoint(correctMinioEndpoint) // ★ 실제 Public 주소 사용
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            // 4. 파일 다운로드
            InputStream videoStream = tempClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );

            // 5. (변경 없음) InputStream -> byte[] -> MultipartFile
            byte[] videoBytes = videoStream.readAllBytes();
            videoStream.close();
            log.info("uploadAndSummarizeFromUrl: MinIO 다운로드 완료. bytes={}, filename={}", videoBytes.length, filename);
            MultipartFile videoFile = new MockMultipartFile("file", filename, "video/mp4", videoBytes);

            // 6. (변경 없음) 기존 로직 호출
            uploadAndSummarize(consultationId, videoFile, preQnA);

        } catch (Exception e) { // MalformedURLException, MinioException, IOException 등
            log.error("URL로부터 영상 처리 중 예외 발생. consultationId={}", consultationId, e);
            throw new RuntimeException("URL 영상 처리 실패", e);
        }
    }

    /**
     * 이미 업로드된 영상에 대해 요약을 재생성
     * 
     * @param consultationId 상담 ID
     * @param preQnA 사전 QnA (선택사항)
     * @return 영상 요약 결과
     */
    @Transactional
    public VideoSummaryResponseDto regenerateSummary(Long consultationId, String preQnA) {
        log.info("Regenerating summary for consultation ID: {}", consultationId);

        // Consultation, IndividualConsultation 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 상담을 찾을 수 없습니다. ID: " + consultationId));
        IndividualConsultation individualConsultation = individualConsultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "1:1 상담 정보를 찾을 수 없습니다. ID: " + consultationId));

        // 비디오 URL 확인
        if (consultation.getVideoUrl() == null || consultation.getVideoUrl().isEmpty()) {
            throw new IllegalStateException("업로드된 영상이 없습니다.");
        }

        // 사전 QnA가 있다면 IndividualConsultation에 저장
        if (preQnA != null && !preQnA.isBlank()) {
            individualConsultation.updatePreQnA(preQnA);
            individualConsultationRepository.save(individualConsultation);
        }

        // AI 요약 생성 요청
        VideoSummaryRequestDto summaryRequest = VideoSummaryRequestDto.builder()
                .consultationId(consultationId)
                .videoUrl(consultation.getVideoUrl())
                .preQnA(preQnA)
                .build();

        return gcpVideoSummaryService.generateVideoSummary(summaryRequest);
    }

    /**
     * 상담 영상과 요약을 모두 삭제
     * 
     * @param consultationId 상담 ID
     */
    @Transactional
    public void deleteConsultationVideo(Long consultationId) {
        log.info("Deleting consultation video for ID: {}", consultationId);

        // Consultation 조회
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 상담을 찾을 수 없습니다. ID: " + consultationId));

        // GCS에서 영상 삭제
        if (consultation.getVideoUrl() != null && !consultation.getVideoUrl().isEmpty()) {
            boolean deleted = gcsStorageService.deleteVideo(consultation.getVideoUrl());
            if (!deleted) {
                log.warn("Failed to delete video from GCS for consultation ID: {}", 
                        consultationId);
            }
        }

        // DB에서 비디오 URL 제거
        consultation.updateVideoUrl(null);
        consultationRepository.save(consultation);

    }
}

