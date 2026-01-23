package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class SellerOrderService {

    private static final Logger log = LoggerFactory.getLogger(SellerOrderService.class);

    private void verifyShopOwner(Long shopId, Long sellerId) {
        var shop = shopRepo.findById(shopId).orElseThrow(() -> ApiException.notFound("Shop not found"));
        if (!shop.getSellerUserId().equals(sellerId)) {
            throw ApiException.forbidden("You do not own this shop");
        }
    }

    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final ProductJpaRepository productRepo;
    private final UserJpaRepository userRepo;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository shopRepo;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.mapper.OrderDomainMapper orderMapper;
    private final ReservationService reservationService;
    private final OrderStatusHistoryService orderHistoryService;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository addressRepo;

    public SellerOrderService(
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository orderItemRepo,
            ProductJpaRepository productRepo,
            UserJpaRepository userRepo,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository shopRepo,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.mapper.OrderDomainMapper orderMapper,
            ReservationService reservationService,
            OrderStatusHistoryService orderHistoryService,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository addressRepo
    ) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.shopRepo = shopRepo;
        this.orderMapper = orderMapper;
        this.reservationService = reservationService;
        this.orderHistoryService = orderHistoryService;
        this.addressRepo = addressRepo;
    }

    // ... (existing methods omitted for brevity) ...

    @Transactional
    public void cancelOrder(Long shopId, Long sellerId, Long orderId, String reason) {
        verifyShopOwner(shopId, sellerId);
        log.info("Cancelling order {} for shop {}", orderId, shopId);

        OrderEntity orderEntity = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        if (!orderEntity.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }

        String previousStatus = orderEntity.getStatus();
        
        // Targeted improvement: Move invariants to domain layer
        var itemEntities = orderItemRepo.findByOrderId(orderEntity.getId());
        com.example.ecommerce.ecommerce_backend.domain.order.Order domainOrder = orderMapper.toDomain(orderEntity, itemEntities);
        
        domainOrder.cancelBySeller();
        
        orderMapper.updateEntity(domainOrder, orderEntity);
        orderRepo.save(orderEntity);
        
        // Log status change
        orderHistoryService.recordSellerChange(orderEntity.getId(), previousStatus, 
            OrderStatus.CANCELLED.name(), sellerId, reason != null ? reason : "Cancelled by seller");

        reservationService.release(orderEntity.getOrderCode());
        reservationService.restore(orderEntity.getOrderCode());

        log.info("Order {} cancelled successfully", orderId);
    }

    /**
     * Get all orders for a shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getShopOrders(Long shopId, Long sellerId, Pageable pageable) {
        verifyShopOwner(shopId, sellerId);
        log.info("Getting orders for shop: {}", shopId);

        Page<OrderEntity> orders = orderRepo.findByShopId(shopId, pageable);

        // Batch fetch item counts to avoid N+1
        List<Long> orderIds = orders.getContent().stream().map(OrderEntity::getId).toList();
        Map<Long, Long> itemCounts = getItemCountsMap(orderIds);

        return orders.map(order -> {
            int itemCount = itemCounts.getOrDefault(order.getId(), 0L).intValue();
            return OrderSummaryResponse.from(order, itemCount);
        });
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getShopOrdersByStatus(Long shopId, Long sellerId, String status, Pageable pageable) {
        verifyShopOwner(shopId, sellerId);
        log.info("Getting {} orders for shop: {}", status, shopId);

        Page<OrderEntity> orders = orderRepo.findByShopIdAndStatus(shopId, status, pageable);

        // Batch fetch item counts to avoid N+1
        List<Long> orderIds = orders.getContent().stream().map(OrderEntity::getId).toList();
        Map<Long, Long> itemCounts = getItemCountsMap(orderIds);

        return orders.map(order -> {
            int itemCount = itemCounts.getOrDefault(order.getId(), 0L).intValue();
            return OrderSummaryResponse.from(order, itemCount);
        });
    }

    /**
     * Helper to batch fetch item counts
     */
    private Map<Long, Long> getItemCountsMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return orderItemRepo.countItemsByOrderIds(orderIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    /**
     * Get order details
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long shopId, Long sellerId, Long orderId) {
        verifyShopOwner(shopId, sellerId);
        log.info("Getting order detail for order: {} in shop: {}", orderId, shopId);

        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        // Verify order belongs to shop
        if (!order.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }

        // Get order items
        List<OrderItemEntity> items = orderItemRepo.findByOrderId(orderId);

        // Batch fetch product names to avoid N+1
        List<Long> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();
        Map<Long, String> productNames = productRepo.findAllById(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        ProductEntity::getId,
                        ProductEntity::getName
                ));

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

        // Use fromWithShipping to include shipping info
        return OrderDetailResponse.fromWithShipping(order, userEmail, itemDetails);
    }

    /**
     * Update order status
     */
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long shopId, Long sellerId, Long orderId, OrderStatus newStatus, String note, String trackingNumber) {
        verifyShopOwner(shopId, sellerId);
        log.info("Updating order {} status to {} for shop {}", orderId, newStatus, shopId);

        OrderEntity orderEntity = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        if (!orderEntity.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }

        String previousStatus = orderEntity.getStatus();
        
        // Targeted improvement: Move invariants to domain layer
        var itemEntities = orderItemRepo.findByOrderId(orderEntity.getId());
        com.example.ecommerce.ecommerce_backend.domain.order.Order domainOrder = orderMapper.toDomain(orderEntity, itemEntities);
        
        domainOrder.transitionTo(newStatus);
        
        orderMapper.updateEntity(domainOrder, orderEntity);

        if (trackingNumber != null && !trackingNumber.isBlank()) {
            orderEntity.setTrackingNumber(trackingNumber);
        }
        
        // Update timestamps based on new status
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case SHIPPED -> orderEntity.setShippedAt(now);
            case DELIVERED -> {
                orderEntity.setDeliveredAt(now);
                orderEntity.setAutoCompleteAt(now.plusDays(7)); // Auto-complete after 7 days
            }
            case COMPLETED -> orderEntity.setCompletedAt(now);
            default -> {} // No special handling
        }

        orderRepo.save(orderEntity);
        
        // Log status change
        orderHistoryService.recordSellerChange(orderEntity.getId(), previousStatus, 
            newStatus.name(), sellerId, note);

        log.info("Order {} status updated to {}", orderId, newStatus);

        return getOrderDetail(shopId, sellerId, orderId);
    }
    
    // === NEW: Shipping Management Methods ===
    
    /**
     * Set shipping information for an order
     */
    @Transactional
    public OrderDetailResponse setShippingInfo(Long shopId, Long sellerId, Long orderId, 
            String shippingProvider, String trackingNumber, String trackingUrl, LocalDateTime estimatedDelivery) {
        verifyShopOwner(shopId, sellerId);
        log.info("Setting shipping info for order {} in shop {}", orderId, shopId);
        
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        
        if (!order.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }
        
        // Validate order is in a state where shipping info can be set
        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());
        if (!Set.of(OrderStatus.PROCESSING, OrderStatus.READY_TO_SHIP, OrderStatus.SHIPPED, 
                    OrderStatus.IN_TRANSIT, OrderStatus.OUT_FOR_DELIVERY).contains(currentStatus)) {
            throw ApiException.badRequest("Cannot set shipping info for order in status: " + currentStatus);
        }
        
        order.setShippingProvider(shippingProvider);
        order.setTrackingNumber(trackingNumber);
        order.setShippingTrackingUrl(trackingUrl);
        if (estimatedDelivery != null) {
            order.setEstimatedDeliveryDate(estimatedDelivery);
        }
        
        orderRepo.save(order);
        log.info("Shipping info set for order {}", orderId);
        
        return getOrderDetail(shopId, sellerId, orderId);
    }
    
    /**
     * Mark order as shipped with shipping details
     */
    @Transactional
    public OrderDetailResponse markShipped(Long shopId, Long sellerId, Long orderId,
            String shippingProvider, String trackingNumber, String trackingUrl, LocalDateTime estimatedDelivery) {
        verifyShopOwner(shopId, sellerId);
        log.info("Marking order {} as shipped for shop {}", orderId, shopId);
        
        OrderEntity orderEntity = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        
        if (!orderEntity.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }
        
        String previousStatus = orderEntity.getStatus();
        
        // Use domain model for validation
        var itemEntities = orderItemRepo.findByOrderId(orderEntity.getId());
        com.example.ecommerce.ecommerce_backend.domain.order.Order domainOrder = orderMapper.toDomain(orderEntity, itemEntities);
        domainOrder.ship();
        orderMapper.updateEntity(domainOrder, orderEntity);
        
        // Set shipping details
        LocalDateTime now = LocalDateTime.now();
        orderEntity.setShippingProvider(shippingProvider);
        orderEntity.setTrackingNumber(trackingNumber);
        orderEntity.setShippingTrackingUrl(trackingUrl);
        orderEntity.setShippedAt(now);
        if (estimatedDelivery != null) {
            orderEntity.setEstimatedDeliveryDate(estimatedDelivery);
        }
        
        orderRepo.save(orderEntity);
        
        // Log status change with shipping metadata
        orderHistoryService.recordChange(orderEntity.getId(), previousStatus, OrderStatus.SHIPPED.name(),
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderStatusHistoryEntity.ActorType.SELLER,
            sellerId, "Order shipped", null, 
            Map.of("trackingNumber", trackingNumber, "shippingProvider", shippingProvider));
        
        log.info("Order {} marked as shipped with tracking: {}", orderId, trackingNumber);
        
        return getOrderDetail(shopId, sellerId, orderId);
    }
    
    /**
     * Mark delivery as failed
     */
    @Transactional
    public OrderDetailResponse markDeliveryFailed(Long shopId, Long sellerId, Long orderId, String failedReason) {
        verifyShopOwner(shopId, sellerId);
        log.info("Marking delivery failed for order {} in shop {}", orderId, shopId);
        
        OrderEntity orderEntity = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        
        if (!orderEntity.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }
        
        String previousStatus = orderEntity.getStatus();
        
        // Use domain model for validation
        var itemEntities = orderItemRepo.findByOrderId(orderEntity.getId());
        com.example.ecommerce.ecommerce_backend.domain.order.Order domainOrder = orderMapper.toDomain(orderEntity, itemEntities);
        domainOrder.markDeliveryFailed();
        orderMapper.updateEntity(domainOrder, orderEntity);
        
        // Update delivery tracking
        orderEntity.setDeliveryAttempts(orderEntity.getDeliveryAttempts() + 1);
        orderEntity.setDeliveryFailedReason(failedReason);
        
        orderRepo.save(orderEntity);
        
        // Log status change
        orderHistoryService.recordSellerChange(orderEntity.getId(), previousStatus, 
            OrderStatus.DELIVERY_FAILED.name(), sellerId, failedReason);
        
        log.info("Order {} marked as delivery failed. Attempt: {}", orderId, orderEntity.getDeliveryAttempts());
        
        return getOrderDetail(shopId, sellerId, orderId);
    }
    
    /**
     * Retry delivery after failure
     */
    @Transactional
    public OrderDetailResponse retryDelivery(Long shopId, Long sellerId, Long orderId) {
        verifyShopOwner(shopId, sellerId);
        log.info("Retrying delivery for order {} in shop {}", orderId, shopId);
        
        OrderEntity orderEntity = orderRepo.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        
        if (!orderEntity.getShopId().equals(shopId)) {
            throw ApiException.forbidden("Order does not belong to your shop");
        }
        
        String previousStatus = orderEntity.getStatus();
        
        // Use domain model for validation
        var itemEntities = orderItemRepo.findByOrderId(orderEntity.getId());
        com.example.ecommerce.ecommerce_backend.domain.order.Order domainOrder = orderMapper.toDomain(orderEntity, itemEntities);
        domainOrder.retryDelivery();
        orderMapper.updateEntity(domainOrder, orderEntity);
        
        // Clear failed reason
        orderEntity.setDeliveryFailedReason(null);
        
        orderRepo.save(orderEntity);
        
        // Log status change
        orderHistoryService.recordSellerChange(orderEntity.getId(), previousStatus, 
            OrderStatus.OUT_FOR_DELIVERY.name(), sellerId, "Delivery retry initiated");
        
        log.info("Order {} set for delivery retry", orderId);
        
        return getOrderDetail(shopId, sellerId, orderId);
    }
    
    /**
     * Get orders pending buyer confirmation (delivered but not confirmed)
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getPendingConfirmationOrders(Long shopId, Long sellerId) {
        verifyShopOwner(shopId, sellerId);
        log.info("Getting pending confirmation orders for shop: {}", shopId);
        
        List<OrderEntity> orders = orderRepo.findPendingConfirmationOrders(shopId, OrderStatus.DELIVERED.name());
        
        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();
        Map<Long, Long> itemCounts = getItemCountsMap(orderIds);
        
        return orders.stream()
                .map(order -> {
                    int itemCount = itemCounts.getOrDefault(order.getId(), 0L).intValue();
                    return OrderSummaryResponse.from(order, itemCount);
                })
                .toList();
    }

    /**
     * Validate status transitions
     */

    /**
     * Get order statistics for shop
     */
    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats(Long shopId, Long sellerId) {
        verifyShopOwner(shopId, sellerId);
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

    // === BATCH OPERATIONS ===

    @Transactional
    public List<OrderDetailResponse> batchShipOrders(Long shopId, Long sellerId, List<Long> orderIds) {
        verifyShopOwner(shopId, sellerId);
        log.info("Batch shipping {} orders for shop {}", orderIds.size(), shopId);

        List<OrderEntity> orders = orderRepo.findAllById(orderIds);
        List<OrderDetailResponse> responses = new java.util.ArrayList<>();

        for (OrderEntity order : orders) {
            try {
                if (!order.getShopId().equals(shopId)) continue;
                
                // Only process ready/processing orders
                String currentStatus = order.getStatus();
                if (!Set.of(OrderStatus.PROCESSING.name(), OrderStatus.READY_TO_SHIP.name()).contains(currentStatus)) {
                    continue;
                }

                // Auto generate tracking number
                String trackingNumber = "SPX" + System.currentTimeMillis() + "-" + order.getId();
                
                // Update Logic
                order.setStatus(OrderStatus.SHIPPED.name());
                order.setShippingProvider("Shopee Express");
                order.setTrackingNumber(trackingNumber);
                order.setShippedAt(LocalDateTime.now());
                order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3));
                
                orderRepo.save(order);
                
                // Log history
                orderHistoryService.recordSellerChange(order.getId(), currentStatus, 
                    OrderStatus.SHIPPED.name(), sellerId, "Batch shipping execution");
                
                responses.add(getOrderDetail(shopId, sellerId, order.getId()));
            } catch (Exception e) {
                log.error("Failed to batch ship order {}", order.getId(), e);
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public String generateBatchShippingLabels(Long shopId, Long sellerId, List<Long> orderIds) {
        verifyShopOwner(shopId, sellerId);
        List<OrderEntity> orders = orderRepo.findAllById(orderIds);
        
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Batch Print</title><style>")
            .append("body { font-family: Helvetica, Arial, sans-serif; font-size: 14px; }")
            .append("@media print { .no-print { display: none; } .page-break { page-break-after: always; } }")
            .append(".label { border: 2px solid #000; padding: 15px; margin: 20px auto; width: 380px; height: 500px; position: relative; box-sizing: border-box; }")
            .append(".header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 15px; }")
            .append(".logo { font-weight: bold; font-size: 24px; }")
            .append(".courier { font-weight: bold; font-size: 18px; border: 1px solid #000; padding: 5px 10px; }")
            .append(".barcode { background: #eee; height: 60px; margin: 15px 0; display: flex; align-items: center; justify-content: center; font-family: 'Courier New'; font-weight: bold; letter-spacing: 3px; border: 1px dashed #999; }")
            .append(".info-row { margin-bottom: 8px; display: flex; }")
            .append(".label-key { width: 100px; color: #666; }")
            .append(".label-val { flex: 1; font-weight: 600; }")
            .append(".cod-box { border: 2px solid #000; padding: 10px; text-align: center; margin-top: 20px; }")
            .append(".cod-amount { font-size: 24px; font-weight: bold; }")
            .append(".footer { position: absolute; bottom: 15px; left: 15px; right: 15px; font-size: 10px; text-align: center; color: #666; }")
            .append("</style></head><body>");
            
        html.append("<div class='no-print' style='text-align:center; padding: 20px; background: #f0f0f0;'>")
            .append("<button onclick='window.print()' style='padding: 10px 20px; font-size: 16px; cursor: pointer; background: #ee4d2d; color: white; border: none; border-radius: 4px;'>Print Labels</button>")
            .append("</div>");

        int count = 0;
        for (OrderEntity order : orders) {
            if (!order.getShopId().equals(shopId)) continue;
            
            // Fetch address
            var address = addressRepo.findById(order.getAddressId()).orElse(new com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity());
            String receiverName = address.getReceiverName() != null ? address.getReceiverName() : "N/A";
            String receiverPhone = address.getReceiverPhone() != null ? address.getReceiverPhone() : "N/A";
            String fullAddress = String.format("%s, %s, %s, %s", 
                address.getLine1() != null ? address.getLine1() : "", 
                address.getWard() != null ? address.getWard() : "",
                address.getDistrict() != null ? address.getDistrict() : "",
                address.getProvince() != null ? address.getProvince() : "").replace("null", "").replaceAll("[, ]+$", "");
            
            html.append("<div class='label'>")
                .append("<div class='header'>")
                .append("<span class='logo'>Shopee</span>")
                .append("<span class='courier'>STANDARD</span>")
                .append("</div>")
                
                .append("<div class='barcode'>").append(order.getTrackingNumber() != null ? order.getTrackingNumber() : "PENDING").append("</div>")
                
                .append("<div class='info-row'><span class='label-key'>Order ID:</span><span class='label-val'>#").append(order.getOrderCode()).append("</span></div>")
                .append("<div class='info-row'><span class='label-key'>Date:</span><span class='label-val'>").append(order.getCreatedAt().toLocalDate()).append("</span></div>")
                
                .append("<div style='margin: 15px 0; border-top: 1px dotted #ccc; padding-top: 10px;'>")
                .append("<div class='info-row'><span class='label-key'>Receiver:</span><span class='label-val'>").append(receiverName).append("</span></div>")
                .append("<div class='info-row'><span class='label-key'>Phone:</span><span class='label-val'>").append(receiverPhone).append("</span></div>")
                .append("<div class='info-row'><span class='label-key'>Address:</span><span class='label-val'>").append(fullAddress).append("</span></div>")
                .append("</div>")
                
                .append("<div class='cod-box'>")
                .append("<div>PLEASE COLLECT</div>")
                .append("<div class='cod-amount'>").append(String.format("%,d", order.getTotalAmount())).append(" VND</div>")
                .append("</div>")
                
                .append("<div class='footer'>")
                .append("Page ").append(++count).append(" of ").append(orders.size())
                .append(" | Powered by MetaShop System")
                .append("</div>")
                .append("</div>");
                
            if (count < orders.size()) {
                html.append("<div class='page-break'></div>");
            }
        }
        
        if (count == 0) {
            html.append("<div style='text-align:center; padding:50px;'>No orders selected or unauthorized.</div>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
}
