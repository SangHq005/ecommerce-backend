package com.example.ecommerce.ecommerce_backend.api.dto.auth;

import com.example.ecommerce.ecommerce_backend.api.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Size(max = 100, message = "Password must not exceed 100 characters")
        @StrongPassword
        String newPassword
) {
}
