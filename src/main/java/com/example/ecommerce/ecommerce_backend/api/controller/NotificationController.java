package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.MessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.notification.NotificationResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.NotificationService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID");
        }
    }

    /**
     * Get all notifications (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Long userId = currentUserId();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<NotificationEntity> notifications = notificationService.getUserNotifications(userId, pageable);
        Page<NotificationResponse> responses = notifications.map(NotificationResponse::from);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        Long userId = currentUserId();
        List<NotificationEntity> notifications = notificationService.getUnreadNotifications(userId);
        List<NotificationResponse> responses = notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = currentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long notificationId) {
        Long userId = currentUserId();
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        Long userId = currentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long notificationId) {
        Long userId = currentUserId();
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok(new MessageResponse("Notification deleted"));
    }
}
