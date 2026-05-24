package com.example.ecommerce.ecommerce_backend.domain.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Order {
    private final String orderCode;
    private final Long userId;
    private final Long shopId;
    private OrderStatus status;
    private long totalAmount;
    private final String currency;
    private long discountAmount;
    private final long shippingFee;
    private final List<OrderItem> items;

    @Builder
    public Order(String orderCode, Long userId, Long shopId, OrderStatus status, 
                 String currency, long shippingFee, long discountAmount, List<OrderItem> items) {
        this.orderCode = orderCode;
        this.userId = userId;
        this.shopId = shopId;
        this.status = status != null ? status : OrderStatus.SUBMITTED;
        this.currency = currency;
        this.shippingFee = shippingFee;
        this.discountAmount = discountAmount;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        calculateTotals();
    }

    /**
     * Invariant: Total amount must be calculated correctly based on items, shipping, and discounts.
     * Invariant: Total amount cannot be negative.
     */
    protected void calculateTotals() {
        long subtotal = items.stream().mapToLong(OrderItem::getTotalPrice).sum();
        this.totalAmount = Math.max(0, subtotal + shippingFee - discountAmount);
    }

    /**
     * Invariant: Only SUBMITTED or PAYMENT_PENDING orders can be cancelled by User.
     */
    public void cancelByUser() {
        if (!this.status.canBuyerCancel()) {
            throw ApiException.conflict("User can only cancel orders in SUBMITTED or PAYMENT_PENDING status. Current: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Invariant: Seller can cancel order if it hasn't been shipped yet.
     */
    public void cancelBySeller() {
        if (!this.status.canSellerCancel()) {
            throw ApiException.conflict("Seller can only cancel orders before shipping. Current status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Invariant: Transition to Payment Pending.
     */
    public void markAsPaymentPending() {
        transitionTo(OrderStatus.PAYMENT_PENDING);
    }
    
    /**
     * Confirm payment received - move to TO_SHIP
     */
    public void confirmPayment() {
        transitionTo(OrderStatus.PAID);
    }
    
    /**
     * Seller starts processing the order
     */
    public void startProcessing() {
        transitionTo(OrderStatus.PROCESSING);
    }
    
    /**
     * Seller marks order as ready for shipping pickup
     */
    public void markReadyToShip() {
        transitionTo(OrderStatus.READY_TO_SHIP);
    }
    
    /**
     * Order handed to courier
     */
    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }
    
    /**
     * Update shipping status to in transit
     */
    public void markInTransit() {
        transitionTo(OrderStatus.IN_TRANSIT);
    }
    
    /**
     * Courier is out for delivery
     */
    public void markOutForDelivery() {
        transitionTo(OrderStatus.OUT_FOR_DELIVERY);
    }
    
    /**
     * Mark delivery as failed
     */
    public void markDeliveryFailed() {
        if (this.status != OrderStatus.IN_TRANSIT && this.status != OrderStatus.OUT_FOR_DELIVERY) {
            throw ApiException.conflict("Can only mark delivery failed when IN_TRANSIT or OUT_FOR_DELIVERY");
        }
        this.status = OrderStatus.DELIVERY_FAILED;
    }
    
    /**
     * Retry delivery after failure
     */
    public void retryDelivery() {
        if (this.status != OrderStatus.DELIVERY_FAILED) {
            throw ApiException.conflict("Can only retry delivery from DELIVERY_FAILED status");
        }
        this.status = OrderStatus.IN_TRANSIT;
    }
    
    /**
     * Mark as delivered
     */
    public void markDelivered() {
        transitionTo(OrderStatus.DELIVERED);
    }
    
    /**
     * Buyer confirms receipt of order
     */
    public void buyerConfirmReceipt() {
        if (this.status != OrderStatus.DELIVERED) {
            throw ApiException.conflict("Buyer can only confirm receipt of DELIVERED orders");
        }
        this.status = OrderStatus.COMPLETED;
    }
    
    /**
     * Auto-complete order (called by scheduler after 7 days)
     */
    public void autoComplete() {
        if (this.status != OrderStatus.DELIVERED) {
            throw ApiException.conflict("Can only auto-complete DELIVERED orders");
        }
        this.status = OrderStatus.COMPLETED;
    }
    
    /**
     * Request return (buyer)
     */
    public void requestReturn() {
        if (this.status != OrderStatus.DELIVERED) {
            throw ApiException.conflict("Can only request return for DELIVERED orders");
        }
        this.status = OrderStatus.RETURN_REQUESTED;
    }
    
    /**
     * Approve return request (seller)
     */
    public void approveReturn() {
        if (this.status != OrderStatus.RETURN_REQUESTED) {
            throw ApiException.conflict("Can only approve RETURN_REQUESTED orders");
        }
        this.status = OrderStatus.RETURN_APPROVED;
    }

    /**
     * Core state transition method with validation
     */
    public void transitionTo(OrderStatus next) {
        if (this.status == next) return;

        boolean valid = switch (this.status) {
            // Payment Phase
            case SUBMITTED -> Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.PROCESSING, OrderStatus.CANCELLED).contains(next);
            case PAYMENT_PENDING -> Set.of(OrderStatus.PAID, OrderStatus.CANCELLED).contains(next);
            case PAID -> Set.of(OrderStatus.TO_SHIP, OrderStatus.PROCESSING, OrderStatus.CANCELLED).contains(next);
            
            // Fulfillment Phase
            case TO_SHIP -> Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED).contains(next);
            case PROCESSING -> Set.of(OrderStatus.READY_TO_SHIP, OrderStatus.CANCELLED).contains(next);
            case READY_TO_SHIP -> Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED).contains(next);
            
            // Shipping Phase
            case SHIPPED -> Set.of(OrderStatus.IN_TRANSIT, OrderStatus.DELIVERED).contains(next);
            case IN_TRANSIT -> Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED).contains(next);
            case OUT_FOR_DELIVERY -> Set.of(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED).contains(next);
            case DELIVERY_FAILED -> Set.of(OrderStatus.IN_TRANSIT, OrderStatus.RETURN_IN_TRANSIT, OrderStatus.CANCELLED).contains(next);
            case DELIVERED -> Set.of(OrderStatus.COMPLETED, OrderStatus.RETURN_REQUESTED, OrderStatus.REFUND_REQUESTED).contains(next);
            
            // Return Phase
            case RETURN_REQUESTED -> Set.of(OrderStatus.RETURN_APPROVED, OrderStatus.COMPLETED).contains(next);
            case RETURN_APPROVED -> next == OrderStatus.RETURN_IN_TRANSIT;
            case RETURN_IN_TRANSIT -> next == OrderStatus.RETURN_COMPLETED;
            case RETURN_COMPLETED -> next == OrderStatus.REFUNDED;
            
            // Refund Phase (without return)
            case REFUND_REQUESTED -> Set.of(OrderStatus.REFUND_APPROVED, OrderStatus.COMPLETED).contains(next);
            case REFUND_APPROVED -> next == OrderStatus.REFUNDED;
            
            // Terminal states
            case COMPLETED, CANCELLED, REFUNDED -> false;
            
            // Legacy
            case FULFILLED -> false;
        };

        if (!valid) {
            throw ApiException.badRequest(String.format("Invalid status transition from %s to %s", this.status, next));
        }
        this.status = next;
    }

    public void applyDiscount(long shopDiscount) {
        if (shopDiscount < 0) {
            throw ApiException.badRequest("Discount cannot be negative");
        }
        this.discountAmount = shopDiscount;
        calculateTotals();
    }

    public void addItem(Long productId, Long skuId, int quantity, long unitPrice) {
        if (this.status != OrderStatus.SUBMITTED) {
            throw ApiException.conflict("Cannot add items to order in status " + this.status);
        }
        this.items.add(new OrderItem(productId, skuId, quantity, unitPrice));
        calculateTotals();
    }

    public List<OrderItem> getItems() {
        return items;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public long getTotalAmount() {
        return totalAmount;
    }
    
    public long getShippingFee() {
        return shippingFee;
    }
    
    /**
     * Check if order can be modified
     */
    public boolean canModify() {
        return this.status == OrderStatus.SUBMITTED;
    }
    
    /**
     * Check if order is in a final state
     */
    public boolean isFinal() {
        return this.status.isTerminal();
    }
}
