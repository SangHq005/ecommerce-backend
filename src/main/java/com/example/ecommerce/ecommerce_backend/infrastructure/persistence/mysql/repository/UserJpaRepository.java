package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;


import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    Optional<UserEntity> findByGoogleSub(String googleSub);

    long countByStatus(String status);
    long countByCreatedAtAfter(Instant instant);

    @Query("select r.code, count(distinct u.id) from UserEntity u join u.roles r group by r.code")
    List<Object[]> countUsersByRole();
    
    @Query("SELECT DISTINCT u FROM UserEntity u JOIN u.roles r WHERE r.code = 'ADMIN' AND u.status = 'ACTIVE'")
    List<UserEntity> findAllAdmins();
}
