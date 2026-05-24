package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByActiveTrueOrderBySortOrderAscIdAsc();
    boolean existsBySlug(String slug);
    boolean existsByParentId(Long parentId);
}