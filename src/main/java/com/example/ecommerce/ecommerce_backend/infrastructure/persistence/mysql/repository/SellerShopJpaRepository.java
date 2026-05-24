package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerShopJpaRepository extends JpaRepository<SellerShopEntity, Long> {
    Optional<SellerShopEntity> findBySellerUserId(Long sellerUserId);
    Optional<SellerShopEntity> findByShopSlug(String shopSlug);
    List<SellerShopEntity> findByStatus(String status);
    long countByStatus(String status);
    List<SellerShopEntity> findByLogoUrlStartingWith(String prefix);
    List<SellerShopEntity> findByBannerUrlStartingWith(String prefix);
}
