package com.example.ecommerce.ecommerce_backend.application.service.notification;

import com.example.ecommerce.ecommerce_backend.api.dto.notification.NotificationResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.NotificationJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationJpaRepository notificationRepo;
    private final UserJpaRepository userRepo;
    private final NotificationDispatcher dispatcher;

    public NotificationService(
            NotificationJpaRepository notificationRepo,
            UserJpaRepository userRepo,
            NotificationDispatcher dispatcher
    ) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
        this.dispatcher = dispatcher;
    }

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

        NotificationPayload payload = new NotificationPayload(userId, type, title, message, referenceType, referenceId);
        dispatcher.dispatch(payload);

        NotificationEntity mockEntity = new NotificationEntity();
        mockEntity.setUserId(userId);
        mockEntity.setType(type);
        mockEntity.setTitle(title);
        mockEntity.setMessage(message);
        mockEntity.setReferenceType(referenceType);
        mockEntity.setReferenceId(referenceId);
        return mockEntity;
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

    /**
     * Notify all admins about new seller profile submission
     */
    @Transactional
    public void notifyAdminsNewSellerProfile(Long profileId, String sellerName, String sellerType) {
        try {
            List<UserEntity> admins = userRepo.findAllAdmins();
            log.info("Notifying {} admins about new seller profile {}", admins.size(), profileId);
            
            String message = String.format("Hồ sơ người bán mới từ %s (%s) đang chờ xác thực", 
                    sellerName, sellerType.equals("INDIVIDUAL") ? "Cá nhân" : "Doanh nghiệp");
            
            for (UserEntity admin : admins) {
                try {
                    createNotification(
                            admin.getId(),
                            "SELLER_PROFILE_PENDING",
                            "Hồ sơ người bán mới",
                            message,
                            "SELLER_PROFILE",
                            profileId
                    );
                } catch (Exception e) {
                    log.warn("Failed to notify admin {}: {}", admin.getId(), e.getMessage());
                }
            }
            
            log.info("Successfully notified admins about seller profile {}", profileId);
        } catch (Exception e) {
            log.error("Failed to notify admins about seller profile {}: {}", profileId, e.getMessage(), e);
        }
    }

    /**
     * Notify all admins about new shop submission
     */
    @Transactional
    public void notifyAdminsNewShop(Long shopId, String shopName, Long sellerUserId) {
        try {
            List<UserEntity> admins = userRepo.findAllAdmins();
            log.info("Notifying {} admins about new shop submission {}", admins.size(), shopId);
            
            String message = String.format("Shop mới '%s' đang chờ duyệt", shopName);
            
            for (UserEntity admin : admins) {
                try {
                    createNotification(
                            admin.getId(),
                            "SHOP_PENDING",
                            "Shop mới chờ duyệt",
                            message,
                            "SHOP",
                            shopId
                    );
                } catch (Exception e) {
                    log.warn("Failed to notify admin {}: {}", admin.getId(), e.getMessage());
                }
            }
            
            log.info("Successfully notified admins about shop {}", shopId);
        } catch (Exception e) {
            log.error("Failed to notify admins about shop {}: {}", shopId, e.getMessage(), e);
        }
    }

    /**
     * Notify all admins about new product submission
     */
    @Transactional
    public void notifyAdminsNewProduct(Long productId, String productName, Long shopId) {
        try {
            List<UserEntity> admins = userRepo.findAllAdmins();
            
            if (admins.isEmpty()) {
                log.warn("No active admins found in database! Product {} will not be notified to any admin.", productId);
                log.warn("Please ensure at least one user has ADMIN role and ACTIVE status.");
                return;
            }
            
            log.info("Notifying {} admins about new product submission: productId={}, name={}, shopId={}", 
                    admins.size(), productId, productName, shopId);
            
            String message = String.format("Sản phẩm mới '%s' đang chờ duyệt", productName);
            int successCount = 0;
            
            for (UserEntity admin : admins) {
                try {
                    createNotification(
                            admin.getId(),
                            "PRODUCT_PENDING",
                            "Sản phẩm mới chờ duyệt",
                            message,
                            "PRODUCT",
                            productId
                    );
                    successCount++;
                    log.debug("Notification sent to admin {} for product {}", admin.getId(), productId);
                } catch (Exception e) {
                    log.warn("Failed to notify admin {}: {}", admin.getId(), e.getMessage(), e);
                }
            }
            
            if (successCount > 0) {
                log.info("Successfully notified {}/{} admins about product {}", successCount, admins.size(), productId);
            } else {
                log.error("Failed to notify any admin about product {} ({} admins found but all notifications failed)", 
                        productId, admins.size());
            }
        } catch (Exception e) {
            log.error("Failed to notify admins about product {}: {}", productId, e.getMessage(), e);
        }
    }
}
