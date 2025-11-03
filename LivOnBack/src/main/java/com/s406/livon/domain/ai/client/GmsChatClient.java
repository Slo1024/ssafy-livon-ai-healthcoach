package com.s406.livon.domain.ai.client;

import com.s406.livon.domain.ai.dto.gms.GmsChatCompletionRequest;
import com.s406.livon.domain.ai.dto.gms.GmsChatCompletionResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmsChatClient {

    private static final String ROLE_DEVELOPER = "developer";
    private static final String ROLE_USER = "user";

    private final WebClient webClient;

    @Value("${gms.api.base-url}")
    private String baseUrl;

    @Value("${gms.api.chat-completions-path}")
    private String chatCompletionsPath;

    @Value("${gms.api.model}")
    private String model;

    @Value("${gms.api.key}")
    private String apiKey;

    private URI chatCompletionUri;

    @PostConstruct
    void init() {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = chatCompletionsPath.startsWith("/") ? chatCompletionsPath : "/" + chatCompletionsPath;

        this.chatCompletionUri = URI.create(normalizedBase + normalizedPath);
        log.info("Initialized GMS chat completion URI: {}", chatCompletionUri);
    }

    public String requestHealthSummary(String developerInstruction, String userPrompt) {
        GmsChatCompletionRequest payload = GmsChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(
                        GmsChatCompletionRequest.GmsChatMessage.builder()
                                .role(ROLE_DEVELOPER)
                                .content(developerInstruction)
                                .build(),
                        GmsChatCompletionRequest.GmsChatMessage.builder()
                                .role(ROLE_USER)
                                .content(userPrompt)
                                .build()
                ))
                .build();

        WebClient client = webClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Mono<GmsChatCompletionResponse> responseMono = client.post()
                .uri(chatCompletionUri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(GmsChatCompletionResponse.class);

        GmsChatCompletionResponse response = responseMono.block();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.warn("GMS response is empty or null for prompt: {}", userPrompt);
            return null;
        }

        GmsChatCompletionResponse.Choice firstChoice = response.getChoices().get(0);
        if (firstChoice.getMessage() == null) {
            log.warn("GMS response choice has no message for prompt: {}", userPrompt);
            return null;
        }

        return firstChoice.getMessage().getContent();
    }
}
