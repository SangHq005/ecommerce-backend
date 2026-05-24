package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.shop.SuspendRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.admin.AdminShopService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/shops")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Shops", description = "Admin shop management")
public class AdminShopController {

    private final AdminShopService adminShopService;

    public AdminShopController(AdminShopService adminShopService) {
        this.adminShopService = adminShopService;
    }
    
    private Long currentAdminId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }

    @GetMapping
    @Operation(summary = "List by status", description = "List shops by status")
    public ResponseEntity<ApiResponse<List<SellerShopEntity>>> list(@RequestParam String status) {
        List<SellerShopEntity> shops = adminShopService.listByStatus(status);
        return ResponseHelper.ok(shops);
    }

    @PostMapping("/{shopId}/approve")
    @Operation(summary = "Approve shop", description = "Approve a pending shop")
    public ResponseEntity<ApiResponse<SellerShopEntity>> approve(
            Authentication auth,
            @PathVariable Long shopId
    ) {
        SellerShopEntity shop = adminShopService.approve(shopId, currentAdminId(auth));
        return ResponseHelper.ok(shop, "Shop approved");
    }

    @PostMapping("/{shopId}/suspend")
    @Operation(summary = "Suspend shop", description = "Suspend a shop")
    public ResponseEntity<ApiResponse<SellerShopEntity>> suspend(
            Authentication auth,
            @PathVariable Long shopId,
            @Valid @RequestBody SuspendRequest req
    ) {
        SellerShopEntity shop = adminShopService.suspend(shopId, req.reason(), currentAdminId(auth));
        return ResponseHelper.ok(shop, "Shop suspended");
    }

    @PostMapping("/{shopId}/reject")
    @Operation(summary = "Reject shop", description = "Reject a pending shop")
    public ResponseEntity<ApiResponse<SellerShopEntity>> reject(
            Authentication auth,
            @PathVariable Long shopId,
            @Valid @RequestBody SuspendRequest req
    ) {
        SellerShopEntity shop = adminShopService.reject(shopId, req.reason(), currentAdminId(auth));
        return ResponseHelper.ok(shop, "Shop rejected");
    }
    
    @PostMapping("/{shopId}/reactivate")
    @Operation(summary = "Reactivate shop", description = "Reactivate a suspended shop")
    public ResponseEntity<ApiResponse<SellerShopEntity>> reactivate(
            Authentication auth,
            @PathVariable Long shopId
    ) {
        SellerShopEntity shop = adminShopService.reactivate(shopId, currentAdminId(auth));
        return ResponseHelper.ok(shop, "Shop reactivated");
    }
}
