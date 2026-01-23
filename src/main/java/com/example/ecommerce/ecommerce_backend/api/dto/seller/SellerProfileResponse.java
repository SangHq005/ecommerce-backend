package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.Instant;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity;

/**
 * Response DTO for seller profile
 */
public record SellerProfileResponse(
    Long id,
    Long userId,
    String status,
    String sellerType,
    String fullName,
    String idType,
    String idNumber,
    String idImageFront,
    String idImageBack,
    String taxCode,
    String contactPhone,
    String contactEmail,
    String city,
    String address,
    Instant submittedAt,
    Instant verifiedAt,
    Instant rejectedAt,
    String rejectedReason,
    Instant createdAt,
    boolean canCreateShop
) {
    public static SellerProfileResponse from(SellerProfileEntity entity) {
        return new SellerProfileResponse(
            entity.getId(),
            entity.getUserId(),
            entity.getStatus().name(),
            entity.getSellerType().name(),
            entity.getFullName(),
            entity.getIdType(),
            maskIdNumber(entity.getIdNumber()),
            entity.getIdImageFront(),
            entity.getIdImageBack(),
            entity.getTaxCode(),
            entity.getContactPhone(),
            entity.getContactEmail(),
            entity.getCity(),
            entity.getAddress(),
            entity.getSubmittedAt(),
            entity.getVerifiedAt(),
            entity.getRejectedAt(),
            entity.getRejectedReason(),
            entity.getCreatedAt(),
            entity.canCreateShop()
        );
    }
    
    /**
     * Mask ID number for privacy (show only last 4 digits)
     */
    private static String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() <= 4) {
            return idNumber;
        }
        return "*".repeat(idNumber.length() - 4) + idNumber.substring(idNumber.length() - 4);
    }
}
