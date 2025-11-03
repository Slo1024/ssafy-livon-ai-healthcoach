package com.s406.livon.domain.ai.dto.gms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmsChatCompletionRequest {

    private String model;
    private List<GmsChatMessage> messages;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GmsChatMessage {
        private String role;
        private String content;
    }
}
