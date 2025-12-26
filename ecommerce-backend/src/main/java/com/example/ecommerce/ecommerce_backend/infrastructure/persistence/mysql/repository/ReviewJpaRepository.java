package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByProductId(Long productId);
    List<ReviewEntity> findByUserId(Long userId);
    List<ReviewEntity> findByStatus(ReviewStatus status);
    Optional<ReviewEntity> findByProductIdAndUserId(Long productId, Long userId);
    List<ReviewEntity> findByProductIdAndStatus(Long productId, ReviewStatus status);
    List<ReviewEntity> findByProductIdAndStatusAndRating(Long productId, ReviewStatus status, Integer rating);
    long countByProductIdAndStatus(Long productId, ReviewStatus status);

    @Query("select avg(r.rating) from ReviewEntity r where r.productId = :productId and r.status = com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus.APPROVED")
    Double calculateAverageRating(@Param("productId") Long productId);
}
