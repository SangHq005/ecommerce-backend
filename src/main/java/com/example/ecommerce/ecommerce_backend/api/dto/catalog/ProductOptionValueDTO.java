package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductOptionValueDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("sortOrder")
    private Integer sortOrder;
    
    public ProductOptionValueDTO() {}
    
    public ProductOptionValueDTO(Long id, String value, Integer sortOrder) {
        this.id = id;
        this.value = value;
        this.sortOrder = sortOrder;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
