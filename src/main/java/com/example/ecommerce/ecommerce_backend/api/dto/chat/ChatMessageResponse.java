package com.example.ecommerce.ecommerce_backend.api.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    String id,
    Long senderId,
    Long recipientId,
    String content,
    String type,
    String metadata,
    boolean isRead,
    LocalDateTime createdAt
) {}
