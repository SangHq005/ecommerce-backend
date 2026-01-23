package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OptionValueJpaRepository extends JpaRepository<OptionValueEntity, Long> {
    List<OptionValueEntity> findByOptionGroupIdOrderBySortOrderAscIdAsc(Long optionGroupId);
    void deleteByOptionGroupId(Long optionGroupId);
    
    /**
     * Fetch all option values for multiple option groups in one query.
     * This optimizes the N+1 query problem.
     */
    @Query("SELECT ov FROM OptionValueEntity ov WHERE ov.optionGroupId IN :groupIds ORDER BY ov.optionGroupId, ov.sortOrder, ov.id")
    List<OptionValueEntity> findByOptionGroupIdsOrderByGroupIdSortOrderAscIdAsc(@Param("groupIds") List<Long> groupIds);
}
