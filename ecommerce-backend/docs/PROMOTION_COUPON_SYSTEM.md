# Promotion & Coupon System

## Overview

The Promotion & Coupon System enables flexible discount management with support for:
- **Multiple discount types**: Percentage, fixed amount, free shipping
- **Advanced restrictions**: Products, categories, users, order minimums
- **Usage controls**: Total limits, per-user limits, date ranges
- **Auto-apply coupons**: Automatic discounts when conditions are met
- **Usage tracking**: Complete audit trail of coupon usage

## Architecture

### Components

1. **CouponEntity** - Main coupon configuration with all rules
2. **CouponUsageEntity** - Tracks every coupon application
3. **CouponService** - Business logic for validation and application
4. **CouponController** - Public API for users
5. **AdminCouponController** - Admin API for coupon management

### Coupon Types

| Type | Description | Example |
|------|-------------|---------|
| **PERCENTAGE** | Percentage discount with optional cap | 10% off (max 100,000 VND) |
| **FIXED_AMOUNT** | Fixed amount discount | 50,000 VND off |
| **FREE_SHIPPING** | Free shipping discount | Free delivery |

### Coupon Status

| Status | Description |
|--------|-------------|
| **ACTIVE** | Coupon is active and can be used |
| **INACTIVE** | Temporarily disabled by admin |
| **EXPIRED** | Past end date |
| **DEPLETED** | Usage limit reached |

## Database Schema

### coupon Table

```sql
CREATE TABLE coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,        -- PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    status VARCHAR(20) NOT NULL,      -- ACTIVE, INACTIVE, EXPIRED, DEPLETED

    -- Discount configuration
    discount_value BIGINT NOT NULL,
    max_discount_amount BIGINT,       -- For PERCENTAGE type
    min_order_amount BIGINT,

    -- Validity period
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,

    -- Usage limits
    usage_limit INT,
    usage_count INT DEFAULT 0,
    usage_limit_per_user INT,

    -- Auto-apply
    auto_apply BOOLEAN DEFAULT FALSE,

    -- Restrictions (JSON arrays)
    applicable_product_ids JSON,
    applicable_category_ids JSON,
    applicable_user_ids JSON,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

### coupon_usage Table

```sql
CREATE TABLE coupon_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount BIGINT NOT NULL,
    used_at DATETIME NOT NULL,

    FOREIGN KEY (coupon_id) REFERENCES coupon(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

## API Endpoints

### User Endpoints

#### 1. Validate Coupon

```http
POST /api/v1/coupons/validate
Authorization: Bearer {token}
Content-Type: application/json

{
  "couponCode": "SUMMER2025",
  "orderTotal": 500000,
  "productIds": [1, 2, 3],
  "categoryIds": [10, 20]
}
```

**Response** (200 OK):
```json
{
  "valid": true,
  "message": "Coupon applied successfully",
  "discountAmount": 50000,
  "couponCode": "SUMMER2025",
  "couponName": "Summer Sale 2025"
}
```

**Error Response**:
```json
{
  "valid": false,
  "message": "Minimum order amount of 300000 VND required",
  "discountAmount": null,
  "couponCode": null,
  "couponName": null
}
```

---

#### 2. Get Active Coupons

```http
GET /api/v1/coupons/active
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "code": "SUMMER2025",
    "name": "Summer Sale 2025",
    "description": "Get 10% off on all summer items",
    "type": "PERCENTAGE",
    "status": "ACTIVE",
    "discountValue": 10,
    "maxDiscountAmount": 100000,
    "minOrderAmount": 300000,
    "startDate": "2025-06-01T00:00:00Z",
    "endDate": "2025-08-31T23:59:59Z",
    "usageLimit": 1000,
    "usageCount": 245,
    "usageLimitPerUser": 1,
    "autoApply": false,
    "applicableProductIds": null,
    "applicableCategoryIds": [5, 10],
    "createdAt": "2025-05-15T10:00:00Z"
  }
]
```

---

#### 3. Get Coupon Details

```http
GET /api/v1/coupons/{code}
Authorization: Bearer {token}
```

**Response**: Same as individual coupon object above

---

### Admin Endpoints

#### 1. Create Coupon

```http
POST /api/v1/admin/coupons
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "code": "WELCOME10",
  "name": "Welcome Discount",
  "description": "10% off for new customers",
  "type": "PERCENTAGE",
  "discountValue": 10,
  "maxDiscountAmount": 50000,
  "minOrderAmount": 200000,
  "startDate": "2025-01-01T00:00:00Z",
  "endDate": "2025-12-31T23:59:59Z",
  "usageLimit": 500,
  "usageLimitPerUser": 1,
  "autoApply": false,
  "applicableProductIds": null,
  "applicableCategoryIds": null,
  "applicableUserIds": null
}
```

**Response** (200 OK): CouponResponse object

---

#### 2. Update Coupon

```http
PUT /api/v1/admin/coupons/{couponId}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body**: Same as create coupon

**Response** (200 OK): Updated CouponResponse object

---

#### 3. Delete Coupon

```http
DELETE /api/v1/admin/coupons/{couponId}
Authorization: Bearer {admin_token}
```

**Response** (200 OK):
```json
{
  "message": "Coupon deleted successfully"
}
```

**Note**: Cannot delete coupons that have been used. Set status to INACTIVE instead.

---

## Validation Rules

The system validates coupons against multiple criteria:

### 1. Basic Validation
- ✅ Coupon code exists
- ✅ Status is ACTIVE
- ✅ Current date is within start_date and end_date

### 2. Usage Limits
- ✅ Total usage hasn't reached `usage_limit`
- ✅ User usage hasn't reached `usage_limit_per_user`

### 3. Order Requirements
- ✅ Order total meets `min_order_amount`

### 4. Product/Category Restrictions
- ✅ If `applicable_product_ids` is set, at least one product in cart must match
- ✅ If `applicable_category_ids` is set, at least one category in cart must match

### 5. User Restrictions
- ✅ If `applicable_user_ids` is set, user ID must be in the list

## Discount Calculation

### Percentage Discount

```java
discount = (orderTotal * discountValue) / 100
if (maxDiscountAmount != null && discount > maxDiscountAmount) {
    discount = maxDiscountAmount
}
```

**Example**:
- Order total: 1,000,000 VND
- Discount: 10%
- Max cap: 50,000 VND
- **Result**: 50,000 VND (capped)

### Fixed Amount Discount

```java
discount = discountValue
if (discount > orderTotal) {
    discount = orderTotal
}
```

**Example**:
- Order total: 200,000 VND
- Discount: 50,000 VND
- **Result**: 50,000 VND

### Free Shipping

```java
discount = discountValue  // Shipping cost
```

## Auto-Apply Coupons

Coupons with `autoApply = true` are automatically checked and applied when conditions are met.

### How It Works

1. User views cart or checkout
2. System calls `findBestAutoApplyCoupon()`
3. All auto-apply coupons are validated
4. Coupon with highest discount is selected
5. Applied automatically to order

**Example Use Cases**:
- Spend 500,000 VND, get 50,000 VND off
- Free shipping for orders over 300,000 VND
- Birthday month discount (user-specific)

## Integration with Order System

### In CheckoutService

```java
@Transactional
public Order checkout(CheckoutRequest request) {
    // Calculate order total
    Long orderTotal = calculateOrderTotal(cart);

    // Validate coupon if provided
    Long discount = 0L;
    String appliedCoupon = null;

    if (request.couponCode() != null) {
        CouponValidationResponse validation = couponService.validateCoupon(
            request.couponCode(),
            userId,
            orderTotal,
            productIds,
            categoryIds
        );

        if (!validation.valid()) {
            throw new ApiException(validation.message());
        }

        discount = validation.discountAmount();
        appliedCoupon = validation.couponCode();
    } else {
        // Check for auto-apply coupons
        CouponEntity autoCoupon = couponService.findBestAutoApplyCoupon(
            userId, orderTotal, productIds, categoryIds
        );

        if (autoCoupon != null) {
            CouponValidationResponse validation = couponService.validateCoupon(
                autoCoupon.getCode(), userId, orderTotal, productIds, categoryIds
            );
            discount = validation.discountAmount();
            appliedCoupon = autoCoupon.getCode();
        }
    }

    // Create order with discount
    Order order = new Order();
    order.setSubtotal(orderTotal);
    order.setDiscount(discount);
    order.setTotal(orderTotal - discount);
    order.setCouponCode(appliedCoupon);
    order = orderRepo.save(order);

    // Record coupon usage
    if (appliedCoupon != null) {
        couponService.applyCoupon(appliedCoupon, userId, order.getId(), discount);
    }

    return order;
}
```

## Frontend Integration

### Validate Coupon in Cart

```javascript
async function validateCoupon(couponCode, orderTotal, productIds, categoryIds) {
  const response = await fetch('/api/v1/coupons/validate', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      couponCode,
      orderTotal,
      productIds,
      categoryIds
    })
  });

  const result = await response.json();

  if (result.valid) {
    // Apply discount
    displayDiscount(result.discountAmount);
    updateTotal(orderTotal - result.discountAmount);
    showSuccessMessage(result.message);
  } else {
    // Show error
    showErrorMessage(result.message);
  }
}
```

### Display Available Coupons

```javascript
async function loadAvailableCoupons() {
  const response = await fetch('/api/v1/coupons/active', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const coupons = await response.json();

  const container = document.getElementById('available-coupons');
  container.innerHTML = coupons.map(coupon => `
    <div class="coupon-card">
      <div class="coupon-code">${coupon.code}</div>
      <div class="coupon-name">${coupon.name}</div>
      <div class="coupon-description">${coupon.description}</div>
      <div class="coupon-details">
        ${formatCouponDiscount(coupon)}
        ${coupon.minOrderAmount ? `Min: ${formatPrice(coupon.minOrderAmount)}` : ''}
      </div>
      <button onclick="applyCoupon('${coupon.code}')">Apply</button>
    </div>
  `).join('');
}

function formatCouponDiscount(coupon) {
  if (coupon.type === 'PERCENTAGE') {
    return `${coupon.discountValue}% off${
      coupon.maxDiscountAmount ? ` (max ${formatPrice(coupon.maxDiscountAmount)})` : ''
    }`;
  } else if (coupon.type === 'FIXED_AMOUNT') {
    return `${formatPrice(coupon.discountValue)} off`;
  } else {
    return 'Free shipping';
  }
}
```

### React Component

```jsx
import { useState } from 'react';

function CouponInput({ orderTotal, productIds, categoryIds, onApply }) {
  const [couponCode, setCouponCode] = useState('');
  const [validating, setValidating] = useState(false);
  const [error, setError] = useState(null);

  async function handleApply() {
    setValidating(true);
    setError(null);

    try {
      const response = await fetch('/api/v1/coupons/validate', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          couponCode,
          orderTotal,
          productIds,
          categoryIds
        })
      });

      const result = await response.json();

      if (result.valid) {
        onApply(result);
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError('Failed to validate coupon');
    } finally {
      setValidating(false);
    }
  }

  return (
    <div className="coupon-input">
      <input
        type="text"
        placeholder="Enter coupon code"
        value={couponCode}
        onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
      />
      <button onClick={handleApply} disabled={validating || !couponCode}>
        {validating ? 'Validating...' : 'Apply'}
      </button>
      {error && <div className="error">{error}</div>}
    </div>
  );
}
```

## Admin Management

### Create Coupon Examples

#### Flash Sale (24 hours, 20% off)

```json
{
  "code": "FLASH20",
  "name": "Flash Sale 20%",
  "description": "24-hour flash sale!",
  "type": "PERCENTAGE",
  "discountValue": 20,
  "maxDiscountAmount": 200000,
  "minOrderAmount": 500000,
  "startDate": "2025-07-15T00:00:00Z",
  "endDate": "2025-07-15T23:59:59Z",
  "usageLimit": 100,
  "usageLimitPerUser": 1,
  "autoApply": false
}
```

#### Free Shipping for VIP Users

```json
{
  "code": "VIPSHIP",
  "name": "VIP Free Shipping",
  "description": "Free shipping for VIP members",
  "type": "FREE_SHIPPING",
  "discountValue": 30000,
  "minOrderAmount": 200000,
  "startDate": "2025-01-01T00:00:00Z",
  "endDate": "2025-12-31T23:59:59Z",
  "usageLimitPerUser": null,
  "autoApply": true,
  "applicableUserIds": [101, 102, 103, 104]
}
```

#### Category-Specific Discount

```json
{
  "code": "ELECTRONICS15",
  "name": "Electronics 15% Off",
  "description": "15% off all electronics",
  "type": "PERCENTAGE",
  "discountValue": 15,
  "maxDiscountAmount": 500000,
  "minOrderAmount": 1000000,
  "startDate": "2025-06-01T00:00:00Z",
  "endDate": "2025-06-30T23:59:59Z",
  "usageLimit": 500,
  "autoApply": false,
  "applicableCategoryIds": [5, 8, 12]
}
```

## Analytics & Reporting

### Track Coupon Performance

```sql
-- Most used coupons
SELECT c.code, c.name, c.usage_count,
       SUM(cu.discount_amount) as total_discount
FROM coupon c
LEFT JOIN coupon_usage cu ON c.id = cu.coupon_id
GROUP BY c.id
ORDER BY c.usage_count DESC
LIMIT 10;

-- Revenue impact by coupon
SELECT c.code, COUNT(cu.id) as uses,
       AVG(cu.discount_amount) as avg_discount,
       SUM(cu.discount_amount) as total_discount
FROM coupon c
JOIN coupon_usage cu ON c.id = cu.coupon_id
WHERE cu.used_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY c.id
ORDER BY total_discount DESC;

-- User coupon usage patterns
SELECT u.id, u.email, COUNT(cu.id) as coupon_uses,
       SUM(cu.discount_amount) as total_savings
FROM user u
JOIN coupon_usage cu ON u.id = cu.user_id
GROUP BY u.id
ORDER BY total_savings DESC
LIMIT 100;
```

## Performance Optimization

### Indexes

Ensure these indexes exist:
- `idx_coupon_code` - Fast code lookup
- `idx_coupon_status` - Filter active coupons
- `idx_coupon_dates` - Date range queries
- `idx_coupon_auto_apply` - Auto-apply lookup
- `idx_coupon_usage_coupon` - Usage count queries
- `idx_coupon_usage_user` - Per-user usage queries

### Caching Strategy

```java
@Cacheable(value = "active-coupons", ttl = 300)
public List<CouponEntity> getActiveCoupons() {
    return couponRepo.findActiveValidCoupons(Instant.now());
}

@Cacheable(value = "coupon-by-code", key = "#code", ttl = 600)
public CouponEntity getCouponByCode(String code) {
    return couponRepo.findByCode(code.toUpperCase())
        .orElseThrow(() -> new ApiException("Coupon not found"));
}
```

## Security Considerations

1. **Coupon Code Guessing**: Use random, hard-to-guess codes
2. **Rate Limiting**: Limit validation attempts per IP/user
3. **Admin Access**: Restrict coupon creation/editing to admin role
4. **Validation on Server**: Always validate on backend, never trust client
5. **Usage Tracking**: Prevent double-application to same order

## Testing

### Manual Testing with cURL

```bash
# Validate coupon
curl -X POST "http://localhost:8080/api/v1/coupons/validate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "couponCode": "SUMMER2025",
    "orderTotal": 500000,
    "productIds": [1, 2, 3],
    "categoryIds": [10]
  }'

# Get active coupons
curl "http://localhost:8080/api/v1/coupons/active" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create coupon (admin)
curl -X POST "http://localhost:8080/api/v1/admin/coupons" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "TEST10",
    "name": "Test Coupon",
    "description": "Test description",
    "type": "PERCENTAGE",
    "discountValue": 10,
    "startDate": "2025-01-01T00:00:00Z",
    "endDate": "2025-12-31T23:59:59Z"
  }'
```

## Future Enhancements

1. **Stackable Coupons**: Allow multiple coupons per order
2. **Coupon Codes from Partners**: External coupon integration
3. **Referral Coupons**: Generate unique codes for user referrals
4. **Smart Recommendations**: Suggest best coupon for cart
5. **Geolocation**: Location-based coupons
6. **Time-based**: Happy hour discounts
7. **Buy X Get Y**: Bundle promotions
8. **Loyalty Points**: Convert points to coupons
9. **A/B Testing**: Test coupon effectiveness
10. **Social Sharing**: Share coupons, earn rewards

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| "Coupon code not found" | Invalid or deleted code | Check code spelling |
| "Coupon has expired" | Past end_date | Update end_date or create new coupon |
| "Usage limit reached" | usage_count >= usage_limit | Increase limit or mark as DEPLETED |
| "Minimum order amount..." | Order below threshold | Add more items or different coupon |
| "Does not apply to products..." | Product/category restriction | Check applicable IDs |
| Cannot delete coupon | usage_count > 0 | Set status to INACTIVE instead |

## Monitoring Metrics

Key metrics to track:
- Coupon redemption rate
- Average discount per order
- Revenue with vs without coupons
- Most popular coupon codes
- Coupon fraud attempts
- Time to redemption (create → use)
