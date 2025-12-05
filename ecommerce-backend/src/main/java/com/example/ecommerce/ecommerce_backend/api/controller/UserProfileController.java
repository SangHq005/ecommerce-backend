package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.profile.*;
import com.example.ecommerce.ecommerce_backend.api.dto.profile.ProfileResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.profile.UpdateProfileRequest;
import com.example.ecommerce.ecommerce_backend.application.service.LocalFileStorageService;
import com.example.ecommerce.ecommerce_backend.application.service.ProfileService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserProfileEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {

    private final ProfileService profileService;
    private final LocalFileStorageService storage;

    public UserProfileController(ProfileService profileService, LocalFileStorageService storage) {
        this.profileService = profileService;
        this.storage = storage;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> get(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        UserProfileEntity p = profileService.getProfile(userId).orElseGet(() -> {
            UserProfileEntity np = new UserProfileEntity();
            np.setUserId(userId);
            return np;
        });
        return ResponseEntity.ok(toResponse(p));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> update(Authentication auth, @Valid @RequestBody UpdateProfileRequest req) {
        Long userId = Long.valueOf(auth.getName());
        UserProfileEntity p = profileService.upsertProfile(userId, req.phone(), req.gender(), req.dateOfBirth());
        return ResponseEntity.ok(toResponse(p));
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> uploadAvatar(Authentication auth, @RequestPart("file") MultipartFile file) {
        Long userId = Long.valueOf(auth.getName());
        String url = storage.save("avatars", file);
        UserProfileEntity p = profileService.updateAvatar(userId, url);
        return ResponseEntity.ok(toResponse(p));
    }

    private ProfileResponse toResponse(UserProfileEntity p) {
        return new ProfileResponse(p.getUserId(), p.getPhone(), p.getGender(), p.getDateOfBirth(), p.getAvatarUrl());
    }
}
