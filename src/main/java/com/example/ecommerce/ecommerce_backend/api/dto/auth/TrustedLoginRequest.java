package com.example.ecommerce.ecommerce_backend.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TrustedLoginRequest(
    @NotBlank @JsonProperty("phoneNumber") String phoneNumber,
    @NotBlank @JsonProperty("deviceId") String deviceId
) {}
