package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.ChatMessageDocument;

@Repository
public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDocument, String> {
    
    List<ChatMessageDocument> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);
    
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    
    long countByConversationIdAndRecipientIdAndIsReadFalse(String conversationId, Long recipientId);
}
