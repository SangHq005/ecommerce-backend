package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CreateCouponRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.coupon.UpdateCouponStatusRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@Tag(name = "Admin Coupons", description = "Admin coupon management")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    @Operation(summary = "List coupons", description = "List coupons with filters")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> listCoupons(
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) Boolean autoApply,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CouponEntity> pageEntities = couponService.adminSearch(status, autoApply, q, pageable);
        List<CouponResponse> content = pageEntities.stream().map(CouponResponse::from).toList();
        Page<CouponResponse> responses = new PageImpl<>(content, pageable, pageEntities.getTotalElements());
        return ResponseHelper.page(responses);
    }

    @PostMapping
    @Operation(summary = "Create coupon", description = "Create a new coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
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
        return ResponseHelper.created(CouponResponse.from(created), "Coupon created");
    }

    @PutMapping("/{couponId}")
    @Operation(summary = "Update coupon", description = "Update an existing coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
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
        return ResponseHelper.ok(CouponResponse.from(updated), "Coupon updated");
    }

    @DeleteMapping("/{couponId}")
    @Operation(summary = "Delete coupon", description = "Delete a coupon")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseHelper.ok(null, "Coupon deleted");
    }

    @PutMapping("/{couponId}/status")
    @Operation(summary = "Update status", description = "Update coupon status")
    public ResponseEntity<ApiResponse<CouponResponse>> updateStatus(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponStatusRequest request
    ) {
        CouponEntity updated = couponService.updateStatus(couponId, request.status());
        return ResponseHelper.ok(CouponResponse.from(updated), "Coupon status updated");
    }
}
