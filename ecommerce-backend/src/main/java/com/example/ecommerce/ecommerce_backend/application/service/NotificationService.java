package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.notification.NotificationResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationJpaRepository notificationRepo;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create notification for user
     */
    @Transactional
    public NotificationEntity createNotification(
            Long userId,
            String type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {
        log.info("Creating notification for user {} of type {}", userId, type);

        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());

        NotificationEntity saved = notificationRepo.save(notification);
        log.info("Notification created: {}", saved.getId());

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                NotificationResponse.from(saved)
        );

        return saved;
    }

    /**
     * Get user notifications (paginated)
     */
    @Transactional(readOnly = true)
    public Page<NotificationEntity> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepo.findByUserId(userId, pageable);
    }

    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationEntity> getUnreadNotifications(Long userId) {
        return notificationRepo.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    /**
     * Get unread count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndIsRead(userId, false);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        NotificationEntity notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw ApiException.forbidden("Not authorized to mark this notification as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());
        notificationRepo.save(notification);

        log.info("Notification {} marked as read", notificationId);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepo.markAllAsRead(userId);
        log.info("All notifications marked as read for user {}", userId);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        NotificationEntity notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw ApiException.forbidden("Not authorized to delete this notification");
        }

        notificationRepo.delete(notification);
        log.info("Notification {} deleted", notificationId);
    }

    /**
     * Create order notification
     */
    public void notifyOrderCreated(Long userId, Long orderId, String orderCode) {
        createNotification(
                userId,
                "ORDER_CREATED",
                "Đơn hàng mới",
                "Đơn hàng " + orderCode + " đã được tạo thành công",
                "ORDER",
                orderId
        );
    }

    /**
     * Create order status change notification
     */
    public void notifyOrderStatusChanged(Long userId, Long orderId, String orderCode, String newStatus) {
        String message = switch (newStatus) {
            case "CONFIRMED" -> "Đơn hàng " + orderCode + " đã được xác nhận";
            case "PROCESSING" -> "Đơn hàng " + orderCode + " đang được xử lý";
            case "SHIPPED" -> "Đơn hàng " + orderCode + " đã được giao cho đơn vị vận chuyển";
            case "DELIVERED" -> "Đơn hàng " + orderCode + " đã được giao thành công";
            case "CANCELLED" -> "Đơn hàng " + orderCode + " đã bị hủy";
            default -> "Đơn hàng " + orderCode + " đã cập nhật trạng thái: " + newStatus;
        };

        createNotification(
                userId,
                "ORDER_STATUS_CHANGED",
                "Cập nhật đơn hàng",
                message,
                "ORDER",
                orderId
        );
    }

    /**
     * Create payment success notification
     */
    public void notifyPaymentSuccess(Long userId, Long orderId, String orderCode) {
        createNotification(
                userId,
                "PAYMENT_SUCCESS",
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng " + orderCode + " đã thành công",
                "ORDER",
                orderId
        );
    }

    /**
     * Create new coupon notification
     */
    public void notifyNewCoupon(Long userId, String couponCode, String couponName) {
        createNotification(
                userId,
                "COUPON_NEW",
                "Mã giảm giá mới",
                "Bạn có mã giảm giá mới: " + couponName + " (" + couponCode + ")",
                "COUPON",
                null
        );
    }

    /**
     * Create review approved notification
     */
    public void notifyReviewApproved(Long userId, Long reviewId, Long productId) {
        createNotification(
                userId,
                "REVIEW_APPROVED",
                "Đánh giá được phê duyệt",
                "Đánh giá của bạn đã được phê duyệt và hiển thị công khai",
                "REVIEW",
                reviewId
        );
    }
}
