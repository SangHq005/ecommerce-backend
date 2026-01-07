package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.IdempotencyKeyEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, Long> {
    Optional<IdempotencyKeyEntity> findByScopeAndIdemKey(String scope, String idemKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM IdempotencyKeyEntity e WHERE e.scope = :scope AND e.idemKey = :idemKey")
    Optional<IdempotencyKeyEntity> findForUpdate(@Param("scope") String scope, @Param("idemKey") String idemKey);
}
