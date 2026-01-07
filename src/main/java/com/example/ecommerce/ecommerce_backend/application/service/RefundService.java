package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.refund.CreateRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.ProcessRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.RefundResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.refund.RefundStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefundEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefundJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefundService {

    private final RefundJpaRepository refundRepo;
    private final OrderJpaRepository orderRepo;
    private final UserJpaRepository userRepo;

    /**
     * Create a new refund request
     */
    @Transactional
    public RefundResponse createRefund(Long userId, CreateRefundRequest request) {
        log.info("Creating refund request for order: {} by user: {}", request.orderId(), userId);

        // Get order
        OrderEntity order = orderRepo.findById(request.orderId())
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("Order does not belong to user");
        }

        // Check if order is eligible for refund
        OrderStatus orderStatus = OrderStatus.valueOf(order.getStatus());
        if (orderStatus != OrderStatus.DELIVERED && orderStatus != OrderStatus.COMPLETED) {
            throw ApiException.badRequest("Only delivered or completed orders can be refunded");
        }

        // Check if refund already exists for this order
        List<RefundEntity> existingRefunds = refundRepo.findByOrderId(request.orderId());
        if (!existingRefunds.isEmpty()) {
            RefundEntity existing = existingRefunds.get(0);
            if (!RefundStatus.REJECTED.name().equals(existing.getStatus()) &&
                !RefundStatus.CANCELLED.name().equals(existing.getStatus())) {
                throw ApiException.badRequest("Refund request already exists for this order");
            }
        }

        // Validate refund amount
        if (request.refundAmount() > order.getTotalAmount()) {
            throw ApiException.badRequest("Refund amount cannot exceed order total");
        }

        // Create refund
        RefundEntity refund = new RefundEntity();
        refund.setOrderId(request.orderId());
        refund.setUserId(userId);
        refund.setShopId(order.getShopId());
        refund.setReason(request.reason());
        refund.setDescription(request.description());
        refund.setRefundAmount(request.refundAmount());
        refund.setCurrency(order.getCurrency());
        refund.setStatus(RefundStatus.PENDING.name());
        refund.setCreatedAt(Instant.now());
        refund.setUpdatedAt(Instant.now());

        RefundEntity saved = refundRepo.save(refund);

        // Update order status
        order.setStatus(OrderStatus.REFUND_REQUESTED.name());
        orderRepo.save(order);

        log.info("Refund request created: {}", saved.getId());

        return toResponse(saved);
    }

    /**
     * Get all refunds for a user
     */
    @Transactional(readOnly = true)
    public Page<RefundResponse> getUserRefunds(Long userId, Pageable pageable) {
        log.info("Getting refunds for user: {}", userId);

        Page<RefundEntity> refunds = refundRepo.findByUserId(userId, pageable);
        return refunds.map(this::toResponse);
    }

    /**
     * Get all refunds for a shop
     */
    @Transactional(readOnly = true)
    public Page<RefundResponse> getShopRefunds(Long shopId, Pageable pageable) {
        log.info("Getting refunds for shop: {}", shopId);

        Page<RefundEntity> refunds = refundRepo.findByShopId(shopId, pageable);
        return refunds.map(this::toResponse);
    }

    /**
     * Get refunds by status for a shop
     */
    @Transactional(readOnly = true)
    public Page<RefundResponse> getShopRefundsByStatus(Long shopId, String status, Pageable pageable) {
        log.info("Getting {} refunds for shop: {}", status, shopId);

        Page<RefundEntity> refunds = refundRepo.findByShopIdAndStatus(shopId, status, pageable);
        return refunds.map(this::toResponse);
    }

    /**
     * Get refund detail
     */
    @Transactional(readOnly = true)
    public RefundResponse getRefundDetail(Long refundId) {
        log.info("Getting refund detail: {}", refundId);

        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));

        return toResponse(refund);
    }

    /**
     * Process refund (approve or reject)
     */
    @Transactional
    public RefundResponse processRefund(Long shopId, Long refundId, ProcessRefundRequest request) {
        log.info("Processing refund: {} with status: {}", refundId, request.status());

        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));

        // Verify refund belongs to shop
        if (!refund.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Refund does not belong to shop");
        }

        // Can only process pending or under review refunds
        if (!RefundStatus.PENDING.name().equals(refund.getStatus()) &&
            !RefundStatus.UNDER_REVIEW.name().equals(refund.getStatus())) {
            throw ApiException.badRequest("Refund cannot be processed in current status");
        }

        // Update refund status
        RefundStatus newStatus = RefundStatus.valueOf(request.status().toUpperCase());
        refund.setStatus(newStatus.name());
        refund.setAdminNote(request.adminNote());
        refund.setUpdatedAt(Instant.now());

        if (newStatus == RefundStatus.APPROVED || newStatus == RefundStatus.REJECTED) {
            refund.setProcessedAt(Instant.now());
        }

        RefundEntity saved = refundRepo.save(refund);

        // Update order status if refund is approved
        if (newStatus == RefundStatus.APPROVED) {
            OrderEntity order = orderRepo.findById(refund.getOrderId())
                    .orElseThrow(() -> ApiException.notFound("Order not found"));
            order.setStatus(OrderStatus.REFUNDED.name());
            orderRepo.save(order);
        }

        log.info("Refund {} processed with status: {}", refundId, newStatus);

        return toResponse(saved);
    }

    /**
     * Cancel refund request (by user)
     */
    @Transactional
    public void cancelRefund(Long userId, Long refundId) {
        log.info("Cancelling refund: {} by user: {}", refundId, userId);

        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));

        // Verify refund belongs to user
        if (!refund.getUserId().equals(userId)) {
            throw ApiException.forbidden("Refund does not belong to user");
        }

        // Can only cancel pending refunds
        if (!RefundStatus.PENDING.name().equals(refund.getStatus())) {
            throw ApiException.badRequest("Only pending refunds can be cancelled");
        }

        refund.setStatus(RefundStatus.CANCELLED.name());
        refund.setUpdatedAt(Instant.now());
        refundRepo.save(refund);

        log.info("Refund {} cancelled", refundId);
    }

    /**
     * Convert entity to response DTO
     */
    private RefundResponse toResponse(RefundEntity refund) {
        String orderCode = orderRepo.findById(refund.getOrderId())
                .map(OrderEntity::getOrderCode)
                .orElse("Unknown");

        String userEmail = userRepo.findById(refund.getUserId())
                .map(UserEntity::getEmail)
                .orElse("Unknown");

        String shopName = "Shop #" + refund.getShopId();

        return RefundResponse.from(refund, orderCode, userEmail, shopName);
    }
}
