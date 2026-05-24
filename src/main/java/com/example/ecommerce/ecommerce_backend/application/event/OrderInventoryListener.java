package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInventoryListener {

    private final ReservationService reservationService;

    public OrderInventoryListener(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.toStatus() == OrderStatus.CANCELLED) {
            reservationService.release(event.orderCode());
            reservationService.restore(event.orderCode());
        }
    }
}
