package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeJpaRepository extends JpaRepository<AttributeEntity, Long> {
    
    @Query("SELECT a FROM AttributeEntity a WHERE a.isActive = true AND a.isComparable = true ORDER BY a.attributeGroupId, a.sortOrder")
    List<AttributeEntity> findAllComparable();
    
    @Query("SELECT a FROM AttributeEntity a WHERE a.attributeGroupId = :groupId AND a.isActive = true AND a.isComparable = true ORDER BY a.sortOrder")
    List<AttributeEntity> findByGroupIdComparable(@Param("groupId") Long groupId);
    
    org.springframework.data.domain.Page<AttributeEntity> findByAttributeGroupId(Long groupId, org.springframework.data.domain.Pageable pageable);
    boolean existsBySlug(String slug);
    boolean existsByAttributeGroupId(Long groupId);
}
