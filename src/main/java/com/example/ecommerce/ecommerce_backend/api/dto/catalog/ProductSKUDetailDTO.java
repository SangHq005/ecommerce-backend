package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductSKUDetailDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("skuCode")
    private String skuCode;
    
    @JsonProperty("price")
    private Long price;
    
    @JsonProperty("compareAtPrice")
    private Long compareAtPrice;
    
    @JsonProperty("stockOnHand")
    private Integer stockOnHand;
    
    @JsonProperty("reservedStock")
    private Integer reservedStock;
    
    @JsonProperty("availableStock")
    private Integer availableStock;
    
    @JsonProperty("optionSignature")
    private String optionSignature;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    public ProductSKUDetailDTO() {}
    
    public ProductSKUDetailDTO(Long id, String skuCode, Long price, Long compareAtPrice,
                              Integer stockOnHand, Integer reservedStock, String optionSignature,
                              Boolean isActive, String imageUrl) {
        this.id = id;
        this.skuCode = skuCode;
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.stockOnHand = stockOnHand;
        this.reservedStock = reservedStock;
        this.availableStock = stockOnHand - reservedStock;
        this.optionSignature = optionSignature;
        this.isActive = isActive;
        this.imageUrl = imageUrl;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Long getCompareAtPrice() { return compareAtPrice; }
    public void setCompareAtPrice(Long compareAtPrice) { this.compareAtPrice = compareAtPrice; }
    public Integer getStockOnHand() { return stockOnHand; }
    public void setStockOnHand(Integer stockOnHand) { this.stockOnHand = stockOnHand; }
    public Integer getReservedStock() { return reservedStock; }
    public void setReservedStock(Integer reservedStock) { this.reservedStock = reservedStock; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public String getOptionSignature() { return optionSignature; }
    public void setOptionSignature(String optionSignature) { this.optionSignature = optionSignature; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
