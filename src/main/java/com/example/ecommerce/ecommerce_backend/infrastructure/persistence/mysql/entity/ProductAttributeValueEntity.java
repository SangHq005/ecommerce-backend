package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product_attribute_value",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_product_attribute",
           columnNames = {"product_id", "attribute_id"}
       ))
public class ProductAttributeValueEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", insertable = false, updatable = false)
    private AttributeEntity attribute;
    
    @Column(name = "value_text", length = 1000)
    private String valueText;
    
    @Column(name = "value_number", precision = 15, scale = 4)
    private BigDecimal valueNumber;
    
    @Column(name = "value_boolean")
    private Boolean valueBoolean;
    
    @Column(name = "display_value", nullable = false, length = 500)
    private String displayValue;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    
}
