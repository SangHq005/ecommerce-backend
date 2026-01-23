package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.SellerProfileRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.SellerProfileResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.upload.UploadResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.ImageUploadService;
import com.example.ecommerce.ecommerce_backend.application.service.SellerProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for Seller Profile management.
 * Handles seller registration and verification status.
 */
@RestController
@RequestMapping("/api/v1/seller/profile")
@Tag(name = "Seller Profile", description = "Seller profile and verification management")
@PreAuthorize("hasRole('SELLER')")
public class SellerProfileController {

    private final SellerProfileService profileService;
    private final ImageUploadService uploadService;

    public SellerProfileController(SellerProfileService profileService, ImageUploadService uploadService) {
        this.profileService = profileService;
        this.uploadService = uploadService;
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

    @GetMapping
    @Operation(summary = "Get my seller profile", description = "Get current user's seller profile and verification status")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getMyProfile() {
        var profile = profileService.getMyProfile(currentUserId());
        if (profile.isEmpty()) {
            return ResponseHelper.ok(null, "No seller profile found. Please submit for verification.");
        }
        return ResponseHelper.ok(profile.get());
    }

    @PostMapping
    @Operation(summary = "Submit seller profile", description = "Create or update seller profile for verification")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> submitProfile(
            @Valid @RequestBody SellerProfileRequest request
    ) {
        SellerProfileResponse profile = profileService.createOrUpdateProfile(currentUserId(), request);
        return ResponseHelper.ok(profile, "Seller profile submitted for verification");
    }

    @GetMapping("/status")
    @Operation(summary = "Check verification status", description = "Check if seller profile is verified and can create shop")
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> checkStatus() {
        Long userId = currentUserId();
        var profile = profileService.getMyProfile(userId);
        
        boolean hasProfile = profile.isPresent();
        String status = hasProfile ? profile.get().status() : "NOT_SUBMITTED";
        boolean canCreateShop = profileService.canCreateShop(userId);
        String rejectedReason = hasProfile ? profile.get().rejectedReason() : null;
        
        return ResponseHelper.ok(new VerificationStatusResponse(
                hasProfile,
                status,
                canCreateShop,
                rejectedReason
        ));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'SELLER')")
    @Operation(summary = "Upload seller document", description = "Upload ID card or business license document for seller verification. Available for CLIENT and SELLER roles.")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) {
        // Use product image upload service (same validation and storage)
        UploadResponse response = uploadService.uploadProductImage(file);
        return ResponseHelper.created(response, "Document uploaded successfully");
    }

    /**
     * Response for verification status check
     */
    public record VerificationStatusResponse(
            boolean hasProfile,
            String status,
            boolean canCreateShop,
            String rejectedReason
    ) {}
}
