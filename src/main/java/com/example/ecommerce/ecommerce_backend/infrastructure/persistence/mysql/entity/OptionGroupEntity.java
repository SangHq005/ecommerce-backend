package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
}