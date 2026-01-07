package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final ProductJpaRepository productRepo;
    private final UserJpaRepository userRepo;

    /**
     * Get all orders for a shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getShopOrders(Long shopId, Pageable pageable) {
        log.info("Getting orders for shop: {}", shopId);

        Page<OrderEntity> orders = orderRepo.findByShopId(shopId, pageable);

        return orders.map(order -> {
            int itemCount = orderItemRepo.findByOrderId(order.getId()).size();
            return OrderSummaryResponse.from(order, itemCount);
        });
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getShopOrdersByStatus(Long shopId, String status, Pageable pageable) {
        log.info("Getting {} orders for shop: {}", status, shopId);

        Page<OrderEntity> orders = orderRepo.findByShopIdAndStatus(shopId, status, pageable);

        return orders.map(order -> {
            int itemCount = orderItemRepo.findByOrderId(order.getId()).size();
            return OrderSummaryResponse.from(order, itemCount);
        });
    }

    /**
     * Get order details
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long shopId, Long orderId) {
        log.info("Getting order detail for order: {} in shop: {}", orderId, shopId);

        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        // Verify order belongs to shop
        if (!order.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }

        // Get order items
        List<OrderItemEntity> items = orderItemRepo.findByOrderId(orderId);

        // Get product names
        Map<Long, String> productNames = new HashMap<>();
        for (OrderItemEntity item : items) {
            ProductEntity product = productRepo.findById(item.getProductId()).orElse(null);
            if (product != null) {
                productNames.put(item.getProductId(), product.getName());
            }
        }

        // Get user email
        String userEmail = userRepo.findById(order.getUserId())
                .map(UserEntity::getEmail)
                .orElse("Unknown");

        // Build items with product names
        List<OrderDetailResponse.OrderItemDetail> itemDetails = items.stream()
                .map(item -> OrderDetailResponse.OrderItemDetail.from(
                        item,
                        productNames.get(item.getProductId())
                ))
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderCode(),
                order.getUserId(),
                userEmail,
                order.getShopId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemDetails
        );
    }

    /**
     * Update order status
     */
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long shopId, Long orderId, OrderStatus newStatus, String note) {
        log.info("Updating order {} status to {} for shop {}", orderId, newStatus, shopId);

        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        // Verify order belongs to shop
        if (!order.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus.name());

        order.setStatus(newStatus.name());
        orderRepo.save(order);

        log.info("Order {} status updated to {}", orderId, newStatus);

        return getOrderDetail(shopId, orderId);
    }

    /**
     * Validate status transitions
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        OrderStatus current = OrderStatus.valueOf(currentStatus);
        OrderStatus next = OrderStatus.valueOf(newStatus);

        // Define valid transitions
        Map<OrderStatus, List<OrderStatus>> validTransitions = new HashMap<>();
        validTransitions.put(OrderStatus.PAID, List.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        validTransitions.put(OrderStatus.PROCESSING, List.of(OrderStatus.READY_TO_SHIP, OrderStatus.CANCELLED));
        validTransitions.put(OrderStatus.READY_TO_SHIP, List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        validTransitions.put(OrderStatus.SHIPPED, List.of(OrderStatus.DELIVERED, OrderStatus.REFUND_REQUESTED));
        validTransitions.put(OrderStatus.DELIVERED, List.of(OrderStatus.COMPLETED, OrderStatus.REFUND_REQUESTED));
        validTransitions.put(OrderStatus.REFUND_REQUESTED, List.of(OrderStatus.REFUNDED, OrderStatus.COMPLETED));

        List<OrderStatus> allowedStatuses = validTransitions.get(current);
        if (allowedStatuses == null || !allowedStatuses.contains(next)) {
            throw ApiException.badRequest(
                    String.format("Invalid status transition from %s to %s", current, next)
            );
        }
    }

    /**
     * Get order statistics for shop
     */
    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats(Long shopId) {
        log.info("Getting order statistics for shop: {}", shopId);

        long totalOrders = orderRepo.countByShopIdAndStatus(shopId, null);
        long pendingOrders = orderRepo.countByShopIdAndStatus(shopId, OrderStatus.PAID.name());
        long processingOrders = orderRepo.countByShopIdAndStatus(shopId, OrderStatus.PROCESSING.name())
                + orderRepo.countByShopIdAndStatus(shopId, OrderStatus.READY_TO_SHIP.name());
        long shippedOrders = orderRepo.countByShopIdAndStatus(shopId, OrderStatus.SHIPPED.name());
        long completedOrders = orderRepo.countByShopIdAndStatus(shopId, OrderStatus.COMPLETED.name())
                + orderRepo.countByShopIdAndStatus(shopId, OrderStatus.DELIVERED.name());
        long cancelledOrders = orderRepo.countByShopIdAndStatus(shopId, OrderStatus.CANCELLED.name());
        long refundRequested = orderRepo.countByShopIdAndStatus(shopId, OrderStatus.REFUND_REQUESTED.name());

        // Calculate revenue
        List<OrderEntity> completedOrdersList = orderRepo.findByShopIdAndStatus(shopId, OrderStatus.COMPLETED.name());
        Long totalRevenue = completedOrdersList.stream()
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        // Today's revenue
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<OrderEntity> todayOrders = orderRepo.findByShopIdAndDateRange(shopId, startOfDay, endOfDay);
        Long todayRevenue = todayOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.name().equals(o.getStatus()) ||
                        OrderStatus.DELIVERED.name().equals(o.getStatus()))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        return new OrderStatsResponse(
                totalOrders,
                pendingOrders,
                processingOrders,
                shippedOrders,
                completedOrders,
                cancelledOrders,
                refundRequested,
                totalRevenue,
                todayRevenue
        );
    }

    /**
     * Cancel order (seller initiated)
     */
    @Transactional
    public void cancelOrder(Long shopId, Long orderId, String reason) {
        log.info("Cancelling order {} for shop {}", orderId, shopId);

        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        if (!order.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }

        // Can only cancel if not yet shipped
        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());
        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED ||
                currentStatus == OrderStatus.COMPLETED) {
            throw ApiException.badRequest("Cannot cancel order that has been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED.name());
        orderRepo.save(order);

        log.info("Order {} cancelled successfully", orderId);
    }
}
