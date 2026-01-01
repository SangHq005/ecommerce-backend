package com.example.ecommerce.ecommerce_backend.application.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redis;

    public TokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void blacklistJti(String jti, Duration ttl) {
        redis.opsForValue().set(keyJti(jti), "1", ttl);
    }

    public boolean isBlacklisted(String jti) {
        return redis.hasKey(keyJti(jti));
    }

    private String keyJti(String jti) {
        return "auth:blacklist:jti:" + jti;
    }
}
