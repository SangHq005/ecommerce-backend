package com.example.ecommerce.ecommerce_backend.application.service.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate redis;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Simple fixed window counter: allow N tries / window.
     */
    public boolean allow(String key, int limit, Duration window) {
        String redisKey = "rate:" + key;
        Long v = redis.opsForValue().increment(redisKey);
        if (v != null && v == 1L) {
            redis.expire(redisKey, window);
        }
        return v != null && v <= limit;
    }
}
