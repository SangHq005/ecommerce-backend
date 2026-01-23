package com.example.ecommerce.ecommerce_backend.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VerifyOtpRequest(
    @NotBlank @JsonProperty("phoneNumber") String phoneNumber,
    @NotBlank @JsonProperty("otp") String otp,
    @JsonProperty("deviceId") String deviceId
) {}
