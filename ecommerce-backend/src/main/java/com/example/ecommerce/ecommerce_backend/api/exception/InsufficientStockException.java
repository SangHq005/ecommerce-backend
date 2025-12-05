package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when there is insufficient stock for a product
 */
public class InsufficientStockException extends RuntimeException {

    private final Long skuId;
    private final Integer requested;
    private final Integer available;

    public InsufficientStockException(Long skuId, Integer requested, Integer available) {
        super(String.format("Insufficient stock for SKU %d. Requested: %d, Available: %d",
                skuId, requested, available));
        this.skuId = skuId;
        this.requested = requested;
        this.available = available;
    }

    public Long getSkuId() { return skuId; }
    public Integer getRequested() { return requested; }
    public Integer getAvailable() { return available; }
}
