package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating/updating seller profile
 */
public record SellerProfileRequest(
    @NotBlank(message = "Full name is required")
    String fullName,
    
    @NotBlank(message = "Seller type is required")
    @Pattern(regexp = "INDIVIDUAL|BUSINESS", message = "Seller type must be INDIVIDUAL or BUSINESS")
    String sellerType,
    
    @NotBlank(message = "ID type is required")
    String idType,  // CCCD, PASSPORT, BUSINESS_LICENSE
    
    @NotBlank(message = "ID number is required")
    String idNumber,
    
    String idImageFront,  // URL to front side of ID card
    
    String idImageBack,   // URL to back side of ID card
    
    String taxCode,  // Required for BUSINESS type
    
    @NotBlank(message = "Contact phone is required")
    String contactPhone,
    
    @Email(message = "Invalid email format")
    String contactEmail,
    
    @NotBlank(message = "City is required")
    String city,
    
    @NotBlank(message = "Address is required")
    String address
) {}
