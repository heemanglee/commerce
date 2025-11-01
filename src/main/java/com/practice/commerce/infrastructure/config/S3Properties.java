package com.practice.commerce.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties("app.aws")
@Component
public class S3Properties {

    private String region;
    private String bucket;
}
