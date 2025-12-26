package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, Long> {
    Optional<StockReservationEntity> findByOrderTokenAndSkuId(String orderToken, Long skuId);
    List<StockReservationEntity> findByOrderToken(String orderToken);

    @Query("SELECT s FROM StockReservationEntity s WHERE s.status = 'RESERVED' AND s.expiresAt < :now")
    List<StockReservationEntity> findExpiredReservations(@Param("now") LocalDateTime now);
}
