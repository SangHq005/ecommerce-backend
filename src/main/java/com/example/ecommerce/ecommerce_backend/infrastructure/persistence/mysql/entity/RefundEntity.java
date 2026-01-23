package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refund")
public class RefundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Lob
    private String description;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Column(nullable = false, length = 8)
    private String currency = "VND";

    @Column(nullable = false, length = 32)
    private String status; // PENDING, UNDER_REVIEW, APPROVED, REJECTED, PROCESSING, COMPLETED, CANCELLED

    @Column(name = "admin_note", length = 1000)
    private String adminNote;
    
    // NEW: Distinguish between REFUND and RETURN
    @Column(name = "refund_type", length = 20)
    private String refundType = "REFUND"; // REFUND or RETURN
    
    // NEW: Return tracking info
    @Column(name = "return_tracking_number", length = 100)
    private String returnTrackingNumber;
    
    @Column(name = "return_shipping_provider", length = 50)
    private String returnShippingProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    @Version
    private long version;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Long refundAmount) { this.refundAmount = refundAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    
    // NEW getters/setters
    public String getRefundType() { return refundType; }
    public void setRefundType(String refundType) { this.refundType = refundType; }
    
    public String getReturnTrackingNumber() { return returnTrackingNumber; }
    public void setReturnTrackingNumber(String returnTrackingNumber) { this.returnTrackingNumber = returnTrackingNumber; }
    
    public String getReturnShippingProvider() { return returnShippingProvider; }
    public void setReturnShippingProvider(String returnShippingProvider) { this.returnShippingProvider = returnShippingProvider; }
}
