package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.review.ReviewResponse;
import com.example.ecommerce.ecommerce_backend.application.service.ReviewService;
import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReviewResponse> listByStatus(@RequestParam ReviewStatus status) {
        return reviewService.adminListByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ReviewResponse approve(@PathVariable("id") Long reviewId) {
        ReviewEntity review = reviewService.adminApprove(reviewId);
        return toResponse(review);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ReviewResponse reject(@PathVariable("id") Long reviewId) {
        ReviewEntity review = reviewService.adminReject(reviewId);
        return toResponse(review);
    }

    private ReviewResponse toResponse(ReviewEntity review) {
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getImages(),
                review.getStatus(),
                review.getCreatedAt()
        );
    }
}
