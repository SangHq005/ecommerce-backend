package com.example.ecommerce.ecommerce_backend.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for AI Assistant queries.
 */
public class AiAssistantRequest {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("productIds")
    private java.util.List<Long> productIds;
    
    @JsonProperty("format")
    private ResponseFormat format = ResponseFormat.MARKDOWN;
    
    public AiAssistantRequest() {}
    
    public AiAssistantRequest(String query) {
        this.query = query;
    }
    
    public AiAssistantRequest(String query, java.util.List<Long> productIds) {
        this.query = query;
        this.productIds = productIds;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public java.util.List<Long> getProductIds() {
        return productIds;
    }
    
    public void setProductIds(java.util.List<Long> productIds) {
        this.productIds = productIds;
    }
    
    public ResponseFormat getFormat() {
        return format;
    }
    
    public void setFormat(ResponseFormat format) {
        this.format = format;
    }
    
    public enum ResponseFormat {
        MARKDOWN,
        HTML,
        JSON,
        TEXT
    }
}
