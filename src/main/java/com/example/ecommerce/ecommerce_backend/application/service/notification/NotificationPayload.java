package com.example.ecommerce.ecommerce_backend.application.service.notification;

public record NotificationPayload(
    Long userId,
    String type,
    String title,
    String message,
    String referenceType,
    Long referenceId
) {}
