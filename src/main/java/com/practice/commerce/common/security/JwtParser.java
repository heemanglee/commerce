package com.practice.commerce.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtParser {

    @Value("${spring.security.jwt.secret-key}")
    private String secretBase64;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 파싱 및 서명/만료 검증
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token); // 유효성, exp, iat 검증
    }

    // Claims 반환
    public Claims getClaims(String token) {
        return parse(token).getPayload();
    }

    // 토큰 만료 여부 확인
    public boolean isExpired(String token) {
        try {
            return getClaims(token)
                    .getExpiration()
                    .before(new java.util.Date());
        } catch (Exception e) {
            return true; // 파싱 실패도 만료로 처리
        }
    }
}
