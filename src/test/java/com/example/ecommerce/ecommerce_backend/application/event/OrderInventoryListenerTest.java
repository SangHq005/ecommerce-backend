package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderInventoryListenerTest {

    @Mock ReservationService reservationService;
    @InjectMocks OrderInventoryListener listener;

    @Test
    void onStatusChanged_releasesAndRestoresReservation_whenOrderCancelled() {
        var event = OrderStatusChangedEvent.of(1L, "ORD-001", 100L,
            OrderStatus.SUBMITTED, OrderStatus.CANCELLED, 42L, "CUSTOMER");

        listener.onOrderStatusChanged(event);

        verify(reservationService).release("ORD-001");
        verify(reservationService).restore("ORD-001");
    }

    @Test
    void onStatusChanged_doesNothing_whenOrderNotCancelled() {
        var event = OrderStatusChangedEvent.of(1L, "ORD-001", 100L,
            OrderStatus.SUBMITTED, OrderStatus.PROCESSING, 99L, "SELLER");

        listener.onOrderStatusChanged(event);

        verifyNoInteractions(reservationService);
    }
}
