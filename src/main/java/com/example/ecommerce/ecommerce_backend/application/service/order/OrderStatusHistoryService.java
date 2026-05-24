package com.example.ecommerce.ecommerce_backend.application.service.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderStatusHistoryResponse;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderStatusHistoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderStatusHistoryEntity.ActorType;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderStatusHistoryJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;

/**
 * Service for managing order status history.
 * Provides audit trail for order status changes.
 */
@Service
public class OrderStatusHistoryService {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusHistoryService.class);

    private final OrderStatusHistoryJpaRepository historyRepository;
    private final UserJpaRepository userRepository;

    public OrderStatusHistoryService(
            OrderStatusHistoryJpaRepository historyRepository,
            UserJpaRepository userRepository
    ) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    // ============ RECORD STATUS CHANGES ============

    /**
     * Record a status change made by the system (auto-transitions, scheduled jobs, etc.)
     * Uses REQUIRED propagation to participate in existing transaction, avoiding deadlocks.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordSystemChange(Long orderId, String fromStatus, String toStatus, String reason) {
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.systemChange(
            orderId, fromStatus, toStatus, reason);
        historyRepository.save(history);
        log.debug("Recorded system status change for order {}: {} -> {}", orderId, fromStatus, toStatus);
    }

    /**
     * Record a status change made by the buyer
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordBuyerChange(Long orderId, String fromStatus, String toStatus, Long buyerId, String reason) {
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.buyerChange(
            orderId, fromStatus, toStatus, buyerId, reason);
        historyRepository.save(history);
        log.debug("Recorded buyer status change for order {}: {} -> {} by user {}", 
            orderId, fromStatus, toStatus, buyerId);
    }

    /**
     * Record a status change made by the seller
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordSellerChange(Long orderId, String fromStatus, String toStatus, Long sellerId, String reason) {
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.sellerChange(
            orderId, fromStatus, toStatus, sellerId, reason);
        historyRepository.save(history);
        log.debug("Recorded seller status change for order {}: {} -> {} by seller {}", 
            orderId, fromStatus, toStatus, sellerId);
    }

    /**
     * Record a status change made by an admin
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordAdminChange(Long orderId, String fromStatus, String toStatus, Long adminId, String reason) {
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.adminChange(
            orderId, fromStatus, toStatus, adminId, reason);
        historyRepository.save(history);
        log.debug("Recorded admin status change for order {}: {} -> {} by admin {}", 
            orderId, fromStatus, toStatus, adminId);
    }

    /**
     * Record a status change with full metadata
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordChange(
            Long orderId,
            String fromStatus,
            String toStatus,
            ActorType actorType,
            Long actorId,
            String reason,
            String note,
            Map<String, Object> metadata
    ) {
        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.create(
            orderId, fromStatus, toStatus, actorType, actorId, reason, note, metadata);
        historyRepository.save(history);
        log.debug("Recorded status change for order {}: {} -> {}", orderId, fromStatus, toStatus);
    }

    // ============ QUERY HISTORY ============

    /**
     * Get complete order history timeline
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderHistory(Long orderId) {
        List<OrderStatusHistoryEntity> history = historyRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        
        // Get actor names
        List<Long> actorIds = history.stream()
            .filter(h -> h.getActorId() != null)
            .map(OrderStatusHistoryEntity::getActorId)
            .distinct()
            .toList();
        
        Map<Long, String> actorNames = userRepository.findAllById(actorIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, UserEntity::getFullName));
        
        return history.stream()
            .map(h -> OrderStatusHistoryResponse.from(h, actorNames.get(h.getActorId())))
            .toList();
    }

    /**
     * Get order history with actor information populated
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderHistoryWithActors(Long orderId) {
        return getOrderHistory(orderId);
    }

    /**
     * Get latest status entry for an order
     */
    @Transactional(readOnly = true)
    public OrderStatusHistoryResponse getLatestStatus(Long orderId) {
        return historyRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
            .map(h -> {
                String actorName = h.getActorId() != null 
                    ? userRepository.findById(h.getActorId()).map(UserEntity::getFullName).orElse(null)
                    : null;
                return OrderStatusHistoryResponse.from(h, actorName);
            })
            .orElse(null);
    }

    /**
     * Get history for multiple orders at once (for list views)
     */
    @Transactional(readOnly = true)
    public Map<Long, List<OrderStatusHistoryResponse>> getHistoryForOrders(List<Long> orderIds) {
        List<OrderStatusHistoryEntity> allHistory = historyRepository.findByOrderIds(orderIds);
        
        // Get actor names
        List<Long> actorIds = allHistory.stream()
            .filter(h -> h.getActorId() != null)
            .map(OrderStatusHistoryEntity::getActorId)
            .distinct()
            .toList();
        
        Map<Long, String> actorNames = userRepository.findAllById(actorIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, UserEntity::getFullName));
        
        return allHistory.stream()
            .collect(Collectors.groupingBy(
                OrderStatusHistoryEntity::getOrderId,
                Collectors.mapping(
                    h -> OrderStatusHistoryResponse.from(h, actorNames.get(h.getActorId())),
                    Collectors.toList()
                )
            ));
    }
}
