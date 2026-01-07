package com.example.ecommerce.ecommerce_backend.domain.event;

public final class EventTypes {
    private EventTypes(){}

    public static final String CATALOG_PRODUCT_CREATED = "CATALOG_PRODUCT_CREATED";
    public static final String CATALOG_PRODUCT_SUBMITTED = "CATALOG_PRODUCT_SUBMITTED";
    public static final String CATALOG_PRODUCT_STATUS_CHANGED = "CATALOG_PRODUCT_STATUS_CHANGED";
    public static final String CATALOG_SKU_UPSERTED = "CATALOG_SKU_UPSERTED";

    public static final String INVENTORY_ADJUSTED = "INVENTORY_ADJUSTED";
    public static final String INVENTORY_RESERVED = "INVENTORY_RESERVED";
    public static final String INVENTORY_RELEASED = "INVENTORY_RELEASED";
    public static final String INVENTORY_COMMITTED = "INVENTORY_COMMITTED";

    public static final String SEARCH_PERFORMED = "SEARCH_PERFORMED";
}
