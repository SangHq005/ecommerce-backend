package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.review.CreateReviewRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.review.ReviewResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.review.UpdateReviewRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.ReviewService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID in token");
        }
    }

    @PostMapping("/products/{id}/reviews")
    public ReviewResponse createReview(@PathVariable("id") Long productId,
                                       @Valid @RequestBody CreateReviewRequest request) {
        ReviewEntity review = reviewService.createReview(
                currentUserId(),
                productId,
                request.rating(),
                request.comment(),
                request.images()
        );
        return toResponse(review);
    }

    @GetMapping("/products/{id}/reviews")
    public List<ReviewResponse> listReviews(@PathVariable("id") Long productId,
                                            @RequestParam(required = false) Integer rating) {
        return reviewService.listReviews(productId, rating).stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse updateReview(@PathVariable("id") Long reviewId,
                                       @Valid @RequestBody UpdateReviewRequest request) {
        ReviewEntity review = reviewService.updateReview(
                currentUserId(),
                reviewId,
                request.rating(),
                request.comment(),
                request.images()
        );
        return toResponse(review);
    }

    @DeleteMapping("/reviews/{id}")
    public void deleteReview(@PathVariable("id") Long reviewId) {
        reviewService.deleteReview(currentUserId(), reviewId);
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
