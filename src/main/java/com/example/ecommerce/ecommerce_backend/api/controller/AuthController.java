package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.auth.*;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.LoginRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.RefreshRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.RegisterRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.TokenResponse;
import com.example.ecommerce.ecommerce_backend.application.service.AuthService;
import com.example.ecommerce.ecommerce_backend.application.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest http) {
        JwtService.TokenPair pair = auth.registerClient(
                req.email(), req.password(), req.fullName(),
                http.getHeader("User-Agent"), http.getRemoteAddr()
        );
        return ResponseEntity.ok(new TokenResponse(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        JwtService.TokenPair pair = auth.login(req.email(), req.password(), http.getHeader("User-Agent"), http.getRemoteAddr());
        return ResponseEntity.ok(new TokenResponse(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        JwtService.TokenPair pair = auth.refreshRotate(req.refreshToken(), http.getHeader("User-Agent"), http.getRemoteAddr());
        return ResponseEntity.ok(new TokenResponse(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authz,
            @RequestBody(required = false) RefreshRequest body
    ) {
        Long userId = Long.valueOf(authentication.getName());
        String access = (authz != null && authz.startsWith("Bearer ")) ? authz.substring(7) : null;
        String refresh = (body != null) ? body.refreshToken() : null;
        auth.logout(userId, access, refresh);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();

        return ResponseEntity.ok(new MeResponse(authentication.getName(), roles));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        auth.initiatePasswordReset(req.email());
        return ResponseEntity.ok(new MessageResponse("Password reset email sent. Please check your inbox."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        auth.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully. Please login with your new password."));
    }

    public record MeResponse(String userId, java.util.List<String> roles) {}
    public record MessageResponse(String message) {}

}
