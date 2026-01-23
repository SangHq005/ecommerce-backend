package com.example.ecommerce.ecommerce_backend.api.dto.chat;

public record ChatMessageRequest(
    Long recipientId,
    Long shopId,
    String content,
    String type,
    String metadata
) {}
