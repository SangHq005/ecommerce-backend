package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.InventoryService;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.InventoryService.BatchAdjustmentRequest;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.InventoryService.BatchAdjustmentResult;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.InventoryService.InventorySummary;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.InventoryService.LowStockAlert;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.InventoryLogEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

/**
 * Enhanced Inventory Controller for Seller Portal
 */
@RestController
@RequestMapping("/api/v1/seller/inventory")
@Tag(name = "Seller Inventory", description = "Enhanced inventory management for sellers")
@PreAuthorize("hasRole('SELLER')")
public class SellerInventoryController {

    private final InventoryService inventoryService;
    private final SellerShopJpaRepository shopRepo;
    private final SkuJpaRepository skuRepo;
    private final ProductJpaRepository productRepo;

    public SellerInventoryController(
            InventoryService inventoryService,
            SellerShopJpaRepository shopRepo,
            SkuJpaRepository skuRepo,
            ProductJpaRepository productRepo
    ) {
        this.inventoryService = inventoryService;
        this.shopRepo = shopRepo;
        this.skuRepo = skuRepo;
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

    private SellerShopEntity getMyShop() {
        return shopRepo.findBySellerUserId(currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Shop not found"));
    }

    private void validateSkuOwnership(Long skuId) {
        SkuEntity sku = skuRepo.findById(skuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU not found: " + skuId));
        ProductEntity product = productRepo.findById(sku.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found for SKU: " + skuId));
        if (!product.getShopId().equals(getMyShop().getId())) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "Access Denied: You do not own this SKU");
        }
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/summary")
    @Operation(summary = "Get inventory summary", description = "Get stock health overview for dashboard")
    public ResponseEntity<ApiResponse<InventorySummary>> getSummary() {
        SellerShopEntity shop = getMyShop();
        InventorySummary summary = inventoryService.getInventorySummary(shop.getId());
        return ResponseHelper.ok(summary);
    }

    @GetMapping("/low-stock-alerts")
    @Operation(summary = "Get low stock alerts", description = "Get list of products needing restocking")
    public ResponseEntity<ApiResponse<List<LowStockAlert>>> getLowStockAlerts(
            @RequestParam(defaultValue = "10") int threshold
    ) {
        SellerShopEntity shop = getMyShop();
        List<LowStockAlert> alerts = inventoryService.getLowStockAlerts(shop.getId(), threshold);
        return ResponseHelper.ok(alerts);
    }

    // ==================== BATCH OPERATIONS ====================

    @PostMapping("/batch-adjust")
    @Operation(summary = "Batch stock adjustment", description = "Adjust stock for multiple SKUs at once")
    public ResponseEntity<ApiResponse<List<BatchAdjustmentResult>>> batchAdjust(
            @Valid @RequestBody BatchAdjustRequest request
    ) {
        SellerShopEntity shop = getMyShop();
        for (var adj : request.adjustments()) {
            validateSkuOwnership(adj.skuId());
        }
        List<BatchAdjustmentResult> results = inventoryService.batchAdjustStock(
                shop.getId(), 
                request.adjustments(), 
                currentUserId()
        );
        return ResponseHelper.ok(results);
    }

    // ==================== SINGLE ADJUSTMENT ====================

    @PostMapping("/adjust/{skuId}")
    @Operation(summary = "Adjust single SKU stock", description = "Adjust stock for a specific SKU")
    public ResponseEntity<ApiResponse<AdjustmentResponse>> adjustStock(
            @PathVariable Long skuId,
            @Valid @RequestBody SingleAdjustRequest request
    ) {
        validateSkuOwnership(skuId);
        var updated = inventoryService.adjustStock(
                skuId, 
                request.delta(), 
                request.reason(), 
                "MANUAL_ADJUST", 
                currentUserId()
        );
        return ResponseHelper.ok(new AdjustmentResponse(
                skuId, 
                updated.getStockOnHand(), 
                request.delta(), 
                "Stock adjusted successfully"
        ));
    }

    // ==================== HISTORY ====================

    @GetMapping("/history")
    @Operation(summary = "Get shop inventory history", description = "Get all stock changes for the shop")
    public ResponseEntity<ApiResponse<List<InventoryLogEntity>>> getShopHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SellerShopEntity shop = getMyShop();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InventoryLogEntity> history = inventoryService.getShopHistory(shop.getId(), pageable);
        return ResponseHelper.page(history);
    }

    @GetMapping("/history/sku/{skuId}")
    @Operation(summary = "Get SKU inventory history", description = "Get stock changes for a specific SKU")
    public ResponseEntity<ApiResponse<List<InventoryLogEntity>>> getSkuHistory(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validateSkuOwnership(skuId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InventoryLogEntity> history = inventoryService.getHistory(skuId, pageable);
        return ResponseHelper.page(history);
    }

    // ==================== REPORTS ====================

    @GetMapping("/movement-report")
    @Operation(summary = "Get stock movement report", description = "Get summary of stock in/out for a period")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMovementReport(
            @RequestParam(defaultValue = "7") int days
    ) {
        SellerShopEntity shop = getMyShop();
        Map<String, Long> report = inventoryService.getStockMovementSummary(shop.getId(), days);
        return ResponseHelper.ok(report);
    }

    // ==================== Request/Response DTOs ====================

    public record BatchAdjustRequest(
            @NotEmpty(message = "Adjustments list cannot be empty")
            List<BatchAdjustmentRequest> adjustments
    ) {}

    public record SingleAdjustRequest(
            @Min(value = -10000, message = "Delta cannot be less than -10000")
            int delta,
            String reason
    ) {}

    public record AdjustmentResponse(
            Long skuId,
            int newStock,
            int delta,
            String message
    ) {}
}
