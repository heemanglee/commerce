package com.practice.commerce.common.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtParser jwtParser;

    private static final AntPathMatcher PM = new AntPathMatcher();
    private static final List<String> WHITE_LIST = List.of(
            "/users/sign-up", "/users/sign-in"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        return WHITE_LIST.stream().anyMatch(p -> PM.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var jws = jwtParser.parse(token); // 서명·만료 검증
                String userIdStr = jws.getPayload().getSubject();
                UUID userId = UUID.fromString(userIdStr);

                Object roleClaim = jws.getPayload().get("role");
                List<GrantedAuthority> authorities = List.of();
                if (roleClaim instanceof String) {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleClaim));
                } else if (roleClaim instanceof List<?>) {
                    authorities = ((List<?>) roleClaim).stream()
                            .map(Object::toString)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList());
                }

                var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
