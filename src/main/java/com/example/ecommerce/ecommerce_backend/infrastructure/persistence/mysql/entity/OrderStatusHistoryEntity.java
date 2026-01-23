package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entity for tracking order status changes.
 * Provides complete audit trail of order lifecycle.
 */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "from_status", length = 32)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 32)
    private String toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private ActorType actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(length = 500)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum ActorType {
        SYSTEM,
        BUYER,
        SELLER,
        ADMIN
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    // Factory methods for easy creation
    public static OrderStatusHistoryEntity create(
            Long orderId,
            String fromStatus,
            String toStatus,
            ActorType actorType,
            Long actorId,
            String reason,
            String note,
            Map<String, Object> metadata
    ) {
        OrderStatusHistoryEntity entity = new OrderStatusHistoryEntity();
        entity.setOrderId(orderId);
        entity.setFromStatus(fromStatus);
        entity.setToStatus(toStatus);
        entity.setActorType(actorType);
        entity.setActorId(actorId);
        entity.setReason(reason);
        entity.setNote(note);
        entity.setMetadata(metadata);
        return entity;
    }

    public static OrderStatusHistoryEntity systemChange(Long orderId, String fromStatus, String toStatus, String reason) {
        return create(orderId, fromStatus, toStatus, ActorType.SYSTEM, null, reason, null, null);
    }

    public static OrderStatusHistoryEntity buyerChange(Long orderId, String fromStatus, String toStatus, Long buyerId, String reason) {
        return create(orderId, fromStatus, toStatus, ActorType.BUYER, buyerId, reason, null, null);
    }

    public static OrderStatusHistoryEntity sellerChange(Long orderId, String fromStatus, String toStatus, Long sellerId, String reason) {
        return create(orderId, fromStatus, toStatus, ActorType.SELLER, sellerId, reason, null, null);
    }

    public static OrderStatusHistoryEntity adminChange(Long orderId, String fromStatus, String toStatus, Long adminId, String reason) {
        return create(orderId, fromStatus, toStatus, ActorType.ADMIN, adminId, reason, null, null);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }

    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }

    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
