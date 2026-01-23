package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductImageDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("productId")
    private Long productId;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("sortOrder")
    private Integer sortOrder;
    
    @JsonProperty("altText")
    private String altText;
    
    public ProductImageDTO() {}
    
    public ProductImageDTO(Long id, Long productId, String imageUrl, Integer sortOrder, String altText) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.altText = altText;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
}
