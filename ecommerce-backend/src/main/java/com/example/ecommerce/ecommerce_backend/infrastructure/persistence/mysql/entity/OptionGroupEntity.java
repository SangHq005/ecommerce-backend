package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="product_option_group",
        uniqueConstraints=@UniqueConstraint(name="uk_og_name", columnNames={"product_id","name"}))
public class OptionGroupEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(nullable=false, length=64)
    private String name;

    @Column(name="sort_order", nullable=false)
    private int sortOrder;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}