package com.example.ecommerce.ecommerce_backend.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for AI Assistant queries.
 * Contains formatted response and structured data.
 */
public class AiAssistantResponse {
    
    @JsonProperty("response")
    private String response;
    
    @JsonProperty("formattedResponse")
    private String formattedResponse;
    
    @JsonProperty("format")
    private String format;
    
    @JsonProperty("metadata")
    private ResponseMetadata metadata;
    
    @JsonProperty("products")
    private List<ProductData> products;
    
    @JsonProperty("comparison")
    private ComparisonData comparison;
    
    public AiAssistantResponse() {}
    
    public AiAssistantResponse(String response, String format) {
        this.response = response;
        this.formattedResponse = response;
        this.format = format;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getFormattedResponse() {
        return formattedResponse;
    }
    
    public void setFormattedResponse(String formattedResponse) {
        this.formattedResponse = formattedResponse;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public ResponseMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(ResponseMetadata metadata) {
        this.metadata = metadata;
    }
    
    public List<ProductData> getProducts() {
        return products;
    }
    
    public void setProducts(List<ProductData> products) {
        this.products = products;
    }
    
    public ComparisonData getComparison() {
        return comparison;
    }
    
    public void setComparison(ComparisonData comparison) {
        this.comparison = comparison;
    }
    
    /**
     * Metadata about the response.
     */
    public static class ResponseMetadata {
        @JsonProperty("queryType")
        private String queryType; // "DETAIL", "COMPARISON", "SEARCH", "GENERAL"
        
        @JsonProperty("productCount")
        private Integer productCount;
        
        @JsonProperty("timestamp")
        private String timestamp;
        
        @JsonProperty("processingTimeMs")
        private Long processingTimeMs;
        
        public ResponseMetadata() {}
        
        public String getQueryType() {
            return queryType;
        }
        
        public void setQueryType(String queryType) {
            this.queryType = queryType;
        }
        
        public Integer getProductCount() {
            return productCount;
        }
        
        public void setProductCount(Integer productCount) {
            this.productCount = productCount;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public Long getProcessingTimeMs() {
            return processingTimeMs;
        }
        
        public void setProcessingTimeMs(Long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
        }
    }
    
    /**
     * Structured product data.
     */
    public static class ProductData {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("slug")
        private String slug;
        
        @JsonProperty("price")
        private Long price;
        
        @JsonProperty("originalPrice")
        private Long originalPrice;
        
        @JsonProperty("rating")
        private java.math.BigDecimal rating;
        
        @JsonProperty("reviewCount")
        private Integer reviewCount;
        
        @JsonProperty("specifications")
        private Map<String, Object> specifications;
        
        @JsonProperty("variants")
        private List<Map<String, Object>> variants;
        
        @JsonProperty("images")
        private List<String> images;
        
        public ProductData() {}
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public Long getPrice() { return price; }
        public void setPrice(Long price) { this.price = price; }
        public Long getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(Long originalPrice) { this.originalPrice = originalPrice; }
        public java.math.BigDecimal getRating() { return rating; }
        public void setRating(java.math.BigDecimal rating) { this.rating = rating; }
        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
        public Map<String, Object> getSpecifications() { return specifications; }
        public void setSpecifications(Map<String, Object> specifications) { this.specifications = specifications; }
        public List<Map<String, Object>> getVariants() { return variants; }
        public void setVariants(List<Map<String, Object>> variants) { this.variants = variants; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
    }
    
    /**
     * Comparison data for multiple products.
     */
    public static class ComparisonData {
        @JsonProperty("products")
        private List<ProductData> products;
        
        @JsonProperty("differences")
        private List<Difference> differences;
        
        @JsonProperty("similarities")
        private List<String> similarities;
        
        @JsonProperty("recommendation")
        private String recommendation;
        
        public ComparisonData() {}
        
        public List<ProductData> getProducts() { return products; }
        public void setProducts(List<ProductData> products) { this.products = products; }
        public List<Difference> getDifferences() { return differences; }
        public void setDifferences(List<Difference> differences) { this.differences = differences; }
        public List<String> getSimilarities() { return similarities; }
        public void setSimilarities(List<String> similarities) { this.similarities = similarities; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        
        public static class Difference {
            @JsonProperty("attribute")
            private String attribute;
            
            @JsonProperty("values")
            private Map<Long, String> values; // productId -> value
            
            public Difference() {}
            
            public String getAttribute() { return attribute; }
            public void setAttribute(String attribute) { this.attribute = attribute; }
            public Map<Long, String> getValues() { return values; }
            public void setValues(Map<Long, String> values) { this.values = values; }
        }
    }
}
