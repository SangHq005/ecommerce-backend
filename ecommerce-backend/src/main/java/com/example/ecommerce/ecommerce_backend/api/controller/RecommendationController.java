package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.RecommendationService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recoService;

    public RecommendationController(RecommendationService recoService) {
        this.recoService = recoService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Recommendation can work for anonymous, but for tracking we might want ID or sessionID
            // For now simplify: enforce login
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
             throw ApiException.unauthorized("Invalid User ID");
        }
    }

    @PostMapping("/events")
    public void trackEvent(@RequestParam Long productId, @RequestParam String eventType) {
        recoService.trackEvent(currentUserId(), productId, eventType);
    }

    @GetMapping("/trending")
    public List<ProductEntity> getTrending() {
        return recoService.getTrendingProducts();
    }

    /**
     * Get personalized recommendations for authenticated user
     */
    @GetMapping("/personalized")
    public List<ProductEntity> getPersonalized(@RequestParam(defaultValue = "10") int limit) {
        return recoService.getPersonalizedRecommendations(currentUserId(), limit);
    }

    /**
     * Get similar products based on a given product
     */
    @GetMapping("/similar/{productId}")
    public List<ProductEntity> getSimilar(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        return recoService.getSimilarProducts(productId, limit);
    }

    /**
     * Get frequently bought together products
     */
    @GetMapping("/bought-together/{productId}")
    public List<ProductEntity> getFrequentlyBoughtTogether(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "4") int limit
    ) {
        return recoService.getFrequentlyBoughtTogether(productId, limit);
    }
}
