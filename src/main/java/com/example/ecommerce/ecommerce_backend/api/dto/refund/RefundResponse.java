package com.example.ecommerce.ecommerce_backend.api.dto.refund;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefundEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record RefundResponse(
        Long id,
        Long orderId,
        String orderCode,
        Long userId,
        String userEmail,
        Long shopId,
        String shopName,
        String reason,
        String description,
        Long refundAmount,
        String currency,
        String status,
        String adminNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime processedAt
) {
    public static RefundResponse from(RefundEntity refund, String orderCode, String userEmail, String shopName) {
        LocalDateTime createdAtLocal = refund.getCreatedAt() != null
                ? LocalDateTime.ofInstant(refund.getCreatedAt(), ZoneId.systemDefault())
                : null;

        LocalDateTime updatedAtLocal = refund.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(refund.getUpdatedAt(), ZoneId.systemDefault())
                : null;

        LocalDateTime processedAtLocal = refund.getProcessedAt() != null
                ? LocalDateTime.ofInstant(refund.getProcessedAt(), ZoneId.systemDefault())
                : null;

        return new RefundResponse(
                refund.getId(),
                refund.getOrderId(),
                orderCode,
                refund.getUserId(),
                userEmail,
                refund.getShopId(),
                shopName,
                refund.getReason(),
                refund.getDescription(),
                refund.getRefundAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getAdminNote(),
                createdAtLocal,
                updatedAtLocal,
                processedAtLocal
        );
    }
}
