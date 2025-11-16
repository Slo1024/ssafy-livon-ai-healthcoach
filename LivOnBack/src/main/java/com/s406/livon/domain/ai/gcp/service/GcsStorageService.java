package com.s406.livon.domain.ai.gcp.service;

import com.google.cloud.storage.*;
import com.s406.livon.domain.ai.gcp.config.GcpConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Google Cloud Storage 서비스
 * 영상 파일을 GCS에 업로드하고 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GcsStorageService {

    private final Storage storage;
    private final GcpConfig gcpConfig;

    /**
     * MultipartFile을 GCS에 업로드합니다.
     * 
     * @param file 업로드할 파일
     * @param consultationId 상담 ID
     * @return GCS URI (gs://bucket/object)
     */
    public String uploadVideo(MultipartFile file, Long consultationId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        // 파일명 생성 (consultationId_timestamp_uuid.확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".mp4";
        
        String filename = String.format("consultations/%d/%d_%s%s", 
                consultationId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8),
                extension);

        // BlobId 생성
        BlobId blobId = BlobId.of(gcpConfig.getBucketName(), filename);
        
        // BlobInfo 생성 (메타데이터 포함)
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // 파일 업로드
        try (InputStream inputStream = file.getInputStream()) {
            storage.createFrom(blobInfo, inputStream);
        }

        log.info("Successfully uploaded video to GCS: gs://{}/{}", 
                gcpConfig.getBucketName(), filename);

        // GCS URI 반환
        return String.format("gs://%s/%s", gcpConfig.getBucketName(), filename);
    }

    /**
     * GCS에서 파일을 삭제합니다.
     * 
     * @param gcsUri GCS URI (gs://bucket/object)
     * @return 삭제 성공 여부
     */
    public boolean deleteVideo(String gcsUri) {
        if (!gcsUri.startsWith("gs://")) {
            log.error("Invalid GCS URI format: {}", gcsUri);
            return false;
        }

        try {
            // gs://bucket/object 형식에서 bucket과 object 추출
            String[] parts = gcsUri.substring(5).split("/", 2);
            if (parts.length != 2) {
                log.error("Invalid GCS URI structure: {}", gcsUri);
                return false;
            }

            String bucket = parts[0];
            String objectName = parts[1];

            BlobId blobId = BlobId.of(bucket, objectName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Successfully deleted video from GCS: {}", gcsUri);
            } else {
                log.warn("Failed to delete video from GCS (may not exist): {}", gcsUri);
            }

            return deleted;

        } catch (Exception e) {
            log.error("Error deleting video from GCS: {}", gcsUri, e);
            return false;
        }
    }

    /**
     * GCS 파일의 공개 URL을 생성합니다.
     * 
     * @param gcsUri GCS URI (gs://bucket/object)
     * @return HTTP(S) URL
     */
    public String getPublicUrl(String gcsUri) {
        if (!gcsUri.startsWith("gs://")) {
            return gcsUri;
        }

        String[] parts = gcsUri.substring(5).split("/", 2);
        if (parts.length != 2) {
            return gcsUri;
        }

        String bucket = parts[0];
        String objectName = parts[1];

        return String.format("https://storage.googleapis.com/%s/%s", bucket, objectName);
    }

    /**
     * 파일이 GCS에 존재하는지 확인합니다.
     * 
     * @param gcsUri GCS URI (gs://bucket/object)
     * @return 존재 여부
     */
    public boolean exists(String gcsUri) {
        if (!gcsUri.startsWith("gs://")) {
            return false;
        }

        try {
            String[] parts = gcsUri.substring(5).split("/", 2);
            if (parts.length != 2) {
                return false;
            }

            String bucket = parts[0];
            String objectName = parts[1];

            BlobId blobId = BlobId.of(bucket, objectName);
            Blob blob = storage.get(blobId);

            return blob != null && blob.exists();

        } catch (Exception e) {
            log.error("Error checking if video exists in GCS: {}", gcsUri, e);
            return false;
        }
    }
}

