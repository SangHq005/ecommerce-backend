package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "user_events")
public class UserEventDoc {
    @Id
    private String id;

    @Indexed
    private Long userId;

    @Indexed
    private String sessionId;

    @Indexed
    private Long productId;

    @Indexed
    private String eventType; // VIEW, ADD_TO_CART, REMOVE_FROM_CART, PURCHASE, WISHLIST_ADD

    private Long orderId;

    private Instant timestamp = Instant.now();

    private String metadata; // JSON-like string for extra info

    // getters setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
