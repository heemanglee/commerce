package com.practice.commerce.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security.jwt")
public record JwtProperty(
        String secretKey,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {
}
