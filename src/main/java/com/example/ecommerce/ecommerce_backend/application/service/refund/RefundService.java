package com.example.ecommerce.ecommerce_backend.application.service.refund;

import com.example.ecommerce.ecommerce_backend.api.dto.refund.CreateRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.ProcessRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.RefundResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.refund.RefundStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefundEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefundJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundJpaRepository refundRepo;
    private final OrderJpaRepository orderRepo;
    private final UserJpaRepository userRepo;
    private final SellerShopJpaRepository shopRepo;
    private final EventLogMongoRepository eventRepo;
    private final ReservationService reservationService;

    public RefundService(
            RefundJpaRepository refundRepo,
            OrderJpaRepository orderRepo,
            UserJpaRepository userRepo,
            SellerShopJpaRepository shopRepo,
            EventLogMongoRepository eventRepo,
            ReservationService reservationService
    ) {
        this.refundRepo = refundRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.shopRepo = shopRepo;
        this.eventRepo = eventRepo;
        this.reservationService = reservationService;
    }

    private void verifyShopOwner(Long shopId, Long sellerId) {
        var shop = shopRepo.findById(shopId).orElseThrow(() -> ApiException.notFound("Shop not found"));
        if (!shop.getSellerUserId().equals(sellerId)) {
            throw ApiException.forbidden("You do not own this shop");
        }
    }

    @Transactional
    public RefundResponse createRefund(Long userId, CreateRefundRequest request) {
        log.info("Creating refund request for order: {} by user: {}", request.orderId(), userId);

        OrderEntity order;
        if (request.orderId() != null) {
            order = orderRepo.findById(request.orderId())
                    .orElseThrow(() -> ApiException.notFound("Order not found"));
        } else if (request.orderCode() != null && !request.orderCode().isBlank()) {
            order = orderRepo.findByOrderCode(request.orderCode())
                    .orElseThrow(() -> ApiException.notFound("Order not found with code: " + request.orderCode()));
        } else {
            throw ApiException.badRequest("Either orderId or orderCode must be provided");
        }

        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("Order does not belong to user");
        }

        OrderStatus orderStatus = OrderStatus.valueOf(order.getStatus());
        if (orderStatus != OrderStatus.DELIVERED && 
            orderStatus != OrderStatus.COMPLETED &&
            orderStatus != OrderStatus.PAID &&
            orderStatus != OrderStatus.PROCESSING) {
            throw ApiException.badRequest("Order in status " + orderStatus + " cannot be refunded");
        }

        List<RefundEntity> existingRefunds = refundRepo.findByOrderId(order.getId());
        if (!existingRefunds.isEmpty()) {
            RefundEntity existing = existingRefunds.get(0);
            if (!RefundStatus.REJECTED.name().equals(existing.getStatus()) &&
                !RefundStatus.CANCELLED.name().equals(existing.getStatus())) {
                throw ApiException.badRequest("Refund request already exists for this order");
            }
        }

        if (request.refundAmount() > order.getTotalAmount()) {
            throw ApiException.badRequest("Refund amount cannot exceed order total");
        }

        RefundEntity refund = new RefundEntity();
        refund.setOrderId(order.getId());
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

        order.setStatus(OrderStatus.REFUND_REQUESTED.name());
        orderRepo.save(order);

        log.info("Refund request created: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> getUserRefunds(Long userId, Pageable pageable) {
        return refundRepo.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> getShopRefunds(Long shopId, Long sellerId, Pageable pageable) {
        verifyShopOwner(shopId, sellerId);
        return refundRepo.findByShopId(shopId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> getShopRefundsByStatus(Long shopId, Long sellerId, String status, Pageable pageable) {
        verifyShopOwner(shopId, sellerId);
        return refundRepo.findByShopIdAndStatus(shopId, status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundDetail(Long refundId) {
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));
        return toResponse(refund);
    }

    @Transactional(readOnly = true)
    public RefundResponse getUserRefundDetail(Long userId, Long refundId) {
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));
        if (!refund.getUserId().equals(userId)) {
            throw ApiException.forbidden("You do not have permission to view this refund");
        }
        return toResponse(refund);
    }

    @Transactional(readOnly = true)
    public RefundResponse getSellerRefundDetail(Long shopId, Long sellerId, Long refundId) {
        verifyShopOwner(shopId, sellerId);
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));
        if (!refund.getShopId().equals(shopId)) {
             throw ApiException.forbidden("Refund does not belong to shop");
        }
        return toResponse(refund);
    }

    @Transactional
    public RefundResponse processRefund(Long shopId, Long sellerId, Long refundId, ProcessRefundRequest request) {
        verifyShopOwner(shopId, sellerId);
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));

        if (!refund.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Refund does not belong to shop");
        }

        if (!RefundStatus.PENDING.name().equals(refund.getStatus()) &&
            !RefundStatus.UNDER_REVIEW.name().equals(refund.getStatus())) {
            throw ApiException.badRequest("Refund cannot be processed in current status");
        }

        RefundStatus newStatus = RefundStatus.valueOf(request.status().toUpperCase());
        refund.setStatus(newStatus.name());
        refund.setAdminNote(request.adminNote());
        refund.setUpdatedAt(Instant.now());

        if (newStatus == RefundStatus.APPROVED || newStatus == RefundStatus.REJECTED) {
            refund.setProcessedAt(Instant.now());
        }

        RefundEntity saved = refundRepo.save(refund);

        if (newStatus == RefundStatus.APPROVED) {
            handleRefundApproval(saved.getOrderId());
        }

        log.info("Refund {} processed with status: {}", refundId, newStatus);
        return toResponse(saved);
    }

    private void handleRefundApproval(Long orderId) {
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        order.setStatus(OrderStatus.REFUNDED.name());
        orderRepo.save(order);

        // Restore stock
        reservationService.restore(order.getOrderCode());
    }

    @Transactional
    public void cancelRefund(Long userId, Long refundId) {
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));
        if (!refund.getUserId().equals(userId)) {
            throw ApiException.forbidden("Refund does not belong to user");
        }
        if (!RefundStatus.PENDING.name().equals(refund.getStatus())) {
            throw ApiException.badRequest("Only pending refunds can be cancelled");
        }
        refund.setStatus(RefundStatus.CANCELLED.name());
        refund.setUpdatedAt(Instant.now());
        refundRepo.save(refund);
    }
    
    // ==================== NEW: Enhanced Return Flow ====================
    
    /**
     * Create a return request (with physical product return)
     */
    @Transactional
    public RefundResponse createReturnRequest(Long userId, CreateRefundRequest request) {
        log.info("Creating return request for order by user: {}", userId);
        
        OrderEntity order = getOrderForRefund(request);
        
        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("Order does not belong to user");
        }
        
        OrderStatus orderStatus = OrderStatus.valueOf(order.getStatus());
        if (orderStatus != OrderStatus.DELIVERED) {
            throw ApiException.badRequest("Return can only be requested for DELIVERED orders. Current: " + orderStatus);
        }
        
        // Check for existing return/refund
        List<RefundEntity> existingRefunds = refundRepo.findByOrderId(order.getId());
        if (!existingRefunds.isEmpty()) {
            RefundEntity existing = existingRefunds.get(0);
            if (!RefundStatus.REJECTED.name().equals(existing.getStatus()) &&
                !RefundStatus.CANCELLED.name().equals(existing.getStatus())) {
                throw ApiException.badRequest("Return/Refund request already exists for this order");
            }
        }
        
        RefundEntity refund = new RefundEntity();
        refund.setOrderId(order.getId());
        refund.setUserId(userId);
        refund.setShopId(order.getShopId());
        refund.setReason(request.reason());
        refund.setDescription(request.description());
        refund.setRefundAmount(request.refundAmount() != null ? request.refundAmount() : order.getTotalAmount());
        refund.setCurrency(order.getCurrency());
        refund.setStatus(RefundStatus.PENDING.name());
        refund.setRefundType("RETURN"); // Mark as return type
        refund.setCreatedAt(Instant.now());
        refund.setUpdatedAt(Instant.now());
        
        RefundEntity saved = refundRepo.save(refund);
        
        // Update order status to RETURN_REQUESTED
        order.setStatus(OrderStatus.RETURN_REQUESTED.name());
        orderRepo.save(order);
        
        eventRepo.save(new EventLogDocument(
                "RETURN_REQUESTED",
                "refund_" + saved.getId(),
                Instant.now(),
                null,
                Map.of("refundId", saved.getId(), "orderId", order.getId(), "userId", userId)
        ));
        
        log.info("Return request created: {}", saved.getId());
        return toResponse(saved);
    }
    
    /**
     * Seller approves return request
     */
    @Transactional
    public RefundResponse approveReturn(Long shopId, Long sellerId, Long refundId, String note) {
        verifyShopOwner(shopId, sellerId);
        
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Return request not found"));
        
        if (!refund.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Return request does not belong to shop");
        }
        
        if (!RefundStatus.PENDING.name().equals(refund.getStatus())) {
            throw ApiException.badRequest("Only PENDING return requests can be approved");
        }
        
        refund.setStatus(RefundStatus.UNDER_REVIEW.name()); // Waiting for return shipment
        refund.setAdminNote(note);
        refund.setUpdatedAt(Instant.now());
        RefundEntity saved = refundRepo.save(refund);
        
        // Update order status
        OrderEntity order = orderRepo.findById(refund.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.RETURN_APPROVED.name());
        orderRepo.save(order);
        
        log.info("Return {} approved by seller, awaiting return shipment", refundId);
        return toResponse(saved);
    }
    
    /**
     * Mark return package as received by seller
     */
    @Transactional
    public RefundResponse confirmReturnReceived(Long shopId, Long sellerId, Long refundId) {
        verifyShopOwner(shopId, sellerId);
        
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Return request not found"));
        
        if (!refund.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Return request does not belong to shop");
        }
        
        // Mark as approved (ready for refund processing)
        refund.setStatus(RefundStatus.APPROVED.name());
        refund.setProcessedAt(Instant.now());
        refund.setUpdatedAt(Instant.now());
        RefundEntity saved = refundRepo.save(refund);
        
        // Update order and trigger refund
        OrderEntity order = orderRepo.findById(refund.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.RETURN_COMPLETED.name());
        orderRepo.save(order);
        
        // Restore inventory
        reservationService.restore(order.getOrderCode());
        
        log.info("Return {} received, refund approved", refundId);
        return toResponse(saved);
    }
    
    /**
     * Reject return request
     */
    @Transactional
    public RefundResponse rejectReturn(Long shopId, Long sellerId, Long refundId, String reason) {
        verifyShopOwner(shopId, sellerId);
        
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Return request not found"));
        
        if (!refund.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Return request does not belong to shop");
        }
        
        if (!RefundStatus.PENDING.name().equals(refund.getStatus())) {
            throw ApiException.badRequest("Only PENDING return requests can be rejected");
        }
        
        refund.setStatus(RefundStatus.REJECTED.name());
        refund.setAdminNote(reason);
        refund.setProcessedAt(Instant.now());
        refund.setUpdatedAt(Instant.now());
        RefundEntity saved = refundRepo.save(refund);
        
        // Restore order to DELIVERED
        OrderEntity order = orderRepo.findById(refund.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.DELIVERED.name());
        orderRepo.save(order);
        
        log.info("Return {} rejected with reason: {}", refundId, reason);
        return toResponse(saved);
    }
    
    private OrderEntity getOrderForRefund(CreateRefundRequest request) {
        if (request.orderId() != null) {
            return orderRepo.findById(request.orderId())
                    .orElseThrow(() -> ApiException.notFound("Order not found"));
        } else if (request.orderCode() != null && !request.orderCode().isBlank()) {
            return orderRepo.findByOrderCode(request.orderCode())
                    .orElseThrow(() -> ApiException.notFound("Order not found with code: " + request.orderCode()));
        } else {
            throw ApiException.badRequest("Either orderId or orderCode must be provided");
        }
    }

    // ADMIN
    @Transactional(readOnly = true)
    public Page<RefundResponse> adminListRefunds(String status, Pageable pageable) {
        if (status == null || status.isBlank()) {
             return refundRepo.findAll(pageable).map(this::toResponse);
        }
        return refundRepo.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RefundResponse adminGetRefundDetail(Long refundId) {
        return getRefundDetail(refundId);
    }

    @Transactional
    public RefundResponse adminProcessRefund(Long refundId, ProcessRefundRequest request) {
        RefundEntity refund = refundRepo.findById(refundId)
                .orElseThrow(() -> ApiException.notFound("Refund not found"));

        RefundStatus newStatus = RefundStatus.valueOf(request.status().toUpperCase());
        refund.setStatus(newStatus.name());
        refund.setAdminNote(request.adminNote());
        refund.setUpdatedAt(Instant.now());

        if (newStatus == RefundStatus.APPROVED || newStatus == RefundStatus.REJECTED) {
            refund.setProcessedAt(Instant.now());
        }

        RefundEntity saved = refundRepo.save(refund);

        eventRepo.save(new EventLogDocument(
                "REFUND_STATUS_CHANGED",
                "refund_" + saved.getId(),
                Instant.now(),
                null,
                Map.of(
                        "refundId", saved.getId(),
                        "status", newStatus,
                        "adminNote", request.adminNote() == null ? "" : request.adminNote()
                )
        ));

        if (newStatus == RefundStatus.APPROVED) {
            handleRefundApproval(saved.getOrderId());
        }

        return toResponse(saved);
    }

    private RefundResponse toResponse(RefundEntity refund) {
        String orderCode = orderRepo.findById(refund.getOrderId())
                .map(OrderEntity::getOrderCode)
                .orElse("Unknown");

        String userEmail = userRepo.findById(refund.getUserId())
                .map(UserEntity::getEmail)
                .orElse("Unknown");

        String shopName = shopRepo.findById(refund.getShopId())
                .map(s -> s.getShopName())
                .orElse("Shop #" + refund.getShopId());

        return RefundResponse.from(refund, orderCode, userEmail, shopName);
    }
}
