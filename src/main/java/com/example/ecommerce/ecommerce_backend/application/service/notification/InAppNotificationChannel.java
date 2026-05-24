package com.example.ecommerce.ecommerce_backend.application.service.notification;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.NotificationJpaRepository;
import com.example.ecommerce.ecommerce_backend.api.dto.notification.NotificationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class InAppNotificationChannel implements NotificationChannel {

    private final NotificationJpaRepository notificationRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public InAppNotificationChannel(
            NotificationJpaRepository notificationRepo,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepo = notificationRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public boolean supports(String type) {
        return true; // In-app always receives all notification types
    }

    @Override
    public void send(NotificationPayload payload) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(payload.userId());
        entity.setType(payload.type());
        entity.setTitle(payload.title());
        entity.setMessage(payload.message());
        entity.setReferenceType(payload.referenceType());
        entity.setReferenceId(payload.referenceId());
        entity.setIsRead(false);
        entity.setCreatedAt(Instant.now());
        
        NotificationEntity saved = notificationRepo.save(entity);

        messagingTemplate.convertAndSend(
            "/topic/notifications/" + payload.userId(),
            NotificationResponse.from(saved)
        );
    }
}
