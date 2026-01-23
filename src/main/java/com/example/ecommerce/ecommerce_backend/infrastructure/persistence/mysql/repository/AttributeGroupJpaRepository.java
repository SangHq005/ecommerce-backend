package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeGroupJpaRepository extends JpaRepository<AttributeGroupEntity, Long> {
    
    @Query("SELECT ag FROM AttributeGroupEntity ag WHERE ag.isActive = true ORDER BY ag.sortOrder")
    List<AttributeGroupEntity> findAllActiveOrderBySortOrder();
}
