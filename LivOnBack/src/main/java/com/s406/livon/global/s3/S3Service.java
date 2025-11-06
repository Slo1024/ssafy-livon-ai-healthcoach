package com.s406.livon.global.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 허용할 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 프로필 이미지를 S3에 업로드
     * @param file 업로드할 파일
     * @return S3에 저장된 파일의 전체 URL
     */
    public String uploadProfileImage(MultipartFile file) {
        return uploadImage(file, "profile-images");
    }

    /**
     * 그룹 상담 이미지를 S3에 업로드
     * @param file 업로드할 파일
     * @return S3에 저장된 파일의 전체 URL
     */
    public String uploadGroupConsultationImage(MultipartFile file) {
        return uploadImage(file, "group-consultation-images");
    }

    /**
     * 이미지를 S3에 업로드하는 공통 메서드
     * @param file 업로드할 파일
     * @param directory S3 버킷 내 디렉토리명
     * @return S3에 저장된 파일의 전체 URL
     */
    private String uploadImage(MultipartFile file, String directory) {
        // 1. 파일 유효성 검증
        validateFile(file);

        // 2. 파일명 생성 (UUID + 확장자)
        String fileName = createFileName(file.getOriginalFilename(), directory);

        // 3. S3에 업로드
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));

            // 4. 업로드된 파일의 전체 URL 반환
            return amazonS3Client.getUrl(bucket, fileName).toString();

        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new GeneralException(ErrorStatus.FILE_NOT_FOUND);
        }

        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GeneralException(ErrorStatus.FILE_SIZE_EXCEED);
        }

        // 파일 확장자 확인
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new GeneralException(ErrorStatus.FILE_INVALID_EXTENSION);
        }
    }

    /**
     * 고유한 파일명 생성 (UUID + 확장자)
     */
    private String createFileName(String originalFileName, String directory) {
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
        return directory + "/" + uniqueFileName;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new GeneralException(ErrorStatus.FILE_INVALID_EXTENSION);
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * S3에서 파일 삭제 (기존 이미지 삭제 시 사용)
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // URL에서 파일명(key) 추출
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            
            // 디렉토리 포함한 전체 key 추출
            String[] urlParts = fileUrl.split(bucket + ".s3.");
            if (urlParts.length > 1) {
                String key = urlParts[1].substring(urlParts[1].indexOf("/") + 1);
                amazonS3Client.deleteObject(bucket, key);
                log.info("S3 파일 삭제 완료: {}", key);
            }
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            // 삭제 실패해도 예외를 던지지 않음 (업로드는 성공했으므로)
        }
    }
}