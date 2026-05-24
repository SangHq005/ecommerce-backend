package com.example.ecommerce.ecommerce_backend.application.service.recommendation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.ecommerce.ecommerce_backend.api.dto.chat.ChatMessageRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.chat.ChatMessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.chat.ConversationResponse;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.ChatMessageDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.ChatMessageMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserProfileJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageMongoRepository chatRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoTemplate mongoTemplate;
    private final UserJpaRepository userRepo;
    private final UserProfileJpaRepository userProfileRepo;

    public ChatMessageResponse sendMessage(Long senderId, ChatMessageRequest request) {
        String conversationId = getConversationId(senderId, request.recipientId());
        
        ChatMessageDocument doc = ChatMessageDocument.builder()
                .senderId(senderId)
                .recipientId(request.recipientId())
                .shopId(request.shopId())
                .content(request.content())
                .type(request.type() != null ? request.type() : "TEXT")
                .metadata(request.metadata())
                .conversationId(conversationId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        ChatMessageDocument saved = chatRepo.save(doc);
        
        ChatMessageResponse response = mapToResponse(saved);
        
        // Push to WebSocket
        // Destination: /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.recipientId()), 
                "/queue/messages", 
                response
        );
        
        return response;
    }

    public List<ChatMessageResponse> getHistory(Long userId1, Long userId2, Pageable pageable) {
        String conversationId = getConversationId(userId1, userId2);
        return chatRepo.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public List<ConversationResponse> getConversations(Long userId) {
        // MongoDB Aggregation to get latest message per conversation
        Aggregation aggregation = Aggregation.newAggregation(
            // Match messages where user is sender OR recipient
            Aggregation.match(new Criteria().orOperator(
                Criteria.where("senderId").is(userId),
                Criteria.where("recipientId").is(userId)
            )),
            // Sort by createdAt DESC
            Aggregation.sort(Sort.Direction.DESC, "createdAt"),
            // Group by conversationId, take the first (latest) document
            Aggregation.group("conversationId")
                .first("$$ROOT").as("latestMessage")
                .first("createdAt").as("lastMessageTime"),
            // Sort conversations by last message time
            Aggregation.sort(Sort.Direction.DESC, "lastMessageTime")
        );

        var results = mongoTemplate.aggregate(aggregation, "chat_messages", org.bson.Document.class).getMappedResults();
        
        List<ConversationResponse> conversations = new ArrayList<>();
        
        for (var doc : results) {
            var messageDoc = (org.bson.Document) doc.get("latestMessage");
            String conversationId = messageDoc.getString("conversationId");
            Long senderId = messageDoc.getLong("senderId");
            Long recipientId = messageDoc.getLong("recipientId");
            
            Long partnerId = senderId.equals(userId) ? recipientId : senderId;
            
            // Unread count: count messages in this conversation where recipient is ME and isRead is false
            long unread = chatRepo.countByConversationIdAndRecipientIdAndIsReadFalse(conversationId, userId);
            
            // Initial placeholder data
            String partnerName = "User " + partnerId;
            String partnerAvatar = null;
            
            conversations.add(new ConversationResponse(
                conversationId,
                partnerId,
                partnerName,
                partnerAvatar, 
                messageDoc.getString("content"),
                messageDoc.getDate("createdAt").toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                unread
            ));
        }
        
        // Batch fetch profiles to fill details
        fillPartnerDetails(conversations);
        
        return conversations;
    }
    
    private void fillPartnerDetails(List<ConversationResponse> conversations) {
        if (conversations.isEmpty()) return;
        List<Long> partnerIds = conversations.stream().map(ConversationResponse::partnerId).toList();
        
        var users = userRepo.findAllById(partnerIds);
        Map<Long, String> names = users.stream().collect(Collectors.toMap(u -> u.getId(), u -> u.getFullName()));
        
        // Fetch profiles for avatars (one by one for now as findById is cached by Hibernate usually, or use findAllById)
        // Since UserProfileEntity PK is userId, we can use findAllById
        var profiles = userProfileRepo.findAllById(partnerIds);
        Map<Long, String> avatars = profiles.stream().collect(Collectors.toMap(p -> p.getUserId(), p -> p.getAvatarUrl() != null ? p.getAvatarUrl() : ""));
        
        for (int i = 0; i < conversations.size(); i++) {
            ConversationResponse c = conversations.get(i);
            String name = names.getOrDefault(c.partnerId(), "User " + c.partnerId());
            String avatar = avatars.getOrDefault(c.partnerId(), null);
            if (avatar != null && avatar.isEmpty()) avatar = null;
            
            // Reconstruct record
            conversations.set(i, new ConversationResponse(
                c.conversationId(), c.partnerId(), name, avatar, c.lastMessage(), c.lastMessageTime(), c.unreadCount()
            ));
        }
    }
    
    private String getConversationId(Long id1, Long id2) {
        return Math.min(id1, id2) + "_" + Math.max(id1, id2);
    }
    
    private ChatMessageResponse mapToResponse(ChatMessageDocument doc) {
        return new ChatMessageResponse(
                doc.getId(),
                doc.getSenderId(),
                doc.getRecipientId(),
                doc.getContent(),
                doc.getType(),
                doc.getMetadata(),
                doc.isRead(),
                doc.getCreatedAt()
        );
    }
}
