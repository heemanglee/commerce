package com.practice.commerce.common.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security.jwt")
public record JwtProperty(
        String secretKey,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {
}
