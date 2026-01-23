package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDocument {
    @Id
    private String id;

    @Indexed
    private Long senderId;
    
    @Indexed
    private Long recipientId; 
    
    @Indexed
    private Long shopId;

    private String content;
    
    private String type; // TEXT, IMAGE, PRODUCT
    
    private String metadata; // JSON string for product info if type=PRODUCT
    
    @Indexed
    private String conversationId;

    private boolean isRead;
    
    private LocalDateTime createdAt;
}
