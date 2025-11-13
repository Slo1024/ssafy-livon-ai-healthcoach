package com.s406.livon.domain.ai.gcp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * GCP (Google Cloud Platform) 설정 클래스
 * Vertex AI 및 Cloud Storage 관련 설정을 담당
 */
@Configuration
@Getter
public class GcpConfig {

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
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsLocation));

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }

    /**
     * Google Credentials를 Bean으로 등록
     */
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(new FileInputStream(credentialsLocation));
    }
}

