package com.example.ecommerce.ecommerce_backend.application.service.notification.impl;

import com.example.ecommerce.ecommerce_backend.application.service.notification.EmailService;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.EmailConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            EmailConfig emailConfig
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailConfig = emailConfig;
    }

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

            // Detailed terminal log for development
            System.out.println("\n-------------------------------------------------");
            System.out.println(">>> SENDING EMAIL TO: " + to);
            System.out.println(">>> SUBJECT: " + subject);
            System.out.println(">>> TEMPLATE: " + templateName);
            System.out.println(">>> VARIABLES: " + variables);
            System.out.println("-------------------------------------------------\n");

            mailSender.send(message);
            log.info("Email sent successfully to: {} with template: {}", to, templateName);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} with template: {}", to, templateName, e);
            // Don't throw exception to prevent blocking main flow
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(emailConfig.getFrom(), emailConfig.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false); // false = plain text

            System.out.println("\n-------------------------------------------------");
            System.out.println(">>> SENDING SIMPLE EMAIL TO: " + to);
            System.out.println(">>> SUBJECT: " + subject);
            System.out.println(">>> CONTENT: " + content);
            System.out.println("-------------------------------------------------\n");

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send simple email to: {}", to, e);
        } catch (Exception e) {
            log.error("Unexpected error sending simple email to: {}", to, e);
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
