package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_category_affinity")
public class UserCategoryAffinityDoc {
    @Id
    private String id;

    @Indexed
    private Long userId;

    @Indexed
    private String sessionId;

    @Indexed
    private Long categoryId;

    private Long viewCount;
    private Long purchaseCount;

    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Long getPurchaseCount() { return purchaseCount; }
    public void setPurchaseCount(Long purchaseCount) { this.purchaseCount = purchaseCount; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Increment view count and update affinity score
     */
    public void incrementView() {
        if (this.viewCount == null) {
            this.viewCount = 0L;
        }
        this.viewCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * Increment purchase count and update affinity score
     */
    public void incrementPurchase() {
        if (this.purchaseCount == null) {
            this.purchaseCount = 0L;
        }
        this.purchaseCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * Calculate affinity score: views * 1 + purchases * 10
     */
    public Long getScore() {
        long views = (viewCount != null) ? viewCount : 0L;
        long purchases = (purchaseCount != null) ? purchaseCount : 0L;
        return views + (purchases * 10);
    }
}
