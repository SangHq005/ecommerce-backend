package com.example.ecommerce.ecommerce_backend.application.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final List<NotificationChannel> channels;

    public NotificationDispatcher(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public void dispatch(NotificationPayload payload) {
        channels.stream()
            .filter(c -> c.supports(payload.type()))
            .forEach(c -> {
                try {
                    c.send(payload);
                } catch (Exception e) {
                    log.warn("Notification channel {} failed for type {}: {}",
                        c.getClass().getSimpleName(), payload.type(), e.getMessage());
                }
            });
    }
}
