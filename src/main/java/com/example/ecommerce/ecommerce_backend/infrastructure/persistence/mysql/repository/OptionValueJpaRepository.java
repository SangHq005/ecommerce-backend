package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionValueJpaRepository extends JpaRepository<OptionValueEntity, Long> {
    List<OptionValueEntity> findByOptionGroupIdOrderBySortOrderAscIdAsc(Long optionGroupId);
    void deleteByOptionGroupId(Long optionGroupId);
}