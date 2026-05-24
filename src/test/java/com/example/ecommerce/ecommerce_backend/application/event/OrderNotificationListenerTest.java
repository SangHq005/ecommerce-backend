package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderNotificationListenerTest {

    @Mock NotificationService notificationService;
    @InjectMocks OrderNotificationListener listener;

    @Test
    void onStatusChanged_sendsNotification_whenOrderCancelled() {
        var event = OrderStatusChangedEvent.of(1L, "ORD-001", 100L,
            OrderStatus.SUBMITTED, OrderStatus.CANCELLED, 42L, "SELLER");

        listener.onOrderStatusChanged(event);

        verify(notificationService).createNotification(
            eq(100L),
            eq("ORDER_STATUS_CHANGED"),
            anyString(),
            anyString(),
            eq("ORDER"),
            eq(1L)
        );
    }
}
