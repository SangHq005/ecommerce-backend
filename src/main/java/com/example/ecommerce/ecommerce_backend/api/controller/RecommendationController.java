package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "Product recommendations")
public class RecommendationController {

    private final RecommendationService recoService;

    public RecommendationController(RecommendationService recoService) {
        this.recoService = recoService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID");
        }
    }

    @PostMapping("/events")
    @Operation(summary = "Track event", description = "Track user interaction event")
    public ResponseEntity<ApiResponse<Void>> trackEvent(
            @RequestParam Long productId,
            @RequestParam String eventType
    ) {
        recoService.trackEvent(currentUserId(), productId, eventType);
        return ResponseHelper.ok(null, "Event tracked");
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending products", description = "Get trending products")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> getTrending() {
        List<ProductEntity> products = recoService.getTrendingProducts();
        return ResponseHelper.ok(products);
    }

    @GetMapping("/personalized")
    @Operation(summary = "Personalized recommendations", description = "Get personalized recommendations for user")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> getPersonalized(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ProductEntity> products = recoService.getPersonalizedRecommendations(currentUserId(), limit);
        return ResponseHelper.ok(products);
    }

    @GetMapping("/similar/{productId}")
    @Operation(summary = "Similar products", description = "Get similar products")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> getSimilar(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<ProductEntity> products = recoService.getSimilarProducts(productId, limit);
        return ResponseHelper.ok(products);
    }

    @GetMapping("/bought-together/{productId}")
    @Operation(summary = "Frequently bought together", description = "Get products frequently bought together")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> getFrequentlyBoughtTogether(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "4") int limit
    ) {
        List<ProductEntity> products = recoService.getFrequentlyBoughtTogether(productId, limit);
        return ResponseHelper.ok(products);
    }
}
