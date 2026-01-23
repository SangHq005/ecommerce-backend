package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import java.util.List;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionValueEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductAttributeValueEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;

public record ProductDetailsResponse(
    ProductEntity product,
    List<ProductImageEntity> images,
    List<SkuEntity> skus,
    List<OptionGroupDetails> options,
    SellerShopEntity shop,
    List<AttributeGroupDetails> attributes
) {
    public record OptionGroupDetails(
        OptionGroupEntity group,
        List<OptionValueEntity> values
    ) {}
    
    public record AttributeGroupDetails(
        AttributeGroupEntity group,
        List<AttributeValueDetails> attributes
    ) {}
    
    public record AttributeValueDetails(
        AttributeEntity attribute,
        ProductAttributeValueEntity value
    ) {}
}
