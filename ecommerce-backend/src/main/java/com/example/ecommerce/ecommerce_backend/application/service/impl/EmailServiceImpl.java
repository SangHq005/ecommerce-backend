package com.example.ecommerce.ecommerce_backend.application.service.impl;

import com.example.ecommerce.ecommerce_backend.application.service.EmailService;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.EmailConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    @Override
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(UserEntity user) {
        log.info("Sending welcome email to: {}", user.getEmail());

        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", user.getFullName());
        variables.put("email", user.getEmail());

        sendEmail(
            user.getEmail(),
            "Welcome to E-Commerce Platform!",
            "welcome",
            variables
        );
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendOrderConfirmationEmail(UserEntity user, OrderEntity order) {
        log.info("Sending order confirmation email to: {} for order: {}", user.getEmail(), order.getOrderCode());

        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", user.getFullName());
        variables.put("orderCode", order.getOrderCode());
        variables.put("totalAmount", formatCurrency(order.getTotalAmount()));
        variables.put("currency", order.getCurrency());
        variables.put("status", order.getStatus());

        sendEmail(
            user.getEmail(),
            "Order Confirmation - " + order.getOrderCode(),
            "order-confirmation",
            variables
        );
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendOrderStatusUpdateEmail(UserEntity user, OrderEntity order, String oldStatus, String newStatus) {
        log.info("Sending order status update email to: {} for order: {}", user.getEmail(), order.getOrderCode());

        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", user.getFullName());
        variables.put("orderCode", order.getOrderCode());
        variables.put("oldStatus", oldStatus);
        variables.put("newStatus", newStatus);
        variables.put("totalAmount", formatCurrency(order.getTotalAmount()));
        variables.put("currency", order.getCurrency());

        sendEmail(
            user.getEmail(),
            "Order Status Update - " + order.getOrderCode(),
            "order-status-update",
            variables
        );
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(UserEntity user, String resetToken) {
        log.info("Sending password reset email to: {}", user.getEmail());

        // Generate reset link (frontend URL)
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", user.getFullName());
        variables.put("resetLink", resetLink);
        variables.put("expiryMinutes", 60); // 1 hour

        sendEmail(
            user.getEmail(),
            "Reset Your Password",
            "password-reset",
            variables
        );
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom(), emailConfig.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);

            // Process template
            Context context = new Context(Locale.getDefault());
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {} with template: {}", to, templateName);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} with template: {}", to, templateName, e);
            // Don't throw exception to prevent blocking main flow
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
        }
    }

    /**
     * Format currency amount (VND doesn't use decimals)
     */
    private String formatCurrency(Long amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,d", amount);
    }
}
