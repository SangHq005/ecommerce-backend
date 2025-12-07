# Wishlist Feature

## Overview

The Wishlist feature allows authenticated users to save products for later purchase. Users can add/remove products, view their wishlist, and check if a product is already wishlisted.

## Architecture

### Components

1. **WishlistItemEntity** - MySQL entity for persistent wishlist storage
2. **WishlistItemJpaRepository** - Data access layer
3. **WishlistService** - Business logic
4. **WishlistController** - REST API endpoints

### Data Storage

- **MySQL**: Persistent wishlist items with user-product relationship
- **MongoDB**: User events tracking (WISHLIST_ADD events for recommendations)

## Database Schema

### wishlist_item Table

```sql
CREATE TABLE wishlist_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(500) NULL,

    CONSTRAINT uk_wishlist_user_product UNIQUE (user_id, product_id),
    INDEX idx_wishlist_user (user_id),
    INDEX idx_wishlist_product (product_id)
);
```

**Key Features**:
- Unique constraint prevents duplicate entries
- Cascade delete when user or product is deleted
- Indexed for fast lookups by user and product

## API Endpoints

### 1. Add to Wishlist

```http
POST /api/v1/wishlist
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 123,
  "note": "Want to buy for birthday gift"
}
```

**Request Body**:
- `productId` (required) - Product ID to add
- `note` (optional) - Personal note about the product

**Response** (200 OK):
```json
{
  "id": 1,
  "productId": 123,
  "productName": "Smartphone XYZ",
  "productSlug": "smartphone-xyz",
  "mainImageUrl": "https://...",
  "minPrice": 5990000,
  "status": "ACTIVE",
  "addedAt": "2025-01-04T00:00:00Z",
  "note": "Want to buy for birthday gift"
}
```

**Error Responses**:
- `400 Bad Request` - Product is not available
- `404 Not Found` - Product not found
- `409 Conflict` - Product already in wishlist

---

### 2. Remove from Wishlist

```http
DELETE /api/v1/wishlist/{productId}
Authorization: Bearer {token}
```

**Path Parameters**:
- `productId` - Product ID to remove

**Response** (200 OK):
```json
{
  "message": "Product removed from wishlist"
}
```

**Error Response**:
- `404 Not Found` - Product not in wishlist

---

### 3. Get Wishlist

```http
GET /api/v1/wishlist
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "productId": 123,
    "productName": "Smartphone XYZ",
    "productSlug": "smartphone-xyz",
    "mainImageUrl": "https://...",
    "minPrice": 5990000,
    "status": "ACTIVE",
    "addedAt": "2025-01-04T00:00:00Z",
    "note": "Want to buy for birthday gift"
  },
  {
    "id": 2,
    "productId": 456,
    "productName": "Laptop ABC",
    "productSlug": "laptop-abc",
    "mainImageUrl": "https://...",
    "minPrice": 15990000,
    "status": "ACTIVE",
    "addedAt": "2025-01-03T12:00:00Z",
    "note": null
  }
]
```

**Note**: Deleted products are automatically filtered out

---

### 4. Check if Product in Wishlist

```http
GET /api/v1/wishlist/check/{productId}
Authorization: Bearer {token}
```

**Path Parameters**:
- `productId` - Product ID to check

**Response** (200 OK):
```json
{
  "inWishlist": true
}
```

---

### 5. Get Wishlist Count

```http
GET /api/v1/wishlist/count
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "count": 5
}
```

---

### 6. Clear Wishlist

```http
DELETE /api/v1/wishlist
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "message": "Wishlist cleared"
}
```

## Business Rules

### Add to Wishlist
1. User must be authenticated
2. Product must exist in database
3. Product status must be "ACTIVE"
4. Product cannot already be in user's wishlist
5. Event is tracked for recommendation system

### Remove from Wishlist
1. User must be authenticated
2. Product must be in user's wishlist

### Get Wishlist
1. User must be authenticated
2. Deleted products are filtered out automatically
3. Returns empty array if no items

## Integration with Recommendation System

When a user adds a product to their wishlist, the system:
1. Saves the wishlist item to MySQL
2. Tracks a "WISHLIST_ADD" event in MongoDB
3. Updates user's category affinity for better personalization

```java
// Automatically called in WishlistService
recommendationService.trackEvent(userId, productId, "WISHLIST_ADD");
```

## Frontend Integration Examples

### Add to Wishlist

```javascript
async function addToWishlist(productId, note = null) {
  const response = await fetch('/api/v1/wishlist', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ productId, note })
  });

  if (response.status === 409) {
    alert('Product already in wishlist');
    return;
  }

  const item = await response.json();
  console.log('Added to wishlist:', item);
  updateWishlistBadge();
}
```

### Toggle Wishlist Button

```javascript
async function toggleWishlist(productId) {
  // Check if already in wishlist
  const checkResponse = await fetch(`/api/v1/wishlist/check/${productId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const { inWishlist } = await checkResponse.json();

  if (inWishlist) {
    // Remove from wishlist
    await fetch(`/api/v1/wishlist/${productId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    updateHeartIcon(productId, false);
  } else {
    // Add to wishlist
    await fetch('/api/v1/wishlist', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ productId })
    });
    updateHeartIcon(productId, true);
  }

  updateWishlistBadge();
}
```

### Display Wishlist Page

```javascript
async function loadWishlist() {
  const response = await fetch('/api/v1/wishlist', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const items = await response.json();

  // Render wishlist items
  const container = document.getElementById('wishlist-container');
  container.innerHTML = items.map(item => `
    <div class="wishlist-item">
      <img src="${item.mainImageUrl}" alt="${item.productName}">
      <h3>${item.productName}</h3>
      <p>Price: ${formatPrice(item.minPrice)} VND</p>
      <p>Added: ${formatDate(item.addedAt)}</p>
      ${item.note ? `<p>Note: ${item.note}</p>` : ''}
      <button onclick="removeFromWishlist(${item.productId})">Remove</button>
      <a href="/products/${item.productSlug}">View Product</a>
    </div>
  `).join('');
}
```

### Update Wishlist Badge

```javascript
async function updateWishlistBadge() {
  const response = await fetch('/api/v1/wishlist/count', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const { count } = await response.json();

  const badge = document.getElementById('wishlist-badge');
  badge.textContent = count;
  badge.style.display = count > 0 ? 'inline' : 'none';
}
```

## React/Vue Example

### React Component

```jsx
import { useState, useEffect } from 'react';

function WishlistButton({ productId }) {
  const [inWishlist, setInWishlist] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    checkWishlistStatus();
  }, [productId]);

  async function checkWishlistStatus() {
    const response = await fetch(`/api/v1/wishlist/check/${productId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await response.json();
    setInWishlist(data.inWishlist);
  }

  async function toggleWishlist() {
    setLoading(true);
    try {
      if (inWishlist) {
        await fetch(`/api/v1/wishlist/${productId}`, {
          method: 'DELETE',
          headers: { 'Authorization': `Bearer ${token}` }
        });
        setInWishlist(false);
      } else {
        await fetch('/api/v1/wishlist', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ productId })
        });
        setInWishlist(true);
      }
    } catch (error) {
      console.error('Error toggling wishlist:', error);
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={toggleWishlist}
      disabled={loading}
      className={inWishlist ? 'in-wishlist' : ''}
    >
      {inWishlist ? '♥' : '♡'} {inWishlist ? 'In Wishlist' : 'Add to Wishlist'}
    </button>
  );
}
```

## Performance Considerations

### Database Indexes

The migration creates these indexes:
- `idx_wishlist_user` - Fast user wishlist lookup
- `idx_wishlist_product` - Fast product wishlist check
- `uk_wishlist_user_product` - Unique constraint prevents duplicates

### Query Optimization

1. **Batch Product Loading**: When fetching wishlist, products are loaded individually. For large wishlists, consider batch loading:

```java
// In WishlistService
List<Long> productIds = items.stream()
    .map(WishlistItemEntity::getProductId)
    .collect(Collectors.toList());
List<ProductEntity> products = productRepo.findAllById(productIds);
Map<Long, ProductEntity> productMap = products.stream()
    .collect(Collectors.toMap(ProductEntity::getId, p -> p));
```

2. **Caching**: Cache wishlist count for quick badge updates

3. **Pagination**: For users with large wishlists, add pagination support

## Security

1. **Authentication Required**: All endpoints require valid JWT token
2. **User Isolation**: Users can only access their own wishlist
3. **Product Validation**: Only ACTIVE products can be added
4. **SQL Injection Protection**: JPA prevents SQL injection
5. **Cascade Delete**: Wishlist items are automatically deleted when user or product is deleted

## Error Handling

### Common Errors

| Error Code | Scenario | Message |
|------------|----------|---------|
| 400 | Product not available | "Product is not available" |
| 401 | Not authenticated | "User not authenticated" |
| 404 | Product not found | "Product not found" |
| 404 | Not in wishlist | "Product not in wishlist" |
| 409 | Duplicate entry | "Product already in wishlist" |

## Testing

### Manual Testing with cURL

```bash
# Add to wishlist
curl -X POST "http://localhost:8080/api/v1/wishlist" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "note": "Test note"}'

# Get wishlist
curl "http://localhost:8080/api/v1/wishlist" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Check if in wishlist
curl "http://localhost:8080/api/v1/wishlist/check/1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get count
curl "http://localhost:8080/api/v1/wishlist/count" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Remove from wishlist
curl -X DELETE "http://localhost:8080/api/v1/wishlist/1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Clear wishlist
curl -X DELETE "http://localhost:8080/api/v1/wishlist" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Future Enhancements

1. **Wishlist Sharing**
   - Generate shareable links for wishlists
   - Make wishlists public/private

2. **Price Drop Alerts**
   - Notify users when wishlist items go on sale
   - Set price thresholds for notifications

3. **Wishlist Collections**
   - Organize wishlist into folders/categories
   - Create multiple wishlists (e.g., "Birthday", "Holiday")

4. **Stock Notifications**
   - Alert when out-of-stock items become available
   - Notify when low stock

5. **Analytics**
   - Track most wishlisted products
   - Measure wishlist-to-purchase conversion rate
   - Popular wishlist categories

6. **Social Features**
   - Share individual items on social media
   - See friends' wishlists
   - Gift registry functionality

## Migration Notes

The database migration (`V0072__create_wishlist_table.sql`) will:
- Create the `wishlist_item` table
- Add foreign key constraints
- Create indexes for performance
- Set up cascade delete rules

Migration is automatically applied on application startup via Flyway.

## Monitoring

Key metrics to monitor:
- Average wishlist size per user
- Wishlist conversion rate (items added to cart)
- Most wishlisted products
- Wishlist abandonment rate
- API response times
