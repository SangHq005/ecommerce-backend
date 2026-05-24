package com.example.ecommerce.ecommerce_backend.api.dto.voucher;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for duplicating an existing voucher.
 */
public record DuplicateVoucherRequest(
    @NotBlank(message = "New voucher code is required")
    @Size(min = 3, max = 50, message = "Code must be 3-50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code can only contain uppercase letters, numbers, underscore and hyphen")
    String code,
    
    @NotBlank(message = "Voucher name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name,
    
    @NotNull(message = "Start date is required")
    Instant startDate,
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    Instant endDate
) {}
