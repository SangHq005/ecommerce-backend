package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderId(Long orderId);
    List<OrderItemEntity> findByProductId(Long productId);

    @Query("select count(oi) > 0 from OrderItemEntity oi " +
            "join OrderEntity o on o.id = oi.orderId " +
            "where oi.productId = :productId and o.userId = :userId and o.status in :statuses")
    boolean existsPurchasedProduct(@Param("userId") Long userId,
                                   @Param("productId") Long productId,
                                   @Param("statuses") List<String> statuses);

    @Query("select oi.orderId from OrderItemEntity oi " +
            "join OrderEntity o on o.id = oi.orderId " +
            "where oi.productId = :productId and o.userId = :userId and o.status in :statuses " +
            "order by o.createdAt desc")
    List<Long> findPurchasedOrderIds(@Param("userId") Long userId,
                                     @Param("productId") Long productId,
                                     @Param("statuses") List<String> statuses,
                                     Pageable pageable);
}
