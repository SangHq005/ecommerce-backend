package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.search.PageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SearchQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
public class ProductSearchService {

    private final SearchQueryRepository searchRepo;
    private final CacheService cache;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;

    public ProductSearchService(SearchQueryRepository searchRepo, CacheService cache, StringRedisTemplate redis, EventLogMongoRepository eventRepo) {
        this.searchRepo = searchRepo;
        this.cache = cache;
        this.redis = redis;
        this.eventRepo = eventRepo;
    }

    public PageResponse<ProductSearchHit> search(
            String q, Long categoryId, Long brandId, Long shopId,
            Long minPrice, Long maxPrice, Boolean inStock,
            String sort, int page, int size
    ) {
        String ver = redis.opsForValue().get("cache:catalog:ver");
        if (ver == null) ver = "0";

        String fingerprint = hash(ver + "|" + q + "|" + categoryId + "|" + brandId + "|" + shopId + "|" +
                minPrice + "|" + maxPrice + "|" + inStock + "|" + sort + "|" + page + "|" + size);

        String key = "cache:search:ver:" + ver + ":" + fingerprint;

        var cached = cache.get(key, new com.fasterxml.jackson.core.type.TypeReference<PageResponse<ProductSearchHit>>() {});
        if (cached.isPresent()) return cached.get();

        Page<ProductSearchHit> p = searchRepo.searchActiveProducts(q, categoryId, brandId, shopId, minPrice, maxPrice, inStock, sort, page, size);

        PageResponse<ProductSearchHit> resp = new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements());
        cache.set(key, resp, Duration.ofSeconds(45));

        // Event log - best effort
        try {
            eventRepo.save(new EventLogDocument(
                    "SEARCH_PERFORMED",
                    "search",
                    Instant.now(),
                    null,
                    Map.of(
                            "q", q,
                            "filters", Map.of("categoryId", categoryId, "brandId", brandId, "shopId", shopId, "minPrice", minPrice, "maxPrice", maxPrice, "inStock", inStock),
                            "sort", sort,
                            "page", page,
                            "size", size,
                            "hitCount", p.getNumberOfElements()
                    )
            ));
        } catch (Exception ignored) {

        }

        return resp;
    }

    private String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }

    /**
     * Get search suggestions for autocomplete
     *
     * @param query Partial search query
     * @param limit Maximum number of suggestions
     * @return List of product name suggestions
     */
    public java.util.List<String> getSearchSuggestions(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.List.of();
        }

        String key = "cache:suggestions:" + query.toLowerCase();
        var cached = cache.get(key, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
        if (cached.isPresent()) return cached.get();

        // Get suggestions from product names
        java.util.List<String> suggestions = searchRepo.findProductNameSuggestions(query, limit);

        cache.set(key, suggestions, Duration.ofMinutes(10));
        return suggestions;
    }

    /**
     * Get popular/trending search terms
     *
     * @param limit Number of terms to return
     * @return List of popular search queries
     */
    public java.util.List<Map<String, Object>> getPopularSearches(int limit) {
        String key = "cache:popular_searches";
        var cached = cache.get(key, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Map<String, Object>>>() {});
        if (cached.isPresent()) return cached.get();

        // This would typically come from search analytics/logs
        // For now, return most searched product categories or keywords
        java.util.List<Map<String, Object>> popular = new java.util.ArrayList<>();
        popular.add(Map.of("term", "laptop", "count", 1500));
        popular.add(Map.of("term", "phone", "count", 1200));
        popular.add(Map.of("term", "headphones", "count", 800));

        cache.set(key, popular, Duration.ofHours(1));
        return popular;
    }
}
