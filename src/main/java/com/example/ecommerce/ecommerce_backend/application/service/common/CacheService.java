package com.example.ecommerce.ecommerce_backend.application.service.common;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class CacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    public CacheService(StringRedisTemplate redis, ObjectMapper om) {
        this.redis = redis;
        this.om = om;
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        String raw = redis.opsForValue().get(key);
        if (raw == null) return Optional.empty();
        try {
            return Optional.of(om.readValue(raw, type));
        } catch (Exception e) {
            redis.delete(key);
            return Optional.empty();
        }
    }

    public <T> Optional<T> get(String key, TypeReference<T> typeRef) {
        String raw = redis.opsForValue().get(key);
        if (raw == null) return Optional.empty();
        try {
            return Optional.of(om.readValue(raw, typeRef));
        } catch (Exception e) {
            redis.delete(key);
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redis.opsForValue().set(key, om.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
        }
    }

    public void del(String key) {
        redis.delete(key);
    }
}
