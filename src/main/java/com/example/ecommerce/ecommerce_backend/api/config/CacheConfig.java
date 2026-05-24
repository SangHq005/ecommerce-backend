package com.example.ecommerce.ecommerce_backend.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * Configures different TTLs for different cache regions
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Cache names
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_PRODUCT_DETAILS = "product-details";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_BRANDS = "brands";
    public static final String CACHE_USER_PROFILE = "user-profile";
    public static final String CACHE_SHOP_INFO = "shop-info";
    public static final String CACHE_COUPONS = "coupons";
    public static final String CACHE_SELLER_ANALYTICS = "seller-analytics";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer =
                GenericJackson2JsonRedisSerializer.builder()
                        .objectMapper(objectMapper.copy())
                        .defaultTyping(true)
                        .build();

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // Default TTL: 5 minutes
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();

        // Custom configurations for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Product cache - 5 minutes TTL
        cacheConfigurations.put(CACHE_PRODUCTS,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Product details - 10 minutes TTL (less frequently changed)
        cacheConfigurations.put(CACHE_PRODUCT_DETAILS,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Categories - 30 minutes TTL (rarely changes)
        cacheConfigurations.put(CACHE_CATEGORIES,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Brands - 30 minutes TTL (rarely changes)
        cacheConfigurations.put(CACHE_BRANDS,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // User profile - 5 minutes TTL
        cacheConfigurations.put(CACHE_USER_PROFILE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Shop info - 15 minutes TTL
        cacheConfigurations.put(CACHE_SHOP_INFO,
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Coupons - 10 minutes TTL
        cacheConfigurations.put(CACHE_COUPONS,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Seller analytics overview - 3 minutes TTL
        cacheConfigurations.put(CACHE_SELLER_ANALYTICS,
                defaultConfig.entryTtl(Duration.ofMinutes(3)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
