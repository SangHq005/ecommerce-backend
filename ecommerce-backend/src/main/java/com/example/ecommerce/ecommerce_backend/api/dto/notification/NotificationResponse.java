package com.example.ecommerce.ecommerce_backend.api.dto.notification;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        Boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResponse from(NotificationEntity notification) {
        LocalDateTime createdAtLocal = notification.getCreatedAt() != null
                ? LocalDateTime.ofInstant(notification.getCreatedAt(), ZoneId.systemDefault())
                : null;

        LocalDateTime readAtLocal = notification.getReadAt() != null
                ? LocalDateTime.ofInstant(notification.getReadAt(), ZoneId.systemDefault())
                : null;

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getIsRead(),
                createdAtLocal,
                readAtLocal
        );
    }
}
