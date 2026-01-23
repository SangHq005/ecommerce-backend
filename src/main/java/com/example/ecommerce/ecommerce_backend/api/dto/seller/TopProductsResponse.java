package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.util.List;

public record TopProductsResponse(
        String sortBy, // "revenue" or "quantity"
        List<TopProduct> products
) {
    public record TopProduct(
            Long productId,
            String productName,
            String mainImageUrl,
            Long revenue,
            Long quantitySold,
            Double averagePrice
    ) {}
}
