package com.example.ecommerce.ecommerce_backend.application.service.notification;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Set<String> EMAIL_TYPES = Set.of(
        "ORDER_CONFIRMED", "ORDER_SHIPPED", "ORDER_DELIVERED", "ACCOUNT_VERIFIED"
    );

    private final EmailService emailService;
    private final UserJpaRepository userRepo;

    public EmailNotificationChannel(EmailService emailService, UserJpaRepository userRepo) {
        this.emailService = emailService;
        this.userRepo = userRepo;
    }

    @Override
    public boolean supports(String type) {
        return EMAIL_TYPES.contains(type);
    }

    @Override
    public void send(NotificationPayload payload) {
        userRepo.findById(payload.userId()).ifPresent(user ->
            emailService.sendSimpleEmail(user.getEmail(), payload.title(), payload.message())
        );
    }
}
