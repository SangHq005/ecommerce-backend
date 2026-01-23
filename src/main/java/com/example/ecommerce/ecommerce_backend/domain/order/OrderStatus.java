package com.example.ecommerce.ecommerce_backend.domain.order;

/**
 * Order Status Enum - Shopee-like Order Fulfillment Flow
 * 
 * Flow: SUBMITTED → PAYMENT_PENDING → PAID → TO_SHIP → PROCESSING → READY_TO_SHIP 
 *       → SHIPPED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED → COMPLETED
 * 
 * Cancellation possible until SHIPPED
 * Return/Refund possible after DELIVERED
 */
public enum OrderStatus {
    // === Payment Phase ===
    SUBMITTED,              // Order submitted, awaiting payment
    PAYMENT_PENDING,        // Payment initiated but not confirmed
    PAID,                   // Payment confirmed, awaiting seller action
    
    // === Fulfillment Phase ===
    TO_SHIP,                // NEW: Waiting for seller to arrange shipping
    PROCESSING,             // Seller is processing/packing the order
    READY_TO_SHIP,          // Order packed and ready for pickup by courier
    
    // === Shipping Phase ===
    SHIPPED,                // Order handed to courier
    IN_TRANSIT,             // NEW: Order in transit to destination
    OUT_FOR_DELIVERY,       // NEW: Courier is delivering to customer
    DELIVERY_FAILED,        // NEW: Delivery attempt failed
    DELIVERED,              // Order delivered successfully
    
    // === Completion Phase ===
    COMPLETED,              // Order completed (buyer confirmed or auto-complete)
    
    // === Cancellation ===
    CANCELLED,              // Order cancelled
    
    // === Return/Refund Phase ===
    RETURN_REQUESTED,       // NEW: Buyer requested return
    RETURN_APPROVED,        // NEW: Return request approved
    RETURN_IN_TRANSIT,      // NEW: Return package in transit to seller
    RETURN_COMPLETED,       // NEW: Return received by seller
    REFUND_REQUESTED,       // Customer requested refund (without return)
    REFUND_APPROVED,        // NEW: Refund approved, processing payment
    REFUNDED,               // Order refunded
    
    // === Legacy ===
    FULFILLED;              // Legacy status for backward compatibility
    
    /**
     * Check if order can be cancelled by buyer
     */
    public boolean canBuyerCancel() {
        return this == SUBMITTED || this == PAYMENT_PENDING;
    }
    
    /**
     * Check if order can be cancelled by seller
     */
    public boolean canSellerCancel() {
        return this == PAID || this == TO_SHIP || this == PROCESSING || this == READY_TO_SHIP;
    }
    
    /**
     * Check if order is in shipping phase
     */
    public boolean isInShipping() {
        return this == SHIPPED || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }
    
    /**
     * Check if order can request return/refund
     */
    public boolean canRequestReturn() {
        return this == DELIVERED;
    }
    
    /**
     * Check if order is terminal (no more transitions)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }
}
