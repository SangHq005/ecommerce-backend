package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);
    private final NotificationService notificationService;

    public OrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            String title = buildTitle(event.toStatus());
            String message = buildMessage(event);
            notificationService.createNotification(
                event.userId(), "ORDER_STATUS_CHANGED",
                title, message, "ORDER", event.orderId()
            );
        } catch (Exception e) {
            log.warn("Failed to send order notification for order {}: {}", event.orderCode(), e.getMessage());
        }
    }

    private String buildTitle(OrderStatus status) {
        return switch (status) {
            case CANCELLED -> "Đơn hàng đã bị hủy";
            case PROCESSING -> "Đơn hàng đang được xử lý";
            case SHIPPED -> "Đơn hàng đã được giao cho vận chuyển";
            case DELIVERED -> "Đơn hàng đã được giao";
            case COMPLETED -> "Đơn hàng đã hoàn thành";
            default -> "Cập nhật trạng thái đơn hàng";
        };
    }

    private String buildMessage(OrderStatusChangedEvent event) {
        return String.format("Đơn hàng %s: %s → %s",
            event.orderCode(), event.fromStatus(), event.toStatus());
    }
}
