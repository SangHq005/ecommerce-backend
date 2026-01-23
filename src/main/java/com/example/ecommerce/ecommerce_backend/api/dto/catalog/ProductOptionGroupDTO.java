package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProductOptionGroupDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("sortOrder")
    private Integer sortOrder;
    
    @JsonProperty("values")
    private List<ProductOptionValueDTO> values;
    
    public ProductOptionGroupDTO() {}
    
    public ProductOptionGroupDTO(Long id, String name, Integer sortOrder, List<ProductOptionValueDTO> values) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.values = values;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public List<ProductOptionValueDTO> getValues() { return values; }
    public void setValues(List<ProductOptionValueDTO> values) { this.values = values; }
}
