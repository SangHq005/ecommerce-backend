package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.ecommerce_backend.api.dto.profile.ProfileResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.profile.UpdateProfileRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.storage.ImageUploadService;
import com.example.ecommerce.ecommerce_backend.application.service.user.ProfileService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserProfileEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "User Profile", description = "User profile management")
public class UserProfileController {

    private final ProfileService profileService;
    private final ImageUploadService imageUploadService;

    public UserProfileController(ProfileService profileService, ImageUploadService imageUploadService) {
        this.profileService = profileService;
        this.imageUploadService = imageUploadService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get profile", description = "Get current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> get(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        UserProfileEntity p = profileService.getProfile(userId).orElseGet(() -> {
            UserProfileEntity np = new UserProfileEntity();
            np.setUserId(userId);
            return np;
        });
        return ResponseHelper.ok(toResponse(p));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile", description = "Update current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> update(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest req
    ) {
        Long userId = Long.valueOf(auth.getName());
        UserProfileEntity p = profileService.upsertProfile(userId, req.phone(), req.gender(), req.dateOfBirth());
        return ResponseHelper.ok(toResponse(p), "Profile updated successfully");
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    @Operation(summary = "Upload avatar", description = "Upload user avatar image")
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadAvatar(
            Authentication auth,
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = Long.valueOf(auth.getName());
        var uploadRes = imageUploadService.uploadUserAvatar(file);
        UserProfileEntity p = profileService.updateAvatar(userId, uploadRes.fileUrl());
        return ResponseHelper.ok(toResponse(p), "Avatar uploaded successfully");
    }

    private ProfileResponse toResponse(UserProfileEntity p) {
        return new ProfileResponse(p.getUserId(), p.getPhone(), p.getGender(), p.getDateOfBirth(), p.getAvatarUrl());
    }
}
