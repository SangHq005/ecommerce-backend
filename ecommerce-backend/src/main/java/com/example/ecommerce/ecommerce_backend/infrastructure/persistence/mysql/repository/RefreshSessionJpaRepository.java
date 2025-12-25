package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshSessionJpaRepository extends JpaRepository<RefreshSessionEntity, Long> {

    Optional<RefreshSessionEntity> findByRefreshJti(String refreshJti);

    @Query("SELECT s FROM RefreshSessionEntity s WHERE s.sessionRootJti = :sessionRootJti")
    List<RefreshSessionEntity> findBySessionRootJti(@Param("sessionRootJti") String sessionRootJti);

    List<RefreshSessionEntity> findByUserId(Long userId);
}
