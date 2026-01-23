package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = true)
    private Integer rating;

    @Lob
    private String comment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> images;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status;

    @ElementCollection
    @CollectionTable(name = "review_likes", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedUserIds = new java.util.HashSet<>();

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }

    public java.util.Set<Long> getLikedUserIds() { return likedUserIds; }
    public void setLikedUserIds(java.util.Set<Long> likedUserIds) { this.likedUserIds = likedUserIds; }

    public void addLike(Long userId) {
        if (likedUserIds.add(userId)) {
            helpfulCount++;
        }
    }

    public void removeLike(Long userId) {
        if (likedUserIds.remove(userId)) {
            helpfulCount--;
        }
    }

    public Instant getCreatedAt() { return createdAt; }
}
