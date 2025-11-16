package com.s406.livon.domain.ai.gcp.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.s406.livon.domain.ai.gcp.config.GcpConfig;
import com.s406.livon.domain.ai.gcp.dto.request.VideoSummaryRequestDto;
import com.s406.livon.domain.ai.gcp.dto.response.VideoSummaryResponseDto;
import com.s406.livon.domain.coach.entity.IndividualConsultation;
import com.s406.livon.domain.coach.repository.IndividualConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * GCP Vertex AI (Gemini)를 사용한 영상 요약 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GcpVideoSummaryService {

    private final GcpConfig gcpConfig;
    private final GoogleCredentials googleCredentials;
    private final IndividualConsultationRepository individualConsultationRepository;

    /**
     * 영상을 분석하고 요약을 생성
     * 
     * @param requestDto 영상 요약 요청 정보
     * @return 영상 요약 결과
     */
    @Transactional
    public VideoSummaryResponseDto generateVideoSummary(VideoSummaryRequestDto requestDto) {

        IndividualConsultation individualConsultation = individualConsultationRepository
                .findById(requestDto.getConsultationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 상담을 찾을 수 없습니다. ID: " + requestDto.getConsultationId()));

        try (VertexAI vertexAi = new VertexAI.Builder()
                .setProjectId(gcpConfig.getProjectId())
                .setLocation(gcpConfig.getVertexAiLocation())
                .setCredentials(googleCredentials)
                .build()) {

            GenerativeModel model = new GenerativeModel(gcpConfig.getModelName(), vertexAi);
            String prompt = buildPrompt(requestDto);
            List<Part> parts = new ArrayList<>();
            
            // 텍스트 파트 추가
            parts.add(Part.newBuilder()
                    .setText(prompt)
                    .build());

            // 비디오 파트 추가 (GCS URI 또는 URL)
            if (requestDto.getVideoUrl() != null && !requestDto.getVideoUrl().isEmpty()) {
                // GCS URI 형식으로 변환 (필요한 경우)
                String videoUri = convertToGcsUri(requestDto.getVideoUrl());
                
                parts.add(Part.newBuilder()
                        .setFileData(com.google.cloud.vertexai.api.FileData.newBuilder()
                                .setFileUri(videoUri)
                                .setMimeType("video/mp4")
                                .build())
                        .build());
            }

            Content content = Content.newBuilder()
                    .setRole("user")
                    .addAllParts(parts)
                    .build();

            GenerateContentResponse response = model.generateContent(content);
            String summary = extractTextFromResponse(response);
            saveAiSummary(individualConsultation, summary);

            return VideoSummaryResponseDto.builder()
                    .consultationId(requestDto.getConsultationId())
                    .summary(summary)
                    .build();

        } catch (Exception e) {
            log.error("Error during Vertex AI processing for consultation ID: {}", 
                    requestDto.getConsultationId(), e);
            throw new RuntimeException("Vertex AI 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 영상 분석을 위한 프롬프트를 생성
     */
    private String buildPrompt(VideoSummaryRequestDto requestDto) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("당신은 전문 코칭 세션 분석가입니다. ");
        promptBuilder.append("다음 1대1 코칭 영상을 분석하고 상세한 요약을 작성해주세요.\n\n");
        
        promptBuilder.append("## 요약에 포함할 내용:\n");
        promptBuilder.append("1. 코칭 세션의 주요 주제와 목표\n");
        promptBuilder.append("2. 논의된 핵심 내용 및 문제점\n");
        promptBuilder.append("3. 코치가 제시한 조언 및 솔루션\n");
        promptBuilder.append("4. 클라이언트의 반응 및 인사이트\n");
        promptBuilder.append("5. 다음 세션을 위한 액션 아이템\n\n");

        // 사전 QnA가 있는 경우 추가
        if (requestDto.getPreQnA() != null && !requestDto.getPreQnA().isEmpty()) {
            promptBuilder.append("## 사전 질문 및 답변:\n");
            promptBuilder.append(requestDto.getPreQnA());
            promptBuilder.append("\n\n");
        }

        promptBuilder.append("## 요청사항:\n");
        promptBuilder.append("- 한국어로 작성해주세요\n");
        promptBuilder.append("- 명확하고 구조화된 형식으로 작성해주세요\n");
        promptBuilder.append("- 중요한 타임스탬프가 있다면 표시해주세요\n");
        promptBuilder.append("- 전문적이면서도 이해하기 쉬운 언어를 사용해주세요\n");

        return promptBuilder.toString();
    }

    /**
     * 비디오 URL을 GCS URI 형식으로 변환
     */
    private String convertToGcsUri(String videoUrl) {
        // 이미 GCS URI 형식인 경우
        if (videoUrl.startsWith("gs://")) {
            return videoUrl;
        }
        
        // HTTP(S) URL인 경우 GCS URI로 변환
        if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
            // storage.googleapis.com/{bucket}/{object} 형식 파싱
            if (videoUrl.contains("storage.googleapis.com")) {
                String[] parts = videoUrl.split("storage.googleapis.com/");
                if (parts.length > 1) {
                    return "gs://" + parts[1];
                }
            }
        }
        
        // 버킷 이름만 있는 경우
        if (!videoUrl.contains("/")) {
            return "gs://" + gcpConfig.getBucketName() + "/" + videoUrl;
        }
        
        return videoUrl;
    }

    /**
     * GenerateContentResponse에서 텍스트를 추출
     */
    private String extractTextFromResponse(GenerateContentResponse response) {
        if (response.getCandidatesCount() == 0) {
            throw new RuntimeException("AI 응답에서 결과를 찾을 수 없습니다.");
        }

        StringBuilder result = new StringBuilder();
        response.getCandidatesList().forEach(candidate -> {
            candidate.getContent().getPartsList().forEach(part -> {
                if (part.hasText()) {
                    result.append(part.getText());
                }
            });
        });

        return result.toString().trim();
    }

    /**
     * AI 요약을 DB에 저장
     */
    @Transactional
    public void saveAiSummary(IndividualConsultation consultation, String summary) {
        consultation.updateAiSummary(summary);
        individualConsultationRepository.save(consultation);
    }

    /**
     * 저장된 요약을 조회
     */
    @Transactional(readOnly = true)
    public VideoSummaryResponseDto getSummary(Long consultationId) {
        IndividualConsultation consultation = individualConsultationRepository
                .findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 상담을 찾을 수 없습니다. ID: " + consultationId));

        if (consultation.getAiSummary() == null || consultation.getAiSummary().isEmpty()) {
            throw new IllegalStateException("요약이 아직 생성되지 않았습니다.");
        }

        return VideoSummaryResponseDto.builder()
                .consultationId(consultationId)
                .summary(consultation.getAiSummary())
                .build();
    }
}

