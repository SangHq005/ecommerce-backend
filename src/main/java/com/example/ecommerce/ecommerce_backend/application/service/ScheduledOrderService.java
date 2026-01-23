package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;

/**
 * Scheduled service for order-related background tasks.
 * 
 * Tasks:
 * 1. Auto-complete delivered orders after 7 days if buyer doesn't confirm
 * 2. Send reminders to buyers to confirm receipt
 */
@Service
public class ScheduledOrderService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledOrderService.class);
    
    // Auto-complete orders 7 days after delivery
    private static final int AUTO_COMPLETE_DAYS = 7;
    
    // Reminder after 3 days of delivery
    private static final int REMINDER_DAYS = 3;

    private final OrderJpaRepository orderRepo;
    private final NotificationService notificationService;
    private final OrderStatusHistoryService orderHistoryService;

    public ScheduledOrderService(
            OrderJpaRepository orderRepo,
            NotificationService notificationService,
            OrderStatusHistoryService orderHistoryService
    ) {
        this.orderRepo = orderRepo;
        this.notificationService = notificationService;
        this.orderHistoryService = orderHistoryService;
    }

    /**
     * Auto-complete delivered orders after 7 days.
     * Runs every hour.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void autoCompleteDeliveredOrders() {
        log.info("Running auto-complete job for delivered orders...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Find orders where auto_complete_at has passed
        List<OrderEntity> ordersToComplete = orderRepo.findOrdersToAutoComplete(
                OrderStatus.DELIVERED.name(),
                now
        );
        
        int completedCount = 0;
        for (OrderEntity order : ordersToComplete) {
            try {
                String previousStatus = order.getStatus();
                order.setStatus(OrderStatus.COMPLETED.name());
                order.setCompletedAt(now);
                order.setBuyerConfirmed(false); // Auto-completed, not buyer confirmed
                orderRepo.save(order);
                
                // Log status change
                orderHistoryService.recordSystemChange(order.getId(), previousStatus, 
                    OrderStatus.COMPLETED.name(), "Auto-completed after 7 days without buyer confirmation");
                
                // Notify buyer that order was auto-completed
                notificationService.createNotification(
                        order.getUserId(),
                        "ORDER_AUTO_COMPLETED",
                        "Đơn hàng đã hoàn thành",
                        String.format("Đơn hàng %s đã được tự động hoàn thành sau 7 ngày. Cảm ơn bạn đã mua sắm!", 
                                order.getOrderCode()),
                        "ORDER",
                        order.getId()
                );
                
                completedCount++;
                log.info("Auto-completed order: {}", order.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to auto-complete order {}: {}", order.getOrderCode(), e.getMessage());
            }
        }
        
        log.info("Auto-complete job finished. Completed {} orders.", completedCount);
    }

    /**
     * Send reminder to buyers to confirm receipt after 3 days of delivery.
     * Runs daily at 10 AM.
     */
    @Scheduled(cron = "0 0 10 * * *") // Daily at 10:00 AM
    @Transactional(readOnly = true)
    public void sendReceiptReminders() {
        log.info("Running receipt reminder job...");
        
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(REMINDER_DAYS);
        LocalDateTime autoCompleteThreshold = LocalDateTime.now().minusDays(AUTO_COMPLETE_DAYS);
        
        // Find delivered orders between 3-7 days that haven't been confirmed
        List<OrderEntity> ordersNeedingReminder = orderRepo.findDeliveredOrdersNeedingReminder(
                OrderStatus.DELIVERED.name(),
                autoCompleteThreshold,
                reminderThreshold
        );
        
        int reminderCount = 0;
        for (OrderEntity order : ordersNeedingReminder) {
            try {
                // Calculate days until auto-complete
                long daysRemaining = java.time.Duration.between(
                        LocalDateTime.now(), 
                        order.getAutoCompleteAt()
                ).toDays();
                
                notificationService.createNotification(
                        order.getUserId(),
                        "ORDER_CONFIRM_REMINDER",
                        "Xác nhận nhận hàng",
                        String.format("Đơn hàng %s đã được giao. Vui lòng xác nhận nhận hàng trong %d ngày hoặc đơn hàng sẽ tự động hoàn thành.",
                                order.getOrderCode(), Math.max(1, daysRemaining)),
                        "ORDER",
                        order.getId()
                );
                
                reminderCount++;
            } catch (Exception e) {
                log.error("Failed to send reminder for order {}: {}", order.getOrderCode(), e.getMessage());
            }
        }
        
        log.info("Reminder job finished. Sent {} reminders.", reminderCount);
    }

    /**
     * Update orders to DELIVERED status from shipping provider webhooks.
     * This would typically be called from a webhook handler, not scheduled.
     */
    @Transactional
    public void markOrderDelivered(String orderCode) {
        OrderEntity order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));
        
        if (!OrderStatus.SHIPPED.name().equals(order.getStatus()) &&
            !OrderStatus.IN_TRANSIT.name().equals(order.getStatus()) &&
            !OrderStatus.OUT_FOR_DELIVERY.name().equals(order.getStatus())) {
            throw new IllegalStateException("Order " + orderCode + " is not in shipping status");
        }
        
        String previousStatus = order.getStatus();
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.DELIVERED.name());
        order.setDeliveredAt(now);
        order.setAutoCompleteAt(now.plusDays(AUTO_COMPLETE_DAYS));
        orderRepo.save(order);
        
        // Log status change
        orderHistoryService.recordSystemChange(order.getId(), previousStatus, 
            OrderStatus.DELIVERED.name(), "Delivery confirmed by shipping provider");
        
        // Notify buyer
        notificationService.createNotification(
                order.getUserId(),
                "ORDER_DELIVERED",
                "Đơn hàng đã giao",
                String.format("Đơn hàng %s đã được giao thành công! Vui lòng xác nhận nhận hàng hoặc đơn hàng sẽ tự động hoàn thành sau %d ngày.",
                        order.getOrderCode(), AUTO_COMPLETE_DAYS),
                "ORDER",
                order.getId()
        );
        
        log.info("Order {} marked as delivered. Auto-complete scheduled for {}", 
                orderCode, order.getAutoCompleteAt());
    }

    /**
     * Buyer confirms receipt of order
     */
    @Transactional
    public void buyerConfirmReceipt(Long userId, String orderCode) {
        OrderEntity order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }
        
        if (!OrderStatus.DELIVERED.name().equals(order.getStatus())) {
            throw new IllegalStateException("Can only confirm receipt for DELIVERED orders");
        }
        
        String previousStatus = order.getStatus();
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setBuyerConfirmed(true);
        order.setBuyerConfirmedAt(now);
        order.setCompletedAt(now);
        orderRepo.save(order);
        
        // Log status change
        orderHistoryService.recordBuyerChange(order.getId(), previousStatus, 
            OrderStatus.COMPLETED.name(), userId, "Buyer confirmed receipt");
        
        // TODO: Notify seller - would need to lookup seller's userId from shopId
        // For now, just log the event. In production, inject SellerShopJpaRepository to get seller userId
        log.info("Buyer confirmed receipt for order {}. Seller (shop {}) should be notified.", 
                order.getOrderCode(), order.getShopId());
        
        log.info("Buyer confirmed receipt for order {}", orderCode);
    }

    /**
     * Get auto-complete days configuration
     */
    public int getAutoCompleteDays() {
        return AUTO_COMPLETE_DAYS;
    }
}
