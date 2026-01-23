package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductAttributeValueEntity;

@Repository
public interface ProductAttributeValueJpaRepository extends JpaRepository<ProductAttributeValueEntity, Long> {
    
    /**
     * Find all attribute values for given products, eager loading attribute and group.
     */
    @Query("""
        SELECT pav FROM ProductAttributeValueEntity pav
        JOIN FETCH pav.attribute a
        JOIN FETCH a.attributeGroup ag
        WHERE pav.productId IN :productIds
        AND a.isActive = true
        AND a.isComparable = true
        AND ag.isActive = true
        ORDER BY ag.sortOrder, a.sortOrder
    """)
    List<ProductAttributeValueEntity> findByProductIdsWithAttributeAndGroup(@Param("productIds") List<Long> productIds);
    
    /**
     * Find attribute values for a single product.
     */
    List<ProductAttributeValueEntity> findByProductId(Long productId);
    
    /**
     * Find all attribute values for a single product, eager loading attribute and group.
     */
    @Query("""
        SELECT pav FROM ProductAttributeValueEntity pav
        JOIN FETCH pav.attribute a
        JOIN FETCH a.attributeGroup ag
        WHERE pav.productId = :productId
        AND a.isActive = true
        AND ag.isActive = true
        ORDER BY ag.sortOrder, a.sortOrder
    """)
    List<ProductAttributeValueEntity> findByProductIdWithAttributeAndGroup(@Param("productId") Long productId);
}
