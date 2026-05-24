package com.example.ecommerce.ecommerce_backend.application.service.notification;

public interface NotificationChannel {
    boolean supports(String notificationType);
    void send(NotificationPayload payload);
}
