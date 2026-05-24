package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderId(Long orderId);
    List<OrderItemEntity> findByProductId(Long productId);


    @Query("SELECT oi.orderId, COUNT(oi) FROM OrderItemEntity oi WHERE oi.orderId IN :orderIds GROUP BY oi.orderId")
    List<Object[]> countItemsByOrderIds(@Param("orderIds") List<Long> orderIds);


    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.orderId IN :orderIds")
    List<OrderItemEntity> findByOrderIdIn(@Param("orderIds") List<Long> orderIds);

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

    @Query("select oi.productId, sum(oi.quantity), sum(oi.totalPrice) " +
            "from OrderItemEntity oi join OrderEntity o on o.id = oi.orderId " +
            "where o.status in :statuses " +
            "group by oi.productId " +
            "order by sum(oi.quantity) desc")
    List<Object[]> aggregateProductSalesByStatus(@Param("statuses") List<String> statuses);

    @Query("select p.categoryId, count(distinct o.id), sum(oi.totalPrice) " +
            "from OrderItemEntity oi " +
            "join OrderEntity o on o.id = oi.orderId " +
            "join ProductEntity p on p.id = oi.productId " +
            "where o.status in :statuses " +
            "group by p.categoryId " +
            "order by sum(oi.totalPrice) desc")
    List<Object[]> aggregateCategorySalesByStatus(@Param("statuses") List<String> statuses);

    @Query("SELECT oi.productId, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItemEntity oi JOIN OrderEntity o ON o.id = oi.orderId " +
           "WHERE o.shopId = :shopId AND o.status IN :statuses " +
           "GROUP BY oi.productId ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> aggregateProductSalesByShopOrderByRevenue(
            @Param("shopId") Long shopId,
            @Param("statuses") List<String> statuses,
            Pageable pageable
    );

    @Query("SELECT oi.productId, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItemEntity oi JOIN OrderEntity o ON o.id = oi.orderId " +
           "WHERE o.shopId = :shopId AND o.status IN :statuses " +
           "GROUP BY oi.productId ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> aggregateProductSalesByShopOrderByQuantity(
            @Param("shopId") Long shopId,
            @Param("statuses") List<String> statuses,
            Pageable pageable
    );
}
