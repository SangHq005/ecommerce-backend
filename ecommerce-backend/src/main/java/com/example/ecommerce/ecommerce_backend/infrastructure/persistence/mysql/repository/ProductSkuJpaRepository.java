package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductSkuEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ProductSkuJpaRepository extends JpaRepository<ProductSkuEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductSkuEntity s where s.id = :id")
    Optional<ProductSkuEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("select min(s.price) from ProductSkuEntity s where s.productId = :productId")
    Long findMinPriceByProductId(@Param("productId") Long productId);
}
