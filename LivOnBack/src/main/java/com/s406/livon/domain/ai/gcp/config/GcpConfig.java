package com.s406.livon.domain.ai.gcp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.s406.livon.global.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * GCP (Google Cloud Platform) 설정 클래스
 * Vertex AI 및 Cloud Storage 관련 설정을 담당
 */
@Configuration
@Getter
public class GcpConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    private static final List<String> CLOUD_SCOPES =
            List.of("https://www.googleapis.com/auth/cloud-platform");

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.credentials.location}")
    private String credentialsLocation;

    @Value("${gcp.storage.bucket.name}")
    private String bucketName;

    @Value("${gcp.vertex.ai.location}")
    private String vertexAiLocation;

    @Value("${gcp.vertex.ai.model.name}")
    private String modelName;

    /**
     * Google Cloud Storage 클라이언트 생성
     */
    @Bean
    public Storage googleCloudStorage() throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(loadCredentials())
                .build()
                .getService();
    }

    /**
     * Google Credentials를 Bean으로 등록
     */
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        return loadCredentials();
    }

    private GoogleCredentials loadCredentials() throws IOException {
        try (FileInputStream fis = new FileInputStream(credentialsLocation)) {
            return GoogleCredentials.fromStream(fis)
                    .createScoped(CLOUD_SCOPES);
        }
    }
}
