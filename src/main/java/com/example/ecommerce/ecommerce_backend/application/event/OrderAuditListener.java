package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.admin.AuditService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditListener {

    private final AuditService auditService;

    public OrderAuditListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        auditService.log(
            "ORDER_STATUS_CHANGED",
            event.actorId(),
            event.actorRole(),
            "Order " + event.orderCode() + ": " + event.fromStatus() + " → " + event.toStatus()
        );
    }
}
