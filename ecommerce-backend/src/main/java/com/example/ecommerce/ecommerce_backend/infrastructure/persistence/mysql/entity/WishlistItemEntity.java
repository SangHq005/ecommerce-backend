package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wishlist_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_user_product", columnNames = {"user_id", "product_id"}))
public class WishlistItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @Column(length = 500)
    private String note;

    @PrePersist
    void prePersist() {
        addedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
