package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByOrderId(Long orderId);
    Optional<PaymentEntity> findByTransactionId(String transactionId);
    Optional<PaymentEntity> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
