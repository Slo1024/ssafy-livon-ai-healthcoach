package com.s406.livon.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String region = "us-east-1";
    private boolean forcePathStyle = true;
}
