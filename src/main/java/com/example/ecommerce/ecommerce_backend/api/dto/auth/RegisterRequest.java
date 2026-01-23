package com.example.ecommerce.ecommerce_backend.api.dto.auth;

import com.example.ecommerce.ecommerce_backend.api.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(max = 72) @StrongPassword String password,
        @NotBlank @Size(min = 2, max = 191) String fullName
) {}
