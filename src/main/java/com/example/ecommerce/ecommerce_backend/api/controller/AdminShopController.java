package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.shop.SuspendRequest;
import com.example.ecommerce.ecommerce_backend.application.service.AdminShopService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shops")
public class AdminShopController {

    private final AdminShopService adminShopService;

    public AdminShopController(AdminShopService adminShopService) {
        this.adminShopService = adminShopService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SellerShopEntity> list(@RequestParam String status) {
        return adminShopService.listByStatus(status);
    }

    @PostMapping("/{shopId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public SellerShopEntity approve(@PathVariable Long shopId) {
        return adminShopService.approve(shopId);
    }

    @PostMapping("/{shopId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public SellerShopEntity suspend(@PathVariable Long shopId, @Valid @RequestBody SuspendRequest req) {
        return adminShopService.suspend(shopId, req.reason());
    }
}
