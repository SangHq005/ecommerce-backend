package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.SellerProfileResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.SellerProfileService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity.SellerStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Admin Controller for Seller Profile verification management.
 */
@RestController
@RequestMapping("/api/v1/admin/sellers")
@Tag(name = "Admin - Seller Management", description = "Admin endpoints for seller verification")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final SellerProfileService profileService;

    public AdminSellerController(SellerProfileService profileService) {
        this.profileService = profileService;
    }

    private Long currentAdminId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Admin not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid Admin ID");
        }
    }

    @GetMapping
    @Operation(summary = "List seller profiles", description = "Get seller profiles by status")
    public ResponseEntity<ApiResponse<List<SellerProfileResponse>>> listProfiles(
            @RequestParam(defaultValue = "PENDING_VERIFICATION") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        SellerStatus sellerStatus = SellerStatus.valueOf(status);
        Page<SellerProfileResponse> profiles = profileService.getProfilesByStatus(sellerStatus, pageable);
        return ResponseHelper.page(profiles);
    }

    @GetMapping("/pending-count")
    @Operation(summary = "Get pending count", description = "Get count of profiles pending verification")
    public ResponseEntity<ApiResponse<PendingCountResponse>> getPendingCount() {
        long count = profileService.getPendingCount();
        return ResponseHelper.ok(new PendingCountResponse(count));
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get profile details", description = "Get seller profile details by ID")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getProfile(@PathVariable Long profileId) {
        SellerProfileResponse profile = profileService.getProfileById(profileId);
        return ResponseHelper.ok(profile);
    }

    @PostMapping("/{profileId}/approve")
    @Operation(summary = "Approve seller", description = "Approve seller profile verification")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> approve(@PathVariable Long profileId) {
        SellerProfileResponse profile = profileService.approve(profileId, currentAdminId());
        return ResponseHelper.ok(profile, "Seller profile approved successfully");
    }

    @PostMapping("/{profileId}/reject")
    @Operation(summary = "Reject seller", description = "Reject seller profile verification")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> reject(
            @PathVariable Long profileId,
            @RequestParam String reason
    ) {
        SellerProfileResponse profile = profileService.reject(profileId, currentAdminId(), reason);
        return ResponseHelper.ok(profile, "Seller profile rejected");
    }

    @PostMapping("/{profileId}/suspend")
    @Operation(summary = "Suspend seller", description = "Suspend an active seller")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> suspend(
            @PathVariable Long profileId,
            @RequestParam String reason
    ) {
        SellerProfileResponse profile = profileService.suspend(profileId, currentAdminId(), reason);
        return ResponseHelper.ok(profile, "Seller suspended");
    }

    @PostMapping("/{profileId}/reactivate")
    @Operation(summary = "Reactivate seller", description = "Reactivate a suspended seller")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> reactivate(@PathVariable Long profileId) {
        SellerProfileResponse profile = profileService.reactivate(profileId, currentAdminId());
        return ResponseHelper.ok(profile, "Seller reactivated");
    }

    public record PendingCountResponse(long count) {}
}
