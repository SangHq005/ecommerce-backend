package com.example.ecommerce.ecommerce_backend.api.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max=191) String receiverName,
        @NotBlank @Size(max=32) String receiverPhone,
        @NotBlank @Size(max=255) String line1,
        @Size(max=255) String line2,
        String ward,
        String district,
        String province,
        String postalCode,
        String addressType,
        Boolean isDefault
) {}
