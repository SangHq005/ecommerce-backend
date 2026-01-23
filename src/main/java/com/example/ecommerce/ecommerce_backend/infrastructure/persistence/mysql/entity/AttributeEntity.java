package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "attribute")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "attributeGroup"})
public class AttributeEntity {
    
    public enum DataType {
        TEXT, NUMBER, BOOLEAN, ENUM
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "attribute_group_id", nullable = false)
    private Long attributeGroupId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_group_id", insertable = false, updatable = false)
    private AttributeGroupEntity attributeGroup;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private DataType dataType = DataType.TEXT;
    
    @Column(length = 50)
    private String unit;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Column(name = "is_filterable", nullable = false)
    private Boolean isFilterable = false;
    
    @Column(name = "is_comparable", nullable = false)
    private Boolean isComparable = true;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAttributeGroupId() { return attributeGroupId; }
    public void setAttributeGroupId(Long attributeGroupId) { this.attributeGroupId = attributeGroupId; }
    
    public AttributeGroupEntity getAttributeGroup() { return attributeGroup; }
    public void setAttributeGroup(AttributeGroupEntity attributeGroup) { this.attributeGroup = attributeGroup; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Boolean getIsFilterable() { return isFilterable; }
    public void setIsFilterable(Boolean isFilterable) { this.isFilterable = isFilterable; }
    
    public Boolean getIsComparable() { return isComparable; }
    public void setIsComparable(Boolean isComparable) { this.isComparable = isComparable; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
