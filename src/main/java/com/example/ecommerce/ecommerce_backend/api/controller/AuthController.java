package com.example.ecommerce.ecommerce_backend.api.controller;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserProfileEntity;
import com.example.ecommerce.ecommerce_backend.api.annotation.RateLimit;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.ForgotPasswordRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.LoginRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.RefreshRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.RegisterRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.ResetPasswordRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.SendOtpRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.TokenResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.TrustedLoginRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.VerifyOtpRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.auth.AuthService;
import com.example.ecommerce.ecommerce_backend.application.service.auth.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and authorization")
public class AuthController {

    private final AuthService auth;
    
    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @RateLimit(limit = 5, window = 15, unit = ChronoUnit.MINUTES)
    @Operation(summary = "Register new user", description = "Register a new client account")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletRequest http
    ) {
        JwtService.TokenPair pair = auth.registerClient(
                req.email(), req.password(), req.fullName(),
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        TokenResponse response = new TokenResponse(
                pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()
        );
        return ResponseHelper.created(response, "Registration successful");
    }

    @PostMapping("/login")
    @RateLimit(limit = 100, window = 15, unit = ChronoUnit.MINUTES) // Increased for dev
    @Operation(summary = "User login", description = "Authenticate with email and password")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest http
    ) {
        JwtService.TokenPair pair = auth.login(
                req.email(), req.password(),
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        TokenResponse response = new TokenResponse(
                pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()
        );
        return ResponseHelper.ok(response, "Login successful");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshRequest req,
            HttpServletRequest http
    ) {
        JwtService.TokenPair pair = auth.refreshRotate(
                req.refreshToken(),
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        TokenResponse response = new TokenResponse(
                pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()
        );
        return ResponseHelper.ok(response, "Token refreshed successfully");
    }

    @PostMapping("/otp/send")
    @RateLimit(limit = 100, window = 5, unit = ChronoUnit.MINUTES)
    @Operation(summary = "Send OTP", description = "Send OTP to phone number")
    public ResponseEntity<ApiResponse<MessageResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest req
    ) {
        auth.sendOtp(req.phoneNumber());
        return ResponseHelper.ok(
                new MessageResponse("OTP sent successfully"),
                "OTP sent"
        );
    }

    @PostMapping("/otp/verify")
    @RateLimit(limit = 100, window = 15, unit = ChronoUnit.MINUTES)
    @Operation(summary = "Verify OTP", description = "Verify OTP and login")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest req,
            HttpServletRequest http
    ) {
        JwtService.TokenPair pair = auth.verifyOtpAndLogin(
                req.phoneNumber(), req.otp(), req.deviceId(),
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        TokenResponse response = new TokenResponse(
                pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()
        );
        return ResponseHelper.ok(response, "Login successful");
    }

    @PostMapping("/trusted-login")
    @RateLimit(limit = 10, window = 15, unit = ChronoUnit.MINUTES)
    @Operation(summary = "Trusted device login", description = "Login with trusted device ID")
    public ResponseEntity<ApiResponse<TokenResponse>> trustedLogin(
            @Valid @RequestBody TrustedLoginRequest req,
            HttpServletRequest http
    ) {
        JwtService.TokenPair pair = auth.trustedDeviceLogin(
                req.phoneNumber(), req.deviceId(),
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        TokenResponse response = new TokenResponse(
                pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()
        );
        return ResponseHelper.ok(response, "Login successful");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate current session")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authz,
            @RequestBody(required = false) RefreshRequest body
    ) {
        Long userId = Long.valueOf(authentication.getName());
        String access = (authz != null && authz.startsWith("Bearer ")) ? authz.substring(7) : null;
        String refresh = (body != null) ? body.refreshToken() : null;
        auth.logout(userId, access, refresh);
        return ResponseHelper.okNoContent();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user info")
    public ResponseEntity<ApiResponse<MeResponse>> me(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        var user = auth.getUserProfile(userId);
        var profile = auth.getUserProfileDetails(userId);
        
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().replaceFirst("^ROLE_", ""))
                .toList();

        return ResponseHelper.ok(new MeResponse(
            String.valueOf(user.getId()), 
            user.getEmail(), 
            user.getFullName(), 
            user.getPhoneNumber(),
            profile.map(p -> p.getGender() != null ? p.getGender().name() : null).orElse(null),
            profile.map(UserProfileEntity::getDateOfBirth).orElse(null),
            profile.map(UserProfileEntity::getAvatarUrl).orElse(null),
            roles
        ));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar", description = "Upload user avatar image")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Create uploads directory if not exists
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                 extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + System.currentTimeMillis() + extension;
            
            // Save file
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Return URL
            String url = "/files/" + filename;
            return ResponseHelper.ok(url);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @PostMapping("/update")
    @Operation(summary = "Update profile", description = "Update user profile information")
    public ResponseEntity<ApiResponse<MeResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        
        java.time.LocalDate dob = null;
        if (req.dateOfBirth() != null && !req.dateOfBirth().isBlank()) {
            try {
                dob = java.time.LocalDate.parse(req.dateOfBirth());
            } catch (java.time.format.DateTimeParseException e) {
                // throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd");
                // or just ignore
            }
        }

        var user = auth.updateProfile(userId, req.fullName(), req.phoneNumber(), req.gender(), dob, req.avatarUrl());
        var profile = auth.getUserProfileDetails(userId);
        
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().replaceFirst("^ROLE_", ""))
                .toList();

        return ResponseHelper.ok(new MeResponse(
            String.valueOf(user.getId()), 
            user.getEmail(), 
            user.getFullName(), 
            user.getPhoneNumber(),
            profile.map(p -> p.getGender() != null ? p.getGender().name() : null).orElse(null),
            profile.map(UserProfileEntity::getDateOfBirth).orElse(null),
            profile.map(UserProfileEntity::getAvatarUrl).orElse(null),
            roles
        ));
    }

    @PostMapping("/register-seller")
    @Operation(summary = "Register as Seller", description = "Upgrade current user account to seller")
    public ResponseEntity<ApiResponse<TokenResponse>> registerAsSeller(
            Authentication authentication,
            HttpServletRequest http
    ) {
        Long userId = Long.valueOf(authentication.getName());
        JwtService.TokenPair pair = auth.registerSeller(
                userId,
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        TokenResponse response = new TokenResponse(
                pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()
        );
        return ResponseHelper.ok(response, "Registered as seller successfully");
    }

    @PostMapping("/forgot-password")
    @RateLimit(limit = 3, window = 15, unit = ChronoUnit.MINUTES)
    @Operation(summary = "Forgot password", description = "Request password reset email")
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req
    ) {
        auth.initiatePasswordReset(req.email());
        return ResponseHelper.ok(
                new MessageResponse("Password reset email sent. Please check your inbox."),
                "Password reset initiated"
        );
    }

    @PostMapping("/reset-password")
    @RateLimit(limit = 5, window = 15, unit = ChronoUnit.MINUTES)
    @Operation(summary = "Reset password", description = "Reset password with token from email")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req
    ) {
        auth.resetPassword(req.token(), req.newPassword());
        return ResponseHelper.ok(
                new MessageResponse("Password has been reset successfully. Please login with your new password."),
                "Password reset successful"
        );
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<MessageResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        auth.changePassword(userId, req.currentPassword(), req.newPassword());
        return ResponseHelper.ok(
                new MessageResponse("Password has been changed successfully."),
                "Password changed successfully"
        );
    }

    // ========== RESPONSE DTOs ==========

    public record MeResponse(String userId, String email, String fullName, String phoneNumber, String gender, java.time.LocalDate dateOfBirth, String avatarUrl, List<String> roles) {}
    
    public record UpdateProfileRequest(String fullName, String phoneNumber, String gender, String dateOfBirth, String avatarUrl) {}

    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    public record MessageResponse(String message) {}
}
