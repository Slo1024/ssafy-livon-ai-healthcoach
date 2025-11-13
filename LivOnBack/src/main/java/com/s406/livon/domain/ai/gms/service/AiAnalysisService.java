package com.s406.livon.domain.ai.gms.service;

import com.s406.livon.domain.ai.gms.client.GmsChatClient;
import com.s406.livon.domain.ai.gms.dto.response.AiSummaryResponseDto;
import com.s406.livon.domain.ai.gms.entity.AiAnalysis;
import com.s406.livon.domain.ai.gms.repository.AiAnalysisRepository;
import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.handler.AiHandler;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiAnalysisService {

    private static final String DEVELOPER_INSTRUCTION = """
            너는 건강 데이터를 분석하는 전문가이자 건강 코치야.
            다음 지침을 반드시 지켜서 한국어로 일관된 답변을 만들어.
            1) 인사나 부가 설명 없이 바로 "현재 상태 요약:"으로 시작하는 한 단락(3~4문장)으로 주요 상태와 주의 사항을 정리해.
            2) 이어서 줄바꿈 후 "추천 행동:"이라는 제목을 적고, 하이픈(-)을 사용한 불릿 2개로 핵심 개선 행동을 제시해.
            3) 의학적 단어는 쉬운 표현으로 풀어 쓰고, 과도한 추정은 피하며 데이터에 기반한 근거를 언급해.
            4) 마지막에 줄바꿈 후 "[건강 설문 데이터]" 제목을 적고, 이어서 입력으로 받은 핵심 수치를 보여줘.
            """;

    private final UserRepository userRepository;
    private final HealthSurveyRepository healthSurveyRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final GmsChatClient gmsChatClient;

    @Transactional
    public AiSummaryResponseDto generateSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        HealthSurvey survey = healthSurveyRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND_HEALTH));

        return generateSummaryInternal(user, survey);
    }

    public AiSummaryResponseDto getSummary(UUID userId) {
        AiAnalysis analysis = aiAnalysisRepository.findById(userId)
                .orElseThrow(() -> new AiHandler(ErrorStatus.AI_SUMMARY_NOT_FOUND));

        return AiSummaryResponseDto.of(userId, analysis.getAnalyzeInfo());
    }

    private AiSummaryResponseDto generateSummaryInternal(User user, HealthSurvey survey) {
        UUID userId = user.getId();
        String prompt = buildUserPrompt(user, survey);
        String aiSummary = gmsChatClient.requestHealthSummary(DEVELOPER_INSTRUCTION, prompt);

        if (!StringUtils.hasText(aiSummary)) {
            throw new AiHandler(ErrorStatus.AI_SUMMARY_REQUEST_FAILED);
        }

        AiAnalysis analysis = aiAnalysisRepository.findById(userId)
                .map(entity -> {
                    entity.updateAnalyzeInfo(aiSummary.trim());
                    return entity;
                })
                .orElseGet(() -> AiAnalysis.builder()
                        .id(userId)
                        .user(user)
                        .analyzeInfo(aiSummary.trim())
                        .build());

        aiAnalysisRepository.save(analysis);

        return AiSummaryResponseDto.of(userId, analysis.getAnalyzeInfo());
    }

    private String buildUserPrompt(User user, HealthSurvey survey) {

        return String.format(Locale.KOREA, """
                다음은 한 사용자의 기본 정보와 건강 설문 데이터야.
                사용자의 현재 건강 상태를 이해하고, 위험 신호나 주의해야 할 생활습관을 짚어줘.
                
                [사용자 기본 정보]
                - 닉네임: %s
                - 성별: %s
                - 생년월일: %s
                
                [건강 설문 데이터]
                - 평균 수면 시간(시간): %d
                - 실제 수면 시간(시간): %d
                - 평균 걸음 수(걸음): %d
                - 키(cm): %.1f
                - 몸무게(kg): %.1f
                - 질병 이력: %s
                - 복용 약 정보: %s
                - 통증 부위: %s
                - 수면의 질: %s
                - 스트레스 수준: %s
                - 흡연 여부: %s
                - 활동량 수준: %s
                - 카페인 섭취 수준: %s
                
                위 데이터를 참고하여 이해하기 쉬운 언어로 요약해줘.
                """,
                user.getNickname(),
                user.getGender(),
                user.getBirthdate(),
                survey.getAvgSleepHours(),
                survey.getSleepTime(),
                survey.getSteps(),
                survey.getHeight(),
                survey.getWeight(),
                emptySafe(survey.getDisease()),
                emptySafe(survey.getMedicationsInfo()),
                emptySafe(survey.getPainArea()),
                emptySafe(survey.getSleepQuality()),
                emptySafe(survey.getStressLevel()),
                emptySafe(survey.getSmokingStatus()),
                emptySafe(survey.getActivityLevel()),
                emptySafe(survey.getCaffeineIntakeLevel())
        );
    }

    private String emptySafe(String value) {
        return StringUtils.hasText(value) ? value : "미기재";
    }
}
