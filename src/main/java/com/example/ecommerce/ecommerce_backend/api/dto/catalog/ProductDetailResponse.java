package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class ProductDetailResponse {
    
    @JsonProperty("product")
    private ProductInfo product;
    
    @JsonProperty("images")
    private List<ProductImageDTO> images;
    
    @JsonProperty("optionGroups")
    private List<ProductOptionGroupDTO> optionGroups;
    
    @JsonProperty("skus")
    private List<ProductSKUDetailDTO> skus;
    
    public ProductDetailResponse() {}
    
    public ProductDetailResponse(ProductInfo product, List<ProductImageDTO> images, 
                                List<ProductOptionGroupDTO> optionGroups, List<ProductSKUDetailDTO> skus) {
        this.product = product;
        this.images = images;
        this.optionGroups = optionGroups;
        this.skus = skus;
    }
    
    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }
    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }
    public List<ProductOptionGroupDTO> getOptionGroups() { return optionGroups; }
    public void setOptionGroups(List<ProductOptionGroupDTO> optionGroups) { this.optionGroups = optionGroups; }
    public List<ProductSKUDetailDTO> getSkus() { return skus; }
    public void setSkus(List<ProductSKUDetailDTO> skus) { this.skus = skus; }
    
    public static class ProductInfo {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("slug")
        private String slug;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("basePrice")
        private Long basePrice;
        
        @JsonProperty("mainImageUrl")
        private String mainImageUrl;
        
        @JsonProperty("averageRating")
        private BigDecimal averageRating;
        
        @JsonProperty("reviewCount")
        private Integer reviewCount;
        
        @JsonProperty("seller")
        private SellerInfo seller;
        
        public ProductInfo() {}
        
        public ProductInfo(Long id, String name, String slug, String description, Long basePrice,
                          String mainImageUrl, BigDecimal averageRating, Integer reviewCount, SellerInfo seller) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.description = description;
            this.basePrice = basePrice;
            this.mainImageUrl = mainImageUrl;
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
            this.seller = seller;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getBasePrice() { return basePrice; }
        public void setBasePrice(Long basePrice) { this.basePrice = basePrice; }
        public String getMainImageUrl() { return mainImageUrl; }
        public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
        public BigDecimal getAverageRating() { return averageRating; }
        public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
        public SellerInfo getSeller() { return seller; }
        public void setSeller(SellerInfo seller) { this.seller = seller; }
    }
    
    public static class SellerInfo {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("rating")
        private BigDecimal rating;
        
        public SellerInfo() {}
        
        public SellerInfo(Long id, String name, BigDecimal rating) {
            this.id = id;
            this.name = name;
            this.rating = rating;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getRating() { return rating; }
        public void setRating(BigDecimal rating) { this.rating = rating; }
    }
}
