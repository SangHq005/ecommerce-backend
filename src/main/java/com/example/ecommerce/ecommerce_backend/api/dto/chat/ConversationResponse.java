package com.example.ecommerce.ecommerce_backend.api.dto.chat;

import java.time.LocalDateTime;

public record ConversationResponse(
    String conversationId,
    Long partnerId,
    String partnerName,
    String partnerAvatar,
    String lastMessage,
    LocalDateTime lastMessageTime,
    long unreadCount
) {}
