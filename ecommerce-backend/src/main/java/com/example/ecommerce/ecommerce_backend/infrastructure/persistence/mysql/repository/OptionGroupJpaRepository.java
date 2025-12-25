package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionGroupJpaRepository extends JpaRepository<OptionGroupEntity, Long> {
    List<OptionGroupEntity> findByProductIdOrderBySortOrderAscIdAsc(Long productId);
    void deleteByProductId(Long productId);
}