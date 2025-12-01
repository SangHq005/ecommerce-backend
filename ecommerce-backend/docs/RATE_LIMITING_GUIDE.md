## Rate Limiting Guide

### How to Use

Apply `@RateLimit` annotation to controller methods:

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    // Allow max 5 login attempts per 15 minutes
    @RateLimit(limit = 5, window = 15, unit = ChronoUnit.MINUTES)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Implementation
    }

    // Allow max 3 registrations per hour
    @RateLimit(limit = 3, window = 1, unit = ChronoUnit.HOURS)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Implementation
    }

    // Allow max 3 password reset requests per hour
    @RateLimit(limit = 3, window = 1, unit = ChronoUnit.HOURS, keyPrefix = "pwd-reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Implementation
    }
}
```

### Response when rate limit exceeded:

```json
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Limit: 5 requests per 15 minutes",
  "retryAfter": 900
}
```

### Recommended Limits:

- **Login**: 5 per 15 minutes
- **Registration**: 3 per hour
- **Password Reset**: 3 per hour
- **Checkout**: 10 per minute
- **Review Submit**: 5 per hour
- **General API**: 100 per minute
