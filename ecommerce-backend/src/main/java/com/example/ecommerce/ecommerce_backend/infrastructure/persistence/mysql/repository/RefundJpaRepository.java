package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefundEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundJpaRepository extends JpaRepository<RefundEntity, Long> {

    Page<RefundEntity> findByUserId(Long userId, Pageable pageable);

    Page<RefundEntity> findByShopId(Long shopId, Pageable pageable);

    Page<RefundEntity> findByStatus(String status, Pageable pageable);

    Page<RefundEntity> findByShopIdAndStatus(Long shopId, String status, Pageable pageable);

    Page<RefundEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    List<RefundEntity> findByOrderId(Long orderId);

    long countByShopIdAndStatus(Long shopId, String status);
}
