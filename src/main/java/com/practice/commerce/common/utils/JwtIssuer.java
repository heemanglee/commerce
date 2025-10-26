package com.practice.commerce.common.utils;

import com.practice.commerce.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtIssuer {

    private final JwtProperty property;

    private SecretKey key() {
        byte[] k = io.jsonwebtoken.io.Decoders.BASE64.decode(property.secretKey());
        return Keys.hmacShaKeyFor(k);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Map<String, Object> claims = generateClaims(user);

        return Jwts.builder()
                .header()
                .type("Bearer")
                .and()
                .subject(user.getEmail())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(property.accessTtlSeconds())))
                .signWith(key(), SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Map<String, Object> claims = generateClaims(user);

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(property.refreshTtlSeconds())))
                .signWith(key(), SIG.HS256)
                .compact();
    }

    private static Map<String, Object> generateClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        return claims;
    }

}
