# Payment Integration - VNPay

## Overview
This document describes the VNPay payment gateway integration for the e-commerce backend.

## Features Implemented

### 1. Payment Entity
- **File**: `PaymentEntity.java`
- **Table**: `payments`
- **Fields**:
  - `id`: Primary key
  - `orderId`: Foreign key to orders table
  - `amount`: Payment amount in smallest currency unit
  - `currency`: Currency code (default: VND)
  - `method`: Payment method (VNPAY, MOMO, STRIPE, etc.)
  - `status`: Payment status (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED)
  - `transactionId`: Unique transaction ID from payment gateway
  - `gateway`: Payment gateway name
  - `gatewayResponse`: Full JSON response from gateway
  - `createdAt`, `updatedAt`: Timestamps

### 2. Payment Flow

#### Step 1: Create Payment URL
```http
POST /api/v1/payment/vnpay/create
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "orderCode": "ORD-1234567890"
}
```

**Response**:
```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
  "orderCode": "ORD-1234567890",
  "amount": 500000,
  "currency": "VND"
}
```

#### Step 2: User Completes Payment
- Frontend redirects user to `paymentUrl`
- User completes payment on VNPay portal
- VNPay redirects back to `VNPAY_RETURN_URL`

#### Step 3: Handle Callback
```http
GET /api/v1/payment/vnpay/callback?vnp_Amount=...&vnp_ResponseCode=00&...
```

**Success Response**:
- Redirects to: `http://localhost:3000/payment/success?orderCode=ORD-1234567890`
- Payment status: COMPLETED
- Order status: PAID
- Stock reservation: COMMITTED

**Failure Response**:
- Redirects to: `http://localhost:3000/payment/failed?orderCode=ORD-1234567890&code=24`
- Payment status: FAILED
- Order status: CANCELLED
- Stock reservation: RELEASED

### 3. Get Payment Information
```http
GET /api/v1/payment/{orderCode}
Authorization: Bearer {access_token}
```

**Response**:
```json
{
  "id": 1,
  "orderCode": "ORD-1234567890",
  "amount": 500000,
  "currency": "VND",
  "method": "VNPAY",
  "status": "COMPLETED",
  "transactionId": "14012345",
  "createdAt": "2026-01-03T23:00:00"
}
```

## VNPay Configuration

### Environment Variables

Add to `.env` file:

```bash
# VNPay Sandbox Credentials
VNPAY_TMN_CODE=your_tmn_code_here
VNPAY_HASH_SECRET=your_hash_secret_here
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:8080/api/v1/payment/vnpay/callback
VNPAY_VERSION=2.1.0
VNPAY_COMMAND=pay
```

### Get VNPay Sandbox Credentials

1. Visit: https://sandbox.vnpayment.vn/merchantv2/
2. Register for sandbox account
3. Get `TMN_CODE` and `HASH_SECRET`
4. Update `.env` file

### VNPay Test Cards

For sandbox testing, use these test cards:

| Bank | Card Number | Cardholder | Issue Date | OTP |
|------|-------------|------------|------------|-----|
| NCB | 9704198526191432198 | NGUYEN VAN A | 07/15 | 123456 |
| VietcomBank | 9704061647543251018 | NGUYEN VAN B | 07/15 | 123456 |
| BIDV | 9704060000000018 | NGUYEN VAN C | 07/15 | 123456 |

## Security Features

### 1. Signature Validation
- All VNPay callbacks are validated using HMAC SHA512
- Invalid signatures are rejected

### 2. Idempotency
- Duplicate payment processing is prevented
- Payment status is checked before processing

### 3. Order Ownership
- Users can only create payments for their own orders
- Users can only view their own payment information

### 4. Order Status Validation
- Payments can only be created for orders in `PAYMENT_PENDING` status
- Order status is automatically updated after payment

## Database Migration

Migration file: `V0070__create_payments_table.sql`

Run migration:
```bash
mvn flyway:migrate
```

Or start the application (Flyway runs automatically):
```bash
mvn spring-boot:run
```

## Testing

### 1. Unit Tests
```bash
mvn test -Dtest=VNPayServiceTest
mvn test -Dtest=PaymentServiceTest
```

### 2. Integration Tests
```bash
mvn test -Dtest=PaymentControllerTest
```

### 3. Manual Testing with Swagger

1. Start application:
```bash
mvn spring-boot:run
```

2. Open Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

3. Test payment flow:
   - Login to get JWT token
   - Create an order via checkout
   - Create payment URL
   - Visit payment URL in browser
   - Complete payment on VNPay sandbox
   - Verify order status updated to PAID

## Error Handling

### Common Error Codes

| Code | Description | Action |
|------|-------------|--------|
| 00 | Success | Payment completed |
| 07 | Suspicious transaction | Contact VNPay |
| 09 | Card not registered for internet banking | Use another card |
| 10 | Incorrect authentication | Re-enter OTP |
| 11 | Payment timeout | Create new payment |
| 12 | Account locked | Contact bank |
| 13 | Incorrect OTP | Re-enter correct OTP |
| 24 | User cancelled | Create new payment |
| 51 | Insufficient balance | Top up account |
| 65 | Transaction limit exceeded | Contact bank |
| 75 | Exceed retry limit | Wait and try again |
| 79 | Incorrect payment amount | Contact support |

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/payment/vnpay/create` | Required | Create VNPay payment URL |
| GET | `/api/v1/payment/vnpay/callback` | Public | VNPay callback handler |
| GET | `/api/v1/payment/{orderCode}` | Required | Get payment by order code |

## Future Enhancements

### Planned Features
- [ ] MoMo payment gateway integration
- [ ] Stripe payment integration
- [ ] PayPal payment integration
- [ ] Payment refund processing
- [ ] Partial refunds
- [ ] Payment webhooks for async notifications
- [ ] Payment analytics dashboard
- [ ] Multiple payment methods per order
- [ ] Installment payment support
- [ ] Saved payment methods
- [ ] Payment retry mechanism
- [ ] Payment notifications via email/SMS

## Troubleshooting

### Issue: Invalid Signature
**Solution**: Verify `VNPAY_HASH_SECRET` is correct and matches VNPay portal.

### Issue: Callback Not Working
**Solution**:
- Check `VNPAY_RETURN_URL` is publicly accessible
- For local development, use ngrok: `ngrok http 8080`
- Update `VNPAY_RETURN_URL` to ngrok URL

### Issue: Payment Status Not Updated
**Solution**: Check logs for errors in `PaymentService.processVNPayCallback()`

### Issue: Stock Not Released After Failed Payment
**Solution**: Verify `ReservationService.release()` is called in catch block

## Contact & Support

For VNPay integration issues:
- **VNPay Support**: support@vnpay.vn
- **Sandbox Issues**: https://sandbox.vnpayment.vn/merchantv2/

For application issues:
- Create GitHub issue with logs and error details
