package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface SkuJpaRepository extends JpaRepository<SkuEntity, Long> {
    List<SkuEntity> findByProductIdOrderByIdAsc(Long productId);
    Optional<SkuEntity> findByProductIdAndOptionSignatureHash(Long productId, String optionSignatureHash);
    Optional<SkuEntity> findByProductIdAndSkuCode(Long productId, String skuCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SkuEntity s where s.id = :id")
    Optional<SkuEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("select min(s.price) from SkuEntity s where s.productId = :productId")
    Long findMinPriceByProductId(@Param("productId") Long productId);

    @Query("select s.productId, sum(s.stockOnHand - s.reservedStock) from SkuEntity s group by s.productId")
    List<Object[]> sumAvailableStockByProductId();

    @Query("SELECT s FROM SkuEntity s, ProductEntity p WHERE s.productId = p.id AND p.shopId = :shopId AND s.stockOnHand <= :threshold")
    List<SkuEntity> findLowStockByShop(@Param("shopId") Long shopId, @Param("threshold") int threshold);
}
