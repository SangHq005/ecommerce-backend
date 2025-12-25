package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.jti = :jti")
    Optional<RefreshTokenEntity> findByJtiForUpdate(@Param("jti") String jti);

    Optional<RefreshTokenEntity> findByJti(String jti);

    @Query("select t from RefreshTokenEntity t where t.userId = :userId and t.familyId = :familyId and t.revokedAt is null and t.expiresAt > :now")
    List<RefreshTokenEntity> findActiveByFamily(@Param("userId") Long userId, @Param("familyId") String familyId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("update RefreshTokenEntity t set t.revokedAt = :now where t.userId = :userId and t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(@Param("userId") Long userId, @Param("familyId") String familyId, @Param("now") LocalDateTime now);
}
