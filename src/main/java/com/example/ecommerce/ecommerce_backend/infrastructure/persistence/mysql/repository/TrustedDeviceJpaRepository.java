package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.TrustedDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrustedDeviceJpaRepository extends JpaRepository<TrustedDeviceEntity, Long> {
    Optional<TrustedDeviceEntity> findByDeviceIdAndUser_PhoneNumber(String deviceId, String phoneNumber);
}
