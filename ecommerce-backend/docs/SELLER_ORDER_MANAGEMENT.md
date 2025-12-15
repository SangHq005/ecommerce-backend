# Seller Order Management

## Overview

The Seller Order Management system provides comprehensive tools for sellers to manage their orders efficiently. Features include:
- **Order tracking** - View all orders with filtering and pagination
- **Status management** - Update order status through defined workflows
- **Order details** - Complete order information with customer and product details
- **Statistics dashboard** - Real-time metrics and revenue tracking
- **Cancellation** - Cancel orders before shipment
- **Status validation** - Enforced status transition rules

## Order Lifecycle

### Order Statuses

| Status | Description | Next Valid Status |
|--------|-------------|-------------------|
| **SUBMITTED** | Order created, awaiting payment | PAYMENT_PENDING, CANCELLED |
| **PAYMENT_PENDING** | Payment initiated but not confirmed | PAID, CANCELLED |
| **PAID** | Payment confirmed, awaiting seller action | PROCESSING, CANCELLED |
| **PROCESSING** | Seller is processing the order | READY_TO_SHIP, CANCELLED |
| **READY_TO_SHIP** | Order packed and ready | SHIPPED, CANCELLED |
| **SHIPPED** | Order shipped to customer | DELIVERED, REFUND_REQUESTED |
| **DELIVERED** | Order delivered successfully | COMPLETED, REFUND_REQUESTED |
| **COMPLETED** | Order completed (final state) | - |
| **CANCELLED** | Order cancelled (final state) | - |
| **REFUND_REQUESTED** | Customer requested refund | REFUNDED, COMPLETED |
| **REFUNDED** | Order refunded (final state) | - |

### Status Flow Diagram

```
SUBMITTED → PAYMENT_PENDING → PAID
                                ↓
                           PROCESSING
                                ↓
                          READY_TO_SHIP
                                ↓
                             SHIPPED
                                ↓
                            DELIVERED
                                ↓
                            COMPLETED

Cancellation: PAID/PROCESSING/READY_TO_SHIP → CANCELLED
Refund: SHIPPED/DELIVERED → REFUND_REQUESTED → REFUNDED
```

## API Endpoints

### 1. Get All Orders

```http
GET /api/v1/seller/orders?shopId={shopId}&page=0&size=20&sortBy=createdAt&sortDirection=DESC
Authorization: Bearer {token}
```

**Query Parameters**:
- `shopId` (required) - Shop ID
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `sortBy` (optional, default: "createdAt") - Sort field
- `sortDirection` (optional, default: "DESC") - ASC or DESC

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "orderCode": "ORD-2025-0001",
      "userId": 123,
      "status": "PROCESSING",
      "totalAmount": 500000,
      "currency": "VND",
      "createdAt": "2025-01-04T10:30:00",
      "itemCount": 3
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 150,
  "totalPages": 8
}
```

---

### 2. Get Orders by Status

```http
GET /api/v1/seller/orders/status/{status}?shopId={shopId}&page=0&size=20
Authorization: Bearer {token}
```

**Path Parameters**:
- `status` - Order status (PAID, PROCESSING, SHIPPED, etc.)

**Query Parameters**:
- `shopId` (required) - Shop ID
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size

**Response**: Same as "Get All Orders"

---

### 3. Get Order Details

```http
GET /api/v1/seller/orders/{orderId}?shopId={shopId}
Authorization: Bearer {token}
```

**Path Parameters**:
- `orderId` - Order ID

**Query Parameters**:
- `shopId` (required) - Shop ID

**Response** (200 OK):
```json
{
  "id": 1,
  "orderCode": "ORD-2025-0001",
  "userId": 123,
  "userEmail": "customer@example.com",
  "shopId": 5,
  "status": "PROCESSING",
  "totalAmount": 500000,
  "currency": "VND",
  "createdAt": "2025-01-04T10:30:00",
  "updatedAt": "2025-01-04T11:00:00",
  "items": [
    {
      "id": 1,
      "productId": 456,
      "productName": "Smartphone XYZ",
      "skuId": 789,
      "quantity": 1,
      "unitPrice": 300000,
      "totalPrice": 300000
    },
    {
      "id": 2,
      "productId": 457,
      "productName": "Phone Case",
      "skuId": 790,
      "quantity": 2,
      "unitPrice": 100000,
      "totalPrice": 200000
    }
  ]
}
```

---

### 4. Update Order Status

```http
PUT /api/v1/seller/orders/{orderId}/status?shopId={shopId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "SHIPPED",
  "note": "Shipped via Express Delivery",
  "trackingNumber": "TRACK123456"
}
```

**Path Parameters**:
- `orderId` - Order ID

**Query Parameters**:
- `shopId` (required) - Shop ID

**Request Body**:
- `status` (required) - New status
- `note` (optional) - Note about status update
- `trackingNumber` (optional) - Tracking number for shipments

**Response** (200 OK): OrderDetailResponse (same as Get Order Details)

**Error Responses**:
- `400 Bad Request` - Invalid status transition
- `403 Forbidden` - Order doesn't belong to shop
- `404 Not Found` - Order not found

---

### 5. Get Order Statistics

```http
GET /api/v1/seller/orders/stats?shopId={shopId}
Authorization: Bearer {token}
```

**Query Parameters**:
- `shopId` (required) - Shop ID

**Response** (200 OK):
```json
{
  "totalOrders": 150,
  "pendingOrders": 12,
  "processingOrders": 8,
  "shippedOrders": 5,
  "completedOrders": 120,
  "cancelledOrders": 3,
  "refundRequested": 2,
  "totalRevenue": 75000000,
  "todayRevenue": 2500000
}
```

**Metrics Explained**:
- `totalOrders` - All orders in the shop
- `pendingOrders` - Orders with status PAID (awaiting action)
- `processingOrders` - Orders in PROCESSING or READY_TO_SHIP
- `shippedOrders` - Orders with status SHIPPED
- `completedOrders` - Orders with status COMPLETED or DELIVERED
- `cancelledOrders` - Orders with status CANCELLED
- `refundRequested` - Orders with status REFUND_REQUESTED
- `totalRevenue` - Total revenue from completed orders (VND)
- `todayRevenue` - Revenue from today's completed orders (VND)

---

### 6. Cancel Order

```http
POST /api/v1/seller/orders/{orderId}/cancel?shopId={shopId}&reason=Out+of+stock
Authorization: Bearer {token}
```

**Path Parameters**:
- `orderId` - Order ID

**Query Parameters**:
- `shopId` (required) - Shop ID
- `reason` (optional) - Cancellation reason

**Response** (200 OK):
```json
{
  "message": "Order cancelled successfully"
}
```

**Business Rules**:
- Can only cancel orders before SHIPPED status
- Cannot cancel SHIPPED, DELIVERED, or COMPLETED orders
- Order status changes to CANCELLED

---

## Status Transition Validation

The system enforces strict status transition rules:

### Valid Transitions

```java
PAID → PROCESSING, CANCELLED
PROCESSING → READY_TO_SHIP, CANCELLED
READY_TO_SHIP → SHIPPED, CANCELLED
SHIPPED → DELIVERED, REFUND_REQUESTED
DELIVERED → COMPLETED, REFUND_REQUESTED
REFUND_REQUESTED → REFUNDED, COMPLETED
```

### Invalid Transitions (Will Fail)

- Cannot go from SHIPPED back to PROCESSING
- Cannot go from COMPLETED to any other status
- Cannot go from CANCELLED to any other status
- Cannot skip intermediate statuses (e.g., PAID → SHIPPED directly)

**Error Example**:
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid status transition from SHIPPED to PROCESSING"
}
```

## Frontend Integration

### Display Orders List

```javascript
async function loadOrders(shopId, page = 0, status = null) {
  let url = `/api/v1/seller/orders?shopId=${shopId}&page=${page}&size=20`;

  if (status) {
    url = `/api/v1/seller/orders/status/${status}?shopId=${shopId}&page=${page}&size=20`;
  }

  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const data = await response.json();

  // Render orders
  renderOrders(data.content);
  renderPagination(data.totalPages, page);
}

function renderOrders(orders) {
  const container = document.getElementById('orders-list');
  container.innerHTML = orders.map(order => `
    <div class="order-card" onclick="viewOrder(${order.id})">
      <div class="order-code">${order.orderCode}</div>
      <div class="order-status status-${order.status.toLowerCase()}">
        ${order.status}
      </div>
      <div class="order-amount">${formatPrice(order.totalAmount)} VND</div>
      <div class="order-date">${formatDate(order.createdAt)}</div>
      <div class="order-items">${order.itemCount} items</div>
    </div>
  `).join('');
}
```

### Update Order Status

```javascript
async function updateOrderStatus(shopId, orderId, newStatus, trackingNumber = null) {
  const response = await fetch(
    `/api/v1/seller/orders/${orderId}/status?shopId=${shopId}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: newStatus,
        trackingNumber: trackingNumber,
        note: `Updated to ${newStatus}`
      })
    }
  );

  if (!response.ok) {
    const error = await response.json();
    alert(error.message);
    return;
  }

  const order = await response.json();
  showSuccessMessage('Order status updated successfully');
  refreshOrderDetails(order);
}
```

### Display Order Statistics Dashboard

```javascript
async function loadDashboard(shopId) {
  const response = await fetch(`/api/v1/seller/orders/stats?shopId=${shopId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const stats = await response.json();

  document.getElementById('total-orders').textContent = stats.totalOrders;
  document.getElementById('pending-orders').textContent = stats.pendingOrders;
  document.getElementById('processing-orders').textContent = stats.processingOrders;
  document.getElementById('shipped-orders').textContent = stats.shippedOrders;
  document.getElementById('completed-orders').textContent = stats.completedOrders;
  document.getElementById('total-revenue').textContent = formatPrice(stats.totalRevenue);
  document.getElementById('today-revenue').textContent = formatPrice(stats.todayRevenue);

  // Highlight pending orders if any
  if (stats.pendingOrders > 0) {
    document.getElementById('pending-alert').style.display = 'block';
  }
}
```

### React Component Example

```jsx
import { useState, useEffect } from 'react';

function SellerOrderManagement({ shopId }) {
  const [orders, setOrders] = useState([]);
  const [stats, setStats] = useState(null);
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadStats();
    loadOrders();
  }, [shopId, selectedStatus, page]);

  async function loadStats() {
    const response = await fetch(`/api/v1/seller/orders/stats?shopId=${shopId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await response.json();
    setStats(data);
  }

  async function loadOrders() {
    let url = `/api/v1/seller/orders?shopId=${shopId}&page=${page}`;
    if (selectedStatus !== 'ALL') {
      url = `/api/v1/seller/orders/status/${selectedStatus}?shopId=${shopId}&page=${page}`;
    }

    const response = await fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await response.json();
    setOrders(data.content);
  }

  async function updateStatus(orderId, newStatus) {
    const response = await fetch(
      `/api/v1/seller/orders/${orderId}/status?shopId=${shopId}`,
      {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
      }
    );

    if (response.ok) {
      loadOrders();
      loadStats();
    }
  }

  return (
    <div className="seller-dashboard">
      {/* Stats Dashboard */}
      {stats && (
        <div className="stats-grid">
          <StatCard title="Pending" value={stats.pendingOrders} color="orange" />
          <StatCard title="Processing" value={stats.processingOrders} color="blue" />
          <StatCard title="Shipped" value={stats.shippedOrders} color="purple" />
          <StatCard title="Completed" value={stats.completedOrders} color="green" />
          <StatCard title="Revenue" value={formatPrice(stats.totalRevenue)} />
        </div>
      )}

      {/* Status Filter */}
      <div className="status-filter">
        <button onClick={() => setSelectedStatus('ALL')}>All</button>
        <button onClick={() => setSelectedStatus('PAID')}>Pending</button>
        <button onClick={() => setSelectedStatus('PROCESSING')}>Processing</button>
        <button onClick={() => setSelectedStatus('SHIPPED')}>Shipped</button>
        <button onClick={() => setSelectedStatus('COMPLETED')}>Completed</button>
      </div>

      {/* Orders List */}
      <div className="orders-list">
        {orders.map(order => (
          <OrderCard
            key={order.id}
            order={order}
            onUpdateStatus={updateStatus}
          />
        ))}
      </div>
    </div>
  );
}
```

## Business Workflows

### Workflow 1: Standard Order Fulfillment

```
1. Customer pays → Order status: PAID
2. Seller receives notification
3. Seller clicks "Start Processing" → Status: PROCESSING
4. Seller packs order
5. Seller clicks "Mark Ready to Ship" → Status: READY_TO_SHIP
6. Seller arranges shipment, enters tracking number
7. Seller clicks "Mark as Shipped" → Status: SHIPPED
8. Delivery completed → Status: DELIVERED (system or manual)
9. After confirmation period → Status: COMPLETED
```

### Workflow 2: Order Cancellation

```
1. Order status: PAID or PROCESSING
2. Seller clicks "Cancel Order"
3. System validates (must be before SHIPPED)
4. Seller enters cancellation reason
5. Status changes to CANCELLED
6. Customer notified (if notification system exists)
7. Payment refund initiated (if payment integration exists)
```

### Workflow 3: Refund Request

```
1. Order status: SHIPPED or DELIVERED
2. Customer requests refund
3. Status changes to REFUND_REQUESTED
4. Seller reviews request in "Refund Requests" tab
5. Seller approves → Status: REFUNDED
   OR Seller rejects → Status: COMPLETED
```

## Performance Optimization

### Database Indexes

Ensure these indexes exist for optimal query performance:

```sql
CREATE INDEX idx_orders_shop_id ON orders(shop_id);
CREATE INDEX idx_orders_shop_status ON orders(shop_id, status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_shop_created ON orders(shop_id, created_at);
```

### Caching Strategy

```java
// Cache order stats for 5 minutes
@Cacheable(value = "order-stats", key = "#shopId", ttl = 300)
public OrderStatsResponse getOrderStats(Long shopId) {
    // ...
}

// Evict cache when order status changes
@CacheEvict(value = "order-stats", key = "#shopId")
public OrderDetailResponse updateOrderStatus(...) {
    // ...
}
```

### Pagination Best Practices

- Default page size: 20 orders
- Maximum page size: 100 orders
- Always sort by `createdAt DESC` for recent orders first
- Use cursor-based pagination for very large datasets

## Security

### Authorization Checks

Every endpoint verifies:
1. User is authenticated (JWT token valid)
2. Shop ID belongs to authenticated user
3. Order belongs to specified shop

```java
// Verify order belongs to shop
if (!order.getShopId().equals(shopId)) {
    throw ApiException.forbidden("Order does not belong to your shop");
}
```

### Rate Limiting

Recommended rate limits:
- Order list: 60 requests/minute
- Order details: 120 requests/minute
- Status updates: 30 requests/minute
- Stats: 20 requests/minute

## Testing

### Manual Testing with cURL

```bash
# Get all orders
curl "http://localhost:8080/api/v1/seller/orders?shopId=1&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get pending orders
curl "http://localhost:8080/api/v1/seller/orders/status/PAID?shopId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get order details
curl "http://localhost:8080/api/v1/seller/orders/123?shopId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Update order status
curl -X PUT "http://localhost:8080/api/v1/seller/orders/123/status?shopId=1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"PROCESSING","note":"Started processing"}'

# Get statistics
curl "http://localhost:8080/api/v1/seller/orders/stats?shopId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Cancel order
curl -X POST "http://localhost:8080/api/v1/seller/orders/123/cancel?shopId=1&reason=Out+of+stock" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Monitoring & Analytics

### Key Metrics to Track

1. **Order Processing Time**
   - Average time from PAID to SHIPPED
   - Target: < 24 hours

2. **Cancellation Rate**
   - Cancelled orders / Total orders
   - Target: < 5%

3. **Refund Rate**
   - Refunded orders / Delivered orders
   - Target: < 2%

4. **Order Value Trends**
   - Average order value over time
   - Peak ordering times

### SQL Queries for Analytics

```sql
-- Average processing time
SELECT
    AVG(TIMESTAMPDIFF(HOUR, created_at, updated_at)) as avg_hours
FROM orders
WHERE shop_id = 1
AND status = 'SHIPPED'
AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Orders by status (last 30 days)
SELECT status, COUNT(*) as count
FROM orders
WHERE shop_id = 1
AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY status;

-- Daily revenue
SELECT
    DATE(created_at) as date,
    COUNT(*) as orders,
    SUM(total_amount) as revenue
FROM orders
WHERE shop_id = 1
AND status IN ('COMPLETED', 'DELIVERED')
AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

## Future Enhancements

1. **Bulk Operations**
   - Bulk status updates
   - Bulk export to CSV/Excel
   - Print packing slips

2. **Notifications**
   - Email notifications for status changes
   - SMS alerts for urgent orders
   - Push notifications to mobile app

3. **Advanced Filtering**
   - Filter by date range
   - Filter by customer
   - Filter by product
   - Filter by order value

4. **Order Notes**
   - Internal notes for each order
   - Customer communication log
   - Status change history

5. **Shipping Integration**
   - Auto-generate shipping labels
   - Real-time tracking updates
   - Multi-carrier support

6. **Inventory Integration**
   - Auto-reduce stock on order
   - Stock alerts
   - Reserved stock management

7. **Returns Management**
   - Return request workflow
   - Return tracking
   - Restocking automation

8. **Analytics Dashboard**
   - Visual charts and graphs
   - Revenue forecasting
   - Seller performance metrics

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| "Order not found" | Invalid order ID | Verify order exists and belongs to shop |
| "Invalid status transition" | Wrong status sequence | Check valid transitions table |
| "Order doesn't belong to shop" | Wrong shop ID | Verify shop ownership |
| "Cannot cancel shipped order" | Order already shipped | Only PAID/PROCESSING can be cancelled |
| Empty order list | No orders or wrong filters | Check shop ID and status filters |
| Stats showing 0 | No completed orders | Stats calculate from completed orders only |

## Best Practices

1. **Always validate shop ownership** before any operation
2. **Log all status changes** for audit trail
3. **Notify customers** of status changes (email/SMS)
4. **Use pagination** for large order lists
5. **Cache statistics** to reduce database load
6. **Handle errors gracefully** with clear messages
7. **Test status transitions** thoroughly
8. **Monitor processing times** to identify bottlenecks
9. **Regular backups** of order data
10. **Document cancellation reasons** for analysis
