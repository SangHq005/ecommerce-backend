package com.example.ecommerce.ecommerce_backend.api.dto.order;

import java.time.Instant;
import java.util.Map;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderStatusHistoryEntity;

/**
 * Response DTO for order status history entry.
 */
public record OrderStatusHistoryResponse(
    Long id,
    Long orderId,
    String fromStatus,
    String toStatus,
    String actorType,
    Long actorId,
    String actorName, // Populated from user lookup if needed
    String reason,
    String note,
    Map<String, Object> metadata,
    Instant createdAt,
    // Computed display info
    String statusDisplayName,
    String description
) {
    public static OrderStatusHistoryResponse from(OrderStatusHistoryEntity entity) {
        return from(entity, null);
    }
    
    public static OrderStatusHistoryResponse from(OrderStatusHistoryEntity entity, String actorName) {
        return new OrderStatusHistoryResponse(
            entity.getId(),
            entity.getOrderId(),
            entity.getFromStatus(),
            entity.getToStatus(),
            entity.getActorType().name(),
            entity.getActorId(),
            actorName,
            entity.getReason(),
            entity.getNote(),
            entity.getMetadata(),
            entity.getCreatedAt(),
            getStatusDisplayName(entity.getToStatus()),
            generateDescription(entity)
        );
    }
    
    private static String getStatusDisplayName(String status) {
        return switch (status) {
            case "SUBMITTED" -> "Order Submitted";
            case "PAYMENT_PENDING" -> "Payment Pending";
            case "PAID" -> "Payment Confirmed";
            case "TO_SHIP" -> "To Ship";
            case "PROCESSING" -> "Processing";
            case "READY_TO_SHIP" -> "Ready to Ship";
            case "SHIPPED" -> "Shipped";
            case "IN_TRANSIT" -> "In Transit";
            case "OUT_FOR_DELIVERY" -> "Out for Delivery";
            case "DELIVERY_FAILED" -> "Delivery Failed";
            case "DELIVERED" -> "Delivered";
            case "COMPLETED" -> "Completed";
            case "CANCELLED" -> "Cancelled";
            case "RETURN_REQUESTED" -> "Return Requested";
            case "RETURN_APPROVED" -> "Return Approved";
            case "RETURN_IN_TRANSIT" -> "Return In Transit";
            case "RETURN_COMPLETED" -> "Return Completed";
            case "REFUND_REQUESTED" -> "Refund Requested";
            case "REFUND_APPROVED" -> "Refund Approved";
            case "REFUNDED" -> "Refunded";
            default -> status;
        };
    }
    
    private static String generateDescription(OrderStatusHistoryEntity entity) {
        String actor = switch (entity.getActorType()) {
            case SYSTEM -> "System";
            case BUYER -> "Buyer";
            case SELLER -> "Seller";
            case ADMIN -> "Admin";
        };
        
        String action = getStatusDisplayName(entity.getToStatus());
        String desc = actor + " changed status to " + action;
        
        if (entity.getReason() != null && !entity.getReason().isBlank()) {
            desc += ": " + entity.getReason();
        }
        
        return desc;
    }
}
