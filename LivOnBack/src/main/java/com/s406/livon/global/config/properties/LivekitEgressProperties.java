package com.s406.livon.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "livekit.egress")
public class LivekitEgressProperties {

    /**
     * Base URL (including scheme) of the LiveKit/Egress API endpoint.
     */
    private String host;

    /**
     * Default layout applied when requesting room composite recordings.
     */
    private String layout = "grid";

    /**
     * Prefix used when composing file paths inside the configured bucket.
     */
    private String filePrefix = "recordings";
}
