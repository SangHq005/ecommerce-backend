package com.example.ecommerce.ecommerce_backend.application.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final javax.crypto.SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(
            @Value("${app.security.jwt.secret-base64}") String secretBase64,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${app.security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        byte[] decoded = Base64.getDecoder().decode(secretBase64);
        this.key = Keys.hmacShaKeyFor(decoded);
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}

    public String newJti() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String issueAccessToken(Long userId, String email, List<String> roles, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("typ", "access")
                .claim("email", email)
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }


    public String issueRefreshToken(Long userId, String sessionRootJti, String refreshJti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTtlSeconds);
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .id(refreshJti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("root", sessionRootJti)
                .claim("typ", "refresh")
                .signWith(key)
                .compact();
    }

    public Claims parse(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public long getAccessTtlSeconds() { return accessTtlSeconds; }
    public long getRefreshTtlSeconds() { return refreshTtlSeconds; }
}

