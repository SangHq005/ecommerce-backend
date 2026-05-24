package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.review.ReviewResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.review.ReviewService;
import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Reviews", description = "Admin review moderation")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "List by status", description = "List reviews by status")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> listByStatus(@RequestParam ReviewStatus status) {
        List<ReviewResponse> reviews = reviewService.adminListByStatus(status).stream()
                .map(this::toResponse)
                .toList();
        return ResponseHelper.ok(reviews);
    }

    @GetMapping("/search")
    @Operation(summary = "Search reviews", description = "Search reviews with filters")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> search(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewResponse> reviews = reviewService.adminSearch(status, productId, userId, rating, pageable)
                .map(this::toResponse);
        return ResponseHelper.page(reviews);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve review", description = "Approve a pending review")
    public ResponseEntity<ApiResponse<ReviewResponse>> approve(@PathVariable("id") Long reviewId) {
        ReviewEntity review = reviewService.adminApprove(reviewId);
        return ResponseHelper.ok(toResponse(review), "Review approved");
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject review", description = "Reject a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> reject(@PathVariable("id") Long reviewId) {
        ReviewEntity review = reviewService.adminReject(reviewId);
        return ResponseHelper.ok(toResponse(review), "Review rejected");
    }

    private ReviewResponse toResponse(ReviewEntity review) {
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                "Unknown User", // Admin view can fetch properly if needed, for now placeholder
                null,
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getImages(),
                review.getStatus(),
                review.getHelpfulCount(),
                java.util.Collections.emptyList(),
                false, // isLiked (not tracked for admin view)
                false, // isAdmin (not tracked for admin view)
                review.getCreatedAt()
        );
    }
}
