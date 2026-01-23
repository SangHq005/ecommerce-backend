package com.example.ecommerce.ecommerce_backend.api.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

//Health Redis indicator
@Component("redis")
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate,
                                 RedisConnectionFactory connectionFactory) {
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            // Test Redis connection
            try (RedisConnection connection = connectionFactory.getConnection()) {
                String pingResponse = connection.ping();
                long responseTime = System.currentTimeMillis() - startTime;

                if ("PONG".equals(pingResponse)) {
                    // Get Redis info
                    Long dbSize = connection.dbSize();

                    Health.Builder builder = Health.up()
                            .withDetail("ping", pingResponse)
                            .withDetail("dbSize", dbSize)
                            .withDetail("responseTime", responseTime + "ms");

                    // Add warning if response time is slow
                    if (responseTime > 500) {
                        builder.withDetail("warning", "Slow Redis response time");
                    }

                    return builder.build();
                } else {
                    return Health.down()
                            .withDetail("ping", pingResponse)
                            .withDetail("error", "Unexpected ping response")
                            .build();
                }
            }
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("error", "Redis connection failed: " + e.getMessage())
                    .build();
        }
    }
}