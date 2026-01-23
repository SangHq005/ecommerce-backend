package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_code", nullable=false, unique=true)
    private String orderCode;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="shop_id", nullable=false)
    private Long shopId;

    @Column(nullable=false)
    private String status;

    @Column(name="total_amount", nullable=false)
    private Long totalAmount;

    @Column(nullable=false)
    private String currency;

    @Column(name="address_id")
    private Long addressId;

    @Column(name="payment_method", length=32)
    private String paymentMethod;

    @Column(columnDefinition="TEXT")
    private String note;

    @Column(name="coupon_code", length=64)
    private String couponCode;

    @Column(name="discount_amount", nullable=false)
    private Long discountAmount = 0L;

    @Column(name="shipping_fee", nullable=false)
    private Long shippingFee = 0L;

    @Column(name="tracking_number", length=64)
    private String trackingNumber;
    
    // === NEW: Shipping Provider Fields ===
    @Column(name="shipping_provider", length=50)
    private String shippingProvider;  // GHN, GHTK, VNPost, JT Express, etc.
    
    @Column(name="shipping_tracking_url", length=512)
    private String shippingTrackingUrl;
    
    // === NEW: Timestamp Fields ===
    @Column(name="shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name="delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name="completed_at")
    private LocalDateTime completedAt;
    
    @Column(name="estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    // === NEW: Delivery Tracking ===
    @Column(name="delivery_attempts", nullable=false)
    private Integer deliveryAttempts = 0;
    
    @Column(name="delivery_failed_reason", length=255)
    private String deliveryFailedReason;
    
    // === NEW: Buyer Confirmation ===
    @Column(name="buyer_confirmed", nullable=false)
    private Boolean buyerConfirmed = false;
    
    @Column(name="buyer_confirmed_at")
    private LocalDateTime buyerConfirmedAt;
    
    // === NEW: Auto-complete Scheduling ===
    @Column(name="auto_complete_at")
    private LocalDateTime autoCompleteAt;  // Set to delivered_at + 7 days

    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters/setters omitted for brevity
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    
    public Long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Long discountAmount) { this.discountAmount = discountAmount; }
    
    public Long getShippingFee() { return shippingFee; }
    public void setShippingFee(Long shippingFee) { this.shippingFee = shippingFee; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    // === NEW: Shipping Provider Getters/Setters ===
    public String getShippingProvider() { return shippingProvider; }
    public void setShippingProvider(String shippingProvider) { this.shippingProvider = shippingProvider; }
    
    public String getShippingTrackingUrl() { return shippingTrackingUrl; }
    public void setShippingTrackingUrl(String shippingTrackingUrl) { this.shippingTrackingUrl = shippingTrackingUrl; }
    
    // === NEW: Timestamp Getters/Setters ===
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    
    // === NEW: Delivery Tracking Getters/Setters ===
    public Integer getDeliveryAttempts() { return deliveryAttempts; }
    public void setDeliveryAttempts(Integer deliveryAttempts) { this.deliveryAttempts = deliveryAttempts; }
    
    public String getDeliveryFailedReason() { return deliveryFailedReason; }
    public void setDeliveryFailedReason(String deliveryFailedReason) { this.deliveryFailedReason = deliveryFailedReason; }
    
    // === NEW: Buyer Confirmation Getters/Setters ===
    public Boolean getBuyerConfirmed() { return buyerConfirmed; }
    public void setBuyerConfirmed(Boolean buyerConfirmed) { this.buyerConfirmed = buyerConfirmed; }
    
    public LocalDateTime getBuyerConfirmedAt() { return buyerConfirmedAt; }
    public void setBuyerConfirmedAt(LocalDateTime buyerConfirmedAt) { this.buyerConfirmedAt = buyerConfirmedAt; }
    
    // === NEW: Auto-complete Getters/Setters ===
    public LocalDateTime getAutoCompleteAt() { return autoCompleteAt; }
    public void setAutoCompleteAt(LocalDateTime autoCompleteAt) { this.autoCompleteAt = autoCompleteAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
