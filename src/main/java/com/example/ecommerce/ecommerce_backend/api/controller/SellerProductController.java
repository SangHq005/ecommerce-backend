package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.ProductQualityService;
import com.example.ecommerce.ecommerce_backend.application.service.ProductQualityService.QualityCheckResult;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * NEW Controller for enhanced seller product management
 */
@RestController
@RequestMapping("/api/v1/seller/products")
@Tag(name = "Seller Products", description = "Enhanced seller product management")
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final ProductQualityService qualityService;
    private final ProductJpaRepository productRepo;

    public SellerProductController(
            ProductQualityService qualityService,
            ProductJpaRepository productRepo
    ) {
        this.qualityService = qualityService;
        this.productRepo = productRepo;
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

    @GetMapping("/{productId}/quality-check")
    @Operation(summary = "Check product quality", description = "Get quality score and improvement suggestions")
    public ResponseEntity<ApiResponse<QualityCheckResult>> checkQuality(@PathVariable Long productId) {
        // Verify ownership
        ProductEntity product = productRepo.findByIdAndSellerUserId(productId, currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found"));

        QualityCheckResult result = qualityService.checkQuality(productId);
        return ResponseHelper.ok(result);
    }

    @PostMapping("/{productId}/update-quality-score")
    @Operation(summary = "Update quality score", description = "Recalculate and save quality score")
    public ResponseEntity<ApiResponse<QualityScoreResponse>> updateQualityScore(@PathVariable Long productId) {
        // Verify ownership
        ProductEntity product = productRepo.findByIdAndSellerUserId(productId, currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found"));

        int score = qualityService.updateQualityScore(productId);
        return ResponseHelper.ok(new QualityScoreResponse(productId, score, QualityCheckResult.gradeFromScore(score)));
    }

    @PutMapping("/{productId}/feature")
    @Operation(summary = "Toggle featured", description = "Mark/unmark product as featured")
    public ResponseEntity<ApiResponse<ProductEntity>> toggleFeatured(
            @PathVariable Long productId,
            @RequestBody FeatureRequest request
    ) {
        ProductEntity product = productRepo.findByIdAndSellerUserId(productId, currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found"));

        product.setIsFeatured(request.featured());
        ProductEntity saved = productRepo.save(product);
        return ResponseHelper.ok(saved);
    }

    @PutMapping("/{productId}/shipping")
    @Operation(summary = "Update shipping info", description = "Update weight and shipping fee type")
    public ResponseEntity<ApiResponse<ProductEntity>> updateShipping(
            @PathVariable Long productId,
            @RequestBody ShippingInfoRequest request
    ) {
        ProductEntity product = productRepo.findByIdAndSellerUserId(productId, currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found"));

        if (request.weightGrams() != null) {
            product.setWeightGrams(request.weightGrams());
        }
        if (request.shippingFeeType() != null) {
            product.setShippingFeeType(request.shippingFeeType());
        }

        ProductEntity saved = productRepo.save(product);
        
        // Recalculate quality score
        qualityService.updateQualityScore(productId);
        
        return ResponseHelper.ok(saved);
    }

    // === Request/Response Records ===

    public record QualityScoreResponse(Long productId, int score, String grade) {}
    public record FeatureRequest(boolean featured) {}
    public record ShippingInfoRequest(Integer weightGrams, String shippingFeeType) {}
}
