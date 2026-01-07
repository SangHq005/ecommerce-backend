package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.MessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CreateCouponRequest;
import com.example.ecommerce.ecommerce_backend.application.service.CouponService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    /**
     * Create new coupon
     */
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponEntity coupon = new CouponEntity();
        coupon.setCode(request.code());
        coupon.setName(request.name());
        coupon.setDescription(request.description());
        coupon.setType(request.type());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setMinOrderAmount(request.minOrderAmount());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsageLimitPerUser(request.usageLimitPerUser());
        coupon.setAutoApply(request.autoApply() != null ? request.autoApply() : false);
        coupon.setApplicableProductIds(request.applicableProductIds());
        coupon.setApplicableCategoryIds(request.applicableCategoryIds());
        coupon.setApplicableUserIds(request.applicableUserIds());

        CouponEntity created = couponService.createCoupon(coupon);
        return ResponseEntity.ok(CouponResponse.from(created));
    }

    /**
     * Update coupon
     */
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CreateCouponRequest request
    ) {
        CouponEntity updates = new CouponEntity();
        updates.setCode(request.code());
        updates.setName(request.name());
        updates.setDescription(request.description());
        updates.setType(request.type());
        updates.setDiscountValue(request.discountValue());
        updates.setMaxDiscountAmount(request.maxDiscountAmount());
        updates.setMinOrderAmount(request.minOrderAmount());
        updates.setStartDate(request.startDate());
        updates.setEndDate(request.endDate());
        updates.setUsageLimit(request.usageLimit());
        updates.setUsageLimitPerUser(request.usageLimitPerUser());
        updates.setAutoApply(request.autoApply() != null ? request.autoApply() : false);
        updates.setApplicableProductIds(request.applicableProductIds());
        updates.setApplicableCategoryIds(request.applicableCategoryIds());
        updates.setApplicableUserIds(request.applicableUserIds());

        CouponEntity updated = couponService.updateCoupon(couponId, updates);
        return ResponseEntity.ok(CouponResponse.from(updated));
    }

    /**
     * Delete coupon
     */
    @DeleteMapping("/{couponId}")
    public ResponseEntity<MessageResponse> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.ok(new MessageResponse("Coupon deleted successfully"));
    }
}
