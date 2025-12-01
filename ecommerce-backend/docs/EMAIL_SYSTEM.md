# Email Notification System

## Overview
This document describes the email notification system implementation for the e-commerce backend.

## Features Implemented

### 1. Email Templates
All email templates are located in `src/main/resources/templates/email/`

#### Welcome Email (`welcome.html`)
- **Triggered**: When user registers
- **Template Variables**:
  - `fullName`: User's full name
  - `email`: User's email address
- **Content**: Welcome message, platform features overview, call-to-action button

#### Order Confirmation Email (`order-confirmation.html`)
- **Triggered**: When payment is successful (order status changes to PAID)
- **Template Variables**:
  - `fullName`: User's full name
  - `orderCode`: Order code
  - `totalAmount`: Formatted total amount
  - `currency`: Currency code (VND)
  - `status`: Order status
- **Content**: Order details, payment confirmation, tracking link

#### Order Status Update Email (`order-status-update.html`)
- **Triggered**: When order status changes
- **Template Variables**:
  - `fullName`: User's full name
  - `orderCode`: Order code
  - `oldStatus`: Previous order status
  - `newStatus`: New order status
  - `totalAmount`: Formatted total amount
  - `currency`: Currency code
- **Content**: Status change notification, contextual message based on new status

#### Password Reset Email (`password-reset.html`)
- **Triggered**: When user requests password reset
- **Template Variables**:
  - `fullName`: User's full name
  - `resetLink`: Password reset URL (frontend)
  - `expiryMinutes`: Token expiry time (60 minutes)
- **Content**: Reset password link, security warnings, expiry notice

### 2. Email Service Architecture

#### EmailConfig
- **File**: `EmailConfig.java`
- **Purpose**: Configuration for email sender and async executor
- **Thread Pool**:
  - Core pool size: 2
  - Max pool size: 5
  - Queue capacity: 100
  - Thread prefix: "email-"

#### EmailService Interface
- **File**: `EmailService.java`
- **Methods**:
  - `sendWelcomeEmail(UserEntity user)`
  - `sendOrderConfirmationEmail(UserEntity user, OrderEntity order)`
  - `sendOrderStatusUpdateEmail(UserEntity user, OrderEntity order, String oldStatus, String newStatus)`
  - `sendPasswordResetEmail(UserEntity user, String resetToken)`
  - `sendEmail(String to, String subject, String templateName, Map<String, Object> variables)`

#### EmailServiceImpl
- **File**: `EmailServiceImpl.java`
- **Features**:
  - Async email sending with `@Async("emailTaskExecutor")`
  - Thymeleaf template processing
  - Error handling (non-blocking)
  - HTML email support
  - Currency formatting for VND

### 3. Integration Points

#### AuthService Integration
```java
// After user registration
emailService.sendWelcomeEmail(user);
```

#### PaymentService Integration
```java
// After successful payment
userRepository.findById(order.getUserId()).ifPresent(user -> {
    emailService.sendOrderConfirmationEmail(user, order);
});
```

## Configuration

### SMTP Configuration

Add to `.env` file:

```bash
# Gmail Configuration (requires App Password)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password-here

# Email Sender Info
EMAIL_FROM=noreply@ecommerce.com
EMAIL_FROM_NAME=E-Commerce Platform
```

### Application YAML Configuration

```yaml
spring:
  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

app:
  email:
    from: ${EMAIL_FROM:noreply@ecommerce.com}
    from-name: ${EMAIL_FROM_NAME:E-Commerce Platform}
```

## SMTP Provider Setup

### Gmail (Recommended for Testing)

1. **Enable 2-Factor Authentication**
   - Go to: https://myaccount.google.com/security
   - Enable 2FA

2. **Generate App Password**
   - Go to: https://myaccount.google.com/apppasswords
   - Select "Mail" and "Other (Custom name)"
   - Copy the 16-character password
   - Use this as `SMTP_PASSWORD` (not your Gmail password!)

3. **Configuration**
   ```bash
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your-email@gmail.com
   SMTP_PASSWORD=xxxx-xxxx-xxxx-xxxx  # App Password
   ```

### Mailtrap (Best for Development/Testing)

1. **Sign up**: https://mailtrap.io/
2. **Get credentials** from your inbox
3. **Configuration**
   ```bash
   SMTP_HOST=smtp.mailtrap.io
   SMTP_PORT=2525
   SMTP_USERNAME=your-mailtrap-username
   SMTP_PASSWORD=your-mailtrap-password
   ```

**Benefits**:
- All emails are caught (never sent to real users)
- Email preview in web interface
- Test spam scores
- HTML/Plain text view

### SendGrid (Production)

1. **Sign up**: https://sendgrid.com/
2. **Create API Key**
3. **Configuration**
   ```bash
   SMTP_HOST=smtp.sendgrid.net
   SMTP_PORT=587
   SMTP_USERNAME=apikey
   SMTP_PASSWORD=your-sendgrid-api-key
   ```

### AWS SES (Production)

1. **Setup in AWS Console**
2. **Verify domain/email**
3. **Generate SMTP credentials**
4. **Configuration**
   ```bash
   SMTP_HOST=email-smtp.us-east-1.amazonaws.com
   SMTP_PORT=587
   SMTP_USERNAME=your-ses-username
   SMTP_PASSWORD=your-ses-password
   ```

## Testing

### Manual Test - Welcome Email

```bash
# 1. Configure SMTP in .env
# 2. Start application
mvn spring-boot:run

# 3. Register new user
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User"
}

# 4. Check email inbox (or Mailtrap)
```

### Manual Test - Order Confirmation

```bash
# 1. Login and create order
# 2. Create payment and complete on VNPay
# 3. Check email inbox for order confirmation
```

### Verify Email Sending

Check application logs:

```
INFO  EmailServiceImpl - Sending welcome email to: test@example.com
INFO  EmailServiceImpl - Email sent successfully to: test@example.com with template: welcome
```

## Email Templates Customization

### Modify Template Content

Edit files in `src/main/resources/templates/email/`:

```html
<!-- Example: Add custom footer -->
<div class="footer">
    <p>© 2026 E-Commerce Platform. All rights reserved.</p>
    <p>Contact us: support@ecommerce.com</p>
    <p>Follow us on social media!</p>
</div>
```

### Add New Template

1. Create new HTML file: `templates/email/my-template.html`
2. Add Thymeleaf variables: `<span th:text="${variableName}">Default</span>`
3. Call from service:
   ```java
   Map<String, Object> vars = new HashMap<>();
   vars.put("variableName", "value");
   emailService.sendEmail(to, subject, "my-template", vars);
   ```

### Template Variables Reference

| Template | Variable | Type | Description |
|----------|----------|------|-------------|
| welcome | fullName | String | User's full name |
| welcome | email | String | User's email |
| order-confirmation | fullName | String | User's full name |
| order-confirmation | orderCode | String | Order code |
| order-confirmation | totalAmount | String | Formatted amount |
| order-confirmation | currency | String | Currency code |
| order-confirmation | status | String | Order status |
| order-status-update | fullName | String | User's full name |
| order-status-update | orderCode | String | Order code |
| order-status-update | oldStatus | String | Previous status |
| order-status-update | newStatus | String | New status |
| order-status-update | totalAmount | String | Formatted amount |
| order-status-update | currency | String | Currency code |
| password-reset | fullName | String | User's full name |
| password-reset | resetLink | String | Reset password URL |
| password-reset | expiryMinutes | Integer | Token expiry (60) |

## Async Email Sending

All email sending is asynchronous using Spring's `@Async`:

**Benefits**:
- Non-blocking - doesn't slow down user requests
- Separate thread pool for email operations
- Automatic retry on failure (configurable)
- No impact on transaction performance

**Configuration**:
```java
@Bean(name = "emailTaskExecutor")
public Executor emailTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("email-");
    executor.initialize();
    return executor;
}
```

## Error Handling

Email failures are logged but don't block main application flow:

```java
try {
    mailSender.send(message);
    log.info("Email sent successfully");
} catch (MessagingException e) {
    log.error("Failed to send email", e);
    // Don't throw - prevent blocking main flow
}
```

**Monitoring**: Check logs for email failures
```bash
grep "Failed to send email" logs/application.log
```

## Troubleshooting

### Issue: Email Not Sending

**Solution**:
- Check SMTP credentials in `.env`
- Verify SMTP host/port are correct
- Check application logs for errors
- Test SMTP connection: `telnet smtp.gmail.com 587`

### Issue: Gmail "Less Secure App" Error

**Solution**: Use App Password instead of regular password

### Issue: Emails Going to Spam

**Solutions**:
- Add SPF/DKIM records to your domain
- Use verified email sender address
- Avoid spam trigger words in subject/content
- Use reputable SMTP provider (SendGrid, AWS SES)

### Issue: Timeout Errors

**Solution**: Increase timeout in `application.yaml`:
```yaml
spring:
  mail:
    properties:
      mail:
        smtp:
          connectiontimeout: 10000
          timeout: 10000
          writetimeout: 10000
```

### Issue: Template Not Found

**Error**: `TemplateInputException: Error resolving template`

**Solution**:
- Verify template file exists in `src/main/resources/templates/email/`
- Check template name matches (case-sensitive)
- Rebuild project: `mvn clean compile`

## Production Recommendations

1. **Use Professional SMTP Provider**
   - SendGrid, AWS SES, Mailgun
   - Better deliverability
   - Email analytics
   - Bounce/complaint handling

2. **Implement Email Queue**
   - Use RabbitMQ or Kafka for email queue
   - Retry failed emails
   - Rate limiting

3. **Monitor Email Metrics**
   - Track delivery rates
   - Monitor bounce rates
   - Log all email attempts

4. **Add Unsubscribe Link**
   - Include in all marketing emails
   - Store user preferences

5. **GDPR Compliance**
   - Get consent before sending marketing emails
   - Provide data export/deletion

## Future Enhancements

- [ ] Email queue with retry mechanism
- [ ] Email templates management UI
- [ ] A/B testing for email content
- [ ] Email analytics (open rate, click rate)
- [ ] Scheduled emails (birthday, promotions)
- [ ] Email preferences per user
- [ ] Localization (multi-language emails)
- [ ] Rich email content (product images, dynamic content)
- [ ] Email verification flow
- [ ] SMS notifications integration
