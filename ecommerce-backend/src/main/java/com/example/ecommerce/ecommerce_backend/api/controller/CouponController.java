package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponValidationResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.ValidateCouponRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.CouponService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID");
        }
    }

    /**
     * Validate coupon code
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(@Valid @RequestBody ValidateCouponRequest request) {
        CouponValidationResponse response = couponService.validateCoupon(
                request.couponCode(),
                currentUserId(),
                request.orderTotal(),
                request.productIds(),
                request.categoryIds()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active coupons
     */
    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getActiveCoupons() {
        List<CouponEntity> coupons = couponService.getActiveCoupons();
        List<CouponResponse> responses = coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get coupon by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        CouponEntity coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(CouponResponse.from(coupon));
    }
}
