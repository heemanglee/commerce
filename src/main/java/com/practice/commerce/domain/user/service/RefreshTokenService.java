package com.practice.commerce.domain.user.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "user:rf:";

    @Value("${spring.security.jwt.refresh-ttl-seconds}")
    private long ttlSeconds;

    public void save(String email, String refreshToken) {
        String key = KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }
}
