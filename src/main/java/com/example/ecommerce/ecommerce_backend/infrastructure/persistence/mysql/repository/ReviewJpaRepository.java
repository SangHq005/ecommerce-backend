package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long>, JpaSpecificationExecutor<ReviewEntity> {
    List<ReviewEntity> findByProductId(Long productId);
    List<ReviewEntity> findByUserId(Long userId);
    List<ReviewEntity> findByStatus(ReviewStatus status);
    Optional<ReviewEntity> findByProductIdAndUserId(Long productId, Long userId);
    List<ReviewEntity> findByProductIdAndStatus(Long productId, ReviewStatus status);
    List<ReviewEntity> findByProductIdAndStatusAndRating(Long productId, ReviewStatus status, Integer rating);
    long countByProductIdAndStatusAndParentIdIsNull(Long productId, ReviewStatus status);

    @Query("select avg(r.rating) from ReviewEntity r where r.productId = :productId and r.status = com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus.APPROVED and r.parentId is null")
    Double calculateAverageRating(@Param("productId") Long productId);

    @Query("select r.rating, count(r) from ReviewEntity r where r.productId = :productId and r.status = com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus.APPROVED and r.parentId is null and r.rating is not null group by r.rating")
    List<Object[]> countByRating(@Param("productId") Long productId);
}
