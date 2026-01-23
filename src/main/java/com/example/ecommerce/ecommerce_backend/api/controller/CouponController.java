package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponValidationResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.ValidateCouponRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.CouponService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = "Coupon validation and listing")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
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

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon", description = "Check if a coupon is valid for the order")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request
    ) {
        CouponValidationResponse response = couponService.validateCoupon(
                request.couponCode(),
                currentUserId(),
                request.orderTotal(),
                request.productIds(),
                request.categoryIds()
        );
        return ResponseHelper.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Active coupons", description = "Get all active coupons")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getActiveCoupons() {
        List<CouponEntity> coupons = couponService.getActiveCoupons();
        List<CouponResponse> responses = coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
        return ResponseHelper.ok(responses);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get coupon", description = "Get coupon details by code")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
        CouponEntity coupon = couponService.getCouponByCode(code);
        return ResponseHelper.ok(CouponResponse.from(coupon));
    }
}
