package com.example.ecommerce.ecommerce_backend.application.service.notification;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;

import java.util.Map;

public interface EmailService {

    /**
     * Send welcome email to new user
     */
    void sendWelcomeEmail(UserEntity user);

    /**
     * Send order confirmation email
     */
    void sendOrderConfirmationEmail(UserEntity user, OrderEntity order);

    /**
     * Send order status update email
     */
    void sendOrderStatusUpdateEmail(UserEntity user, OrderEntity order, String oldStatus, String newStatus);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(UserEntity user, String resetToken);

    /**
     * Send generic email with custom template
     */
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);

    /**
     * Send simple email with plain text
     */
    void sendSimpleEmail(String to, String subject, String content);
}
