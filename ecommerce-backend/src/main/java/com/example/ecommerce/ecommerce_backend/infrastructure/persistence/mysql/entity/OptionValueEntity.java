package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="product_option_value",
        uniqueConstraints=@UniqueConstraint(name="uk_ov_value", columnNames={"option_group_id","value"}))
public class OptionValueEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="option_group_id", nullable=false)
    private Long optionGroupId;

    @Column(nullable=false, length=64)
    private String value;

    @Column(name="sort_order", nullable=false)
    private int sortOrder;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getOptionGroupId() { return optionGroupId; }
    public void setOptionGroupId(Long optionGroupId) { this.optionGroupId = optionGroupId; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
