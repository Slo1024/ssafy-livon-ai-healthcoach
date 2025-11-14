package com.s406.livon.global.config;

import com.s406.livon.global.config.properties.LivekitEgressProperties;
import com.s406.livon.global.config.properties.MinioProperties;
import io.livekit.server.EgressServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties({LivekitEgressProperties.class, MinioProperties.class})
@RequiredArgsConstructor
public class LivekitEgressConfig {

    private final LivekitEgressProperties egressProperties;

    @Bean
    public EgressServiceClient egressServiceClient(
            @Value("${livekit.api.url}") String livekitApiUrl,
            @Value("${livekit.api.key}") String apiKey,
            @Value("${livekit.api.secret}") String apiSecret
    ) {
        String host = StringUtils.hasText(egressProperties.getHost())
                ? egressProperties.getHost()
                : livekitApiUrl;
        if (!host.endsWith("/")) {
            host = host + "/";
        }
        return EgressServiceClient.Companion.createClient(host, apiKey, apiSecret);
    }
}
