package com.example.ecommerce.ecommerce_backend.application.service.notification;

import com.example.ecommerce.ecommerce_backend.api.dto.notification.NotificationResponse;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.NotificationJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InAppNotificationChannelTest {

    @Mock NotificationJpaRepository notificationRepo;
    @Mock SimpMessagingTemplate messagingTemplate;
    @InjectMocks InAppNotificationChannel channel;

    @Test
    void send_savesNotificationAndPushesWebSocket() {
        var payload = new NotificationPayload(1L, "TEST", "Title", "Msg", "ORDER", 10L);
        var savedEntity = new NotificationEntity();
        savedEntity.setId(100L);
        savedEntity.setUserId(1L);
        savedEntity.setType("TEST");
        savedEntity.setTitle("Title");
        savedEntity.setMessage("Msg");

        when(notificationRepo.save(any(NotificationEntity.class))).thenReturn(savedEntity);

        channel.send(payload);

        var captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepo).save(captor.capture());
        var entity = captor.getValue();

        assertEquals(1L, entity.getUserId());
        assertEquals("TEST", entity.getType());
        assertEquals("Title", entity.getTitle());
        assertEquals("Msg", entity.getMessage());
        assertEquals("ORDER", entity.getReferenceType());
        assertEquals(10L, entity.getReferenceId());
        assertFalse(entity.getIsRead());

        verify(messagingTemplate).convertAndSend(
            eq("/topic/notifications/1"),
            any(NotificationResponse.class)
        );
    }
}
