# 4.2.3.5. KỸ THUẬT LẬP TRÌNH TRANG CHECKOUT

## 4.2.3.5.1. MÔ TẢ

### 4.2.3.5.1.1. Tổng quan

Trang Checkout là trang thanh toán cuối cùng trong quy trình mua hàng, được thiết kế với mục tiêu:
- **Xác nhận thông tin đơn hàng** trước khi thanh toán
- **Quản lý địa chỉ giao hàng** (chọn hoặc thêm mới)
- **Áp dụng mã giảm giá** và tính toán tổng tiền
- **Chọn phương thức thanh toán** (COD, VNPAY, MoMo)
- **Xử lý thanh toán** và chuyển hướng đến gateway
- **Tối ưu UX** với validation và feedback rõ ràng

### 4.2.3.5.1.2. Cấu trúc trang

Trang Checkout được chia thành 2 cột chính:

**Cột trái (8/12 - Desktop):**
1. **Danh sách sản phẩm** - Hiển thị các sản phẩm trong giỏ hàng
2. **Thông tin người đặt hàng** - Hiển thị tên và số điện thoại
3. **Địa chỉ nhận hàng** - Chọn địa chỉ có sẵn hoặc thêm mới
4. **Mã giảm giá** - Nhập và áp dụng coupon
5. **Phương thức thanh toán** - Chọn COD, VNPAY, hoặc MoMo

**Cột phải (4/12 - Desktop):**
1. **Tóm tắt đơn hàng** - Tổng tiền, khuyến mãi, phí vận chuyển
2. **Nút đặt hàng** - Xử lý checkout và thanh toán

---

## 4.2.3.5.2. KỸ THUẬT THIẾT KẾ

### 4.2.3.5.2.1. Component Structure

#### a) **Client Component với React Hooks**

```tsx
"use client"

import { useState, useEffect } from "react"
import { useCart } from "@/lib/cart-context"
import { useRouter } from "next/navigation"
```

**Giải thích:**
- `"use client"`: Đánh dấu component chạy trên client-side (cần interactivity)
- Sử dụng React Hooks để quản lý state và side effects
- `useCart`: Custom hook để truy cập giỏ hàng từ Context
- `useRouter`: Next.js hook để điều hướng

#### b) **State Management**

```tsx
const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("VNPAY")
const [loading, setLoading] = useState(false)
const [addresses, setAddresses] = useState<Address[]>([])
const [selectedAddressId, setSelectedAddressId] = useState<string>("new")
const [newAddress, setNewAddress] = useState<Address>({...})
const [couponCode, setCouponCode] = useState("")
const [appliedCoupon, setAppliedCoupon] = useState<{code: string; discount: number} | null>(null)
const [validatingCoupon, setValidatingCoupon] = useState(false)
const [idempotencyKey] = useState(uuidv4())
```

**Giải thích:**
- **Local State**: Quản lý UI state (loading, form inputs)
- **Idempotency Key**: UUID để đảm bảo request không bị trùng lặp
- **TypeScript Types**: Đảm bảo type safety

---

### 4.2.3.5.2.2. Data Flow

#### a) **Load Initial Data**

```tsx
useEffect(() => {
  async function init() {
    try {
      const addrList = await UserService.getAddresses()
      setAddresses(addrList)
      if (addrList.length > 0 && addrList[0].id) {
        setSelectedAddressId(String(addrList[0].id))
      }
    } catch (e) {
      console.error(e)
    }
  }
  init()
}, [])
```

**Giải thích:**
- `useEffect` với dependency `[]`: Chạy một lần khi component mount
- Load danh sách địa chỉ từ API
- Tự động chọn địa chỉ đầu tiên nếu có

#### b) **Cart Data từ Context**

```tsx
const { items, getTotalPrice, clearCart } = useCart()
```

**Giải thích:**
- Sử dụng Cart Context để lấy dữ liệu giỏ hàng
- Không cần fetch lại, data đã được sync từ server

---

### 4.2.3.5.2.3. Tính toán Giá

```tsx
const subtotalItems = getTotalPrice()
const subtotal = subtotalItems
const shipping = subtotal > 5000000 ? 0 : 30000 // Free shipping for orders over 5M
const discount = appliedCoupon?.discount || 0
const total = Math.max(0, subtotal + shipping - discount)
```

**Giải thích:**
- **Subtotal**: Tổng tiền sản phẩm từ giỏ hàng
- **Shipping**: Miễn phí vận chuyển cho đơn > 5 triệu
- **Discount**: Giảm giá từ coupon (nếu có)
- **Total**: Tổng cần thanh toán (đảm bảo >= 0)

---

### 4.2.3.5.2.4. Xử lý Coupon

#### a) **Validate Coupon**

```tsx
const handleApplyCoupon = async () => {
  if (!couponCode.trim()) {
    toast.error("Vui lòng nhập mã giảm giá")
    return
  }

  setValidatingCoupon(true)
  try {
    const result = await CouponService.validateCoupon({
      couponCode: couponCode.trim(),
      orderTotal: subtotal,
      productIds: items.map((item) => item.productId),
    })

    if (result.valid && result.discountAmount) {
      setAppliedCoupon({ 
        code: result.couponCode || couponCode, 
        discount: result.discountAmount 
      })
      toast.success(`Áp dụng mã thành công! Bạn được giảm ${formatPrice(result.discountAmount)}`)
      setCouponCode("")
    } else {
      toast.error(result.message || "Mã giảm giá không hợp lệ")
    }
  } catch (error: any) {
    toast.error(error.response?.data?.message || "Lỗi khi kiểm tra mã giảm giá")
  } finally {
    setValidatingCoupon(false)
  }
}
```

**Giải thích:**
- **Validation**: Kiểm tra coupon code với backend
- **Context**: Gửi kèm `orderTotal` và `productIds` để validate điều kiện
- **Error Handling**: Hiển thị thông báo lỗi rõ ràng
- **Loading State**: Disable button khi đang validate

#### b) **Remove Coupon**

```tsx
const handleRemoveCoupon = () => {
  setAppliedCoupon(null)
  toast.success("Đã gỡ mã giảm giá")
}
```

---

### 4.2.3.5.2.5. Xử lý Đặt hàng

#### a) **Validation và Tạo Địa chỉ**

```tsx
const handlePlaceOrder = async () => {
  setLoading(true)
  try {
    let finalAddressId: number | null = null

    if (selectedAddressId === "new") {
      // Validate form
      if (!newAddress.receiverName || !newAddress.receiverPhone || !newAddress.line1) {
        toast.error("Vui lòng điền đầy đủ thông tin địa chỉ")
        setLoading(false)
        return
      }

      // Create new address
      const createdAddr = await UserService.createAddress({
        receiverName: newAddress.receiverName,
        receiverPhone: newAddress.receiverPhone,
        line1: newAddress.line1,
        line2: newAddress.line2 || "",
        ward: newAddress.ward || "",
        district: newAddress.district || "",
        province: newAddress.province || "",
        postalCode: newAddress.postalCode || "",
      })

      if (createdAddr?.id) {
        finalAddressId = createdAddr.id
        setSelectedAddressId(String(createdAddr.id))
      } else {
        throw new Error("Không thể tạo địa chỉ mới")
      }
    } else {
      finalAddressId = Number(selectedAddressId)
    }
```

**Giải thích:**
- **Conditional Logic**: Kiểm tra nếu chọn "Thêm địa chỉ mới"
- **Form Validation**: Validate các trường bắt buộc
- **API Call**: Tạo địa chỉ mới nếu cần
- **Error Handling**: Throw error nếu không tạo được

#### b) **Checkout Request**

```tsx
    const orderPayload: CheckoutRequest = {
      addressId: finalAddressId,
      paymentMethod: paymentMethod,
      couponCode: appliedCoupon?.code,
      note: "Checkout from Web",
      items: items.map((item) => ({
        productId: item.productId,
        skuId: item.skuId,
        quantity: item.quantity,
      })),
    }

    const orders = await OrderService.checkout(orderPayload, idempotencyKey)
    const orderCode = orders[0]?.orderCode
```

**Giải thích:**
- **Payload Structure**: Map data từ UI sang API format
- **Idempotency Key**: Đảm bảo không tạo đơn trùng lặp
- **Response**: Backend trả về array orders (một đơn mỗi shop)

#### c) **Xử lý Thanh toán**

```tsx
    if (paymentMethod === "VNPAY" && orderCode) {
      const paymentRes = await OrderService.createVNPayPaymentUrl(orderCode)
      if (paymentRes.paymentUrl) {
        window.location.href = paymentRes.paymentUrl
        return
      }
    } else if (paymentMethod === "MOMO" && orderCode) {
      const paymentRes = await OrderService.createMomoPaymentUrl(orderCode)
      if (paymentRes.paymentUrl) {
        window.location.href = paymentRes.paymentUrl
        return
      }
    }

    // COD - No payment redirect
    await clearCart()
    toast.success(`Đặt hàng thành công! Mã đơn: ${orderCode}`)
    router.push("/profile/history")
```

**Giải thích:**
- **Payment Gateway**: Tạo payment URL và redirect
- **COD**: Không cần redirect, xử lý trực tiếp
- **Cleanup**: Xóa giỏ hàng sau khi đặt hàng thành công
- **Navigation**: Chuyển đến trang lịch sử đơn hàng

---

### 4.2.3.5.2.6. UI Components

#### a) **Address Selection với RadioGroup**

```tsx
<RadioGroup value={selectedAddressId} onValueChange={setSelectedAddressId}>
  {addresses.map((addr) => (
    <div key={addr.id} className={cn(
      "flex items-center space-x-3 border rounded-md p-4",
      selectedAddressId === String(addr.id) 
        ? "border-[#cb1c22] bg-red-50/10" 
        : "border-gray-100 hover:bg-gray-50"
    )}>
      <RadioGroupItem value={String(addr.id)} />
      <Label>
        <div>{addr.receiverName} ({addr.receiverPhone})</div>
        <div>{addr.line1}, {addr.ward}, {addr.district}, {addr.province}</div>
      </Label>
    </div>
  ))}
</RadioGroup>
```

**Giải thích:**
- **Controlled Component**: RadioGroup với value và onChange
- **Conditional Styling**: Highlight địa chỉ được chọn
- **Dynamic Rendering**: Map qua danh sách địa chỉ

#### b) **New Address Form (Conditional)**

```tsx
{selectedAddressId === "new" && (
  <div className="mt-6 space-y-5 border-t pt-6">
    <div className="grid grid-cols-2 gap-4">
      <Input 
        placeholder="Nguyễn Văn A" 
        value={newAddress.receiverName} 
        onChange={(e) => setNewAddress({ ...newAddress, receiverName: e.target.value })} 
      />
      <Input 
        placeholder="090..." 
        value={newAddress.receiverPhone} 
        onChange={(e) => setNewAddress({ ...newAddress, receiverPhone: e.target.value })} 
      />
    </div>
    {/* More fields... */}
  </div>
)}
```

**Giải thích:**
- **Conditional Rendering**: Chỉ hiển thị form khi chọn "Thêm mới"
- **Controlled Inputs**: Mỗi input bind với state
- **Grid Layout**: Responsive 2 cột trên desktop

#### c) **Coupon Input với Enter Key**

```tsx
<Input
  placeholder="Nhập mã ưu đãi..."
  value={couponCode}
  onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
  onKeyDown={(e) => e.key === "Enter" && handleApplyCoupon()}
/>
<Button onClick={handleApplyCoupon} disabled={validatingCoupon}>
  {validatingCoupon ? <Loader2 className="animate-spin" /> : "Áp dụng"}
</Button>
```

**Giải thích:**
- **Auto Uppercase**: Convert input thành chữ hoa
- **Enter Key**: Submit khi nhấn Enter
- **Loading State**: Hiển thị spinner khi đang validate

#### d) **Payment Method Selection**

```tsx
<RadioGroup value={paymentMethod} onValueChange={(v) => setPaymentMethod(v as PaymentMethod)}>
  <div className="flex items-center space-x-3">
    <RadioGroupItem value="COD" />
    <Label>
      <Banknote className="h-5 w-5" />
      <span>Thanh toán khi nhận hàng</span>
    </Label>
  </div>
  {/* VNPAY, MoMo options... */}
</RadioGroup>
```

**Giải thích:**
- **Icon Display**: Hiển thị icon cho mỗi phương thức
- **Type Casting**: Cast value về PaymentMethod type

---

### 4.2.3.5.2.7. Order Summary Sidebar

```tsx
<div className="lg:col-span-4 sticky top-24">
  <div className="bg-white rounded-lg border p-6">
    <h2>Thông tin đơn hàng</h2>
    
    <div className="space-y-3">
      <div className="flex justify-between">
        <span>Tổng tiền</span>
        <span>{formatPrice(subtotal)}</span>
      </div>
      
      <Separator />
      
      <div className="flex justify-between">
        <span>Tổng khuyến mãi</span>
        <span>-{formatPrice(discount)}</span>
      </div>
      
      <div className="flex justify-between">
        <span>Phí vận chuyển</span>
        <span>{shipping === 0 ? "Miễn phí" : formatPrice(shipping)}</span>
      </div>
    </div>
    
    <div className="border-t my-4"></div>
    
    <div className="flex justify-between">
      <span>Cần thanh toán</span>
      <span className="text-xl font-bold text-[#cb1c22]">{formatPrice(total)}</span>
    </div>
    
    <Button 
      className="w-full h-12 bg-[#cb1c22]" 
      onClick={handlePlaceOrder} 
      disabled={loading}
    >
      {loading ? <Loader2 className="animate-spin" /> : "Đặt hàng ngay"}
    </Button>
  </div>
</div>
```

**Giải thích:**
- **Sticky Position**: Sidebar dính khi scroll
- **Price Breakdown**: Hiển thị chi tiết từng khoản
- **Final Total**: Highlight tổng cần thanh toán
- **CTA Button**: Nút đặt hàng với loading state

---

### 4.2.3.5.2.8. Error Handling

#### a) **Empty Cart State**

```tsx
if (items.length === 0) {
  return (
    <>
      <Header />
      <main>
        <EmptyState
          title="Giỏ hàng của bạn đang trống"
          description="Vui lòng thêm sản phẩm vào giỏ trước khi thanh toán."
          action={{ label: "Tiếp tục mua hàng", href: "/products" }}
        />
      </main>
      <Footer />
    </>
  )
}
```

**Giải thích:**
- **Early Return**: Kiểm tra và hiển thị empty state nếu giỏ hàng trống
- **User Guidance**: Hướng dẫn người dùng quay lại mua hàng

#### b) **Try-Catch trong handlePlaceOrder**

```tsx
try {
  // ... checkout logic
} catch (error: any) {
  console.error("Order failed", error)
  toast.error(error.response?.data?.message || "Đặt hàng thất bại")
} finally {
  setLoading(false)
}
```

**Giải thích:**
- **Error Logging**: Log error để debug
- **User Feedback**: Hiển thị thông báo lỗi từ API hoặc message mặc định
- **Cleanup**: Đảm bảo loading state được reset

---

## 4.2.3.5.3. API INTEGRATION

### 4.2.3.5.3.1. Checkout Endpoint

**Backend API:**
```
POST /api/v1/checkout
Headers:
  - Authorization: Bearer {token}
  - Idempotency-Key: {uuid}
Body:
{
  "items": [
    {
      "productId": 1,
      "skuId": 1,
      "quantity": 2
    }
  ],
  "addressId": 1,
  "paymentMethod": "VNPAY",
  "couponCode": "SAVE10",
  "note": "Checkout from Web"
}
Response:
{
  "success": true,
  "data": [
    {
      "orderCode": "ORD20240101001",
      "status": "PENDING",
      "totalAmount": 1000000,
      ...
    }
  ]
}
```

**Frontend Service:**

```typescript
checkout: async (request: CheckoutRequest, idempotencyKey: string): Promise<OrderResponse[]> => {
  return apiCall<OrderResponse[]>(api.post("/api/v1/checkout", request, {
    headers: { "Idempotency-Key": idempotencyKey },
    timeout: 60000 // 60 seconds for checkout
  }));
}
```

**Giải thích:**
- **Idempotency Key**: Đảm bảo không tạo đơn trùng lặp khi retry
- **Extended Timeout**: 60s vì checkout phức tạp (stock reservation, order creation)
- **Response Type**: Array orders (một đơn mỗi shop)

---

### 4.2.3.5.3.2. Payment Gateway Integration

#### a) **VNPAY**

```typescript
createVNPayPaymentUrl: async (orderCode: string): Promise<PaymentUrlResponse> => {
  return apiCall<PaymentUrlResponse>(api.post(`/api/v1/payment/vnpay/create`, {
    orderCode
  }, { timeout: 30000 }));
}
```

**Flow:**
1. Frontend gọi API tạo payment URL
2. Backend tạo payment request với VNPAY
3. Backend trả về payment URL
4. Frontend redirect: `window.location.href = paymentUrl`
5. User thanh toán trên VNPAY
6. VNPAY redirect về `/checkout/vnpay-return`

#### b) **MoMo**

```typescript
createMomoPaymentUrl: async (orderCode: string): Promise<PaymentUrlResponse> => {
  return apiCall<PaymentUrlResponse>(api.post(`/api/v1/payment/momo/create`, {
    orderCode
  }, { timeout: 30000 }));
}
```

**Flow:** Tương tự VNPAY

#### c) **COD (Cash on Delivery)**

- Không cần tạo payment URL
- Đơn hàng được tạo với status `PENDING`
- Thanh toán khi nhận hàng

---

### 4.2.3.5.3.3. Coupon Validation

**Backend API:**
```
POST /api/v1/coupons/validate
Body:
{
  "couponCode": "SAVE10",
  "orderTotal": 1000000,
  "productIds": [1, 2, 3]
}
Response:
{
  "valid": true,
  "message": "Coupon applied successfully",
  "discountAmount": 100000,
  "couponCode": "SAVE10",
  "couponName": "Giảm 10%"
}
```

**Frontend Service:**

```typescript
validateCoupon: async (request: ValidateCouponRequest): Promise<CouponValidationResponse> => {
  return apiCall<CouponValidationResponse>(api.post("/api/v1/coupons/validate", request));
}
```

**Giải thích:**
- **Pre-validation**: Validate trước khi checkout để hiển thị discount
- **Context-aware**: Gửi kèm orderTotal và productIds để validate điều kiện

---

## 4.2.3.5.4. SECURITY & BEST PRACTICES

### 4.2.3.5.4.1. Idempotency

**Vấn đề:** User có thể click "Đặt hàng" nhiều lần, tạo đơn trùng lặp.

**Giải pháp:**
```tsx
const [idempotencyKey] = useState(uuidv4())

// Sử dụng trong checkout
const orders = await OrderService.checkout(orderPayload, idempotencyKey)
```

**Giải thích:**
- **UUID**: Tạo unique key cho mỗi checkout session
- **Backend Check**: Backend kiểm tra key, nếu đã tồn tại thì trả về đơn cũ
- **Prevent Duplicate**: Đảm bảo mỗi lần checkout chỉ tạo một lần

---

### 4.2.3.5.4.2. Input Validation

```tsx
// Validate address form
if (!newAddress.receiverName || !newAddress.receiverPhone || !newAddress.line1) {
  toast.error("Vui lòng điền đầy đủ thông tin địa chỉ")
  return
}

// Validate coupon code
if (!couponCode.trim()) {
  toast.error("Vui lòng nhập mã giảm giá")
  return
}
```

**Giải thích:**
- **Client-side Validation**: Validate trước khi gọi API
- **User Feedback**: Hiển thị lỗi rõ ràng
- **Prevent Invalid Requests**: Giảm số lượng request không hợp lệ

---

### 4.2.3.5.4.3. Loading States

```tsx
const [loading, setLoading] = useState(false)
const [validatingCoupon, setValidatingCoupon] = useState(false)

// Disable button khi đang xử lý
<Button onClick={handlePlaceOrder} disabled={loading}>
  {loading ? <Loader2 className="animate-spin" /> : "Đặt hàng ngay"}
</Button>
```

**Giải thích:**
- **Prevent Double Submit**: Disable button khi đang xử lý
- **Visual Feedback**: Hiển thị spinner để user biết đang xử lý
- **UX Improvement**: Cải thiện trải nghiệm người dùng

---

### 4.2.3.5.4.4. Error Handling

```tsx
try {
  // ... API calls
} catch (error: any) {
  console.error("Order failed", error)
  toast.error(error.response?.data?.message || "Đặt hàng thất bại")
} finally {
  setLoading(false)
}
```

**Giải thích:**
- **Try-Catch**: Bắt mọi lỗi có thể xảy ra
- **Error Logging**: Log để debug
- **User-friendly Messages**: Hiển thị message từ API hoặc message mặc định
- **Cleanup**: Đảm bảo state được reset trong finally

---

## 4.2.3.5.5. RESPONSIVE DESIGN

### 4.2.3.5.5.1. Grid Layout

```tsx
<div className="grid lg:grid-cols-12 gap-6">
  <div className="lg:col-span-8">
    {/* Main content */}
  </div>
  <div className="lg:col-span-4">
    {/* Sidebar */}
  </div>
</div>
```

**Giải thích:**
- **Desktop (lg)**: 2 cột (8/12 và 4/12)
- **Mobile**: Stack vertically (full width)

---

### 4.2.3.5.5.2. Sticky Sidebar

```tsx
<div className="lg:col-span-4 sticky top-24">
  {/* Order summary */}
</div>
```

**Giải thích:**
- **Sticky Position**: Sidebar dính khi scroll trên desktop
- **Top Offset**: `top-24` để tránh header

---

### 4.2.3.5.5.3. Form Layout

```tsx
<div className="grid grid-cols-2 gap-4">
  <Input placeholder="Họ và tên" />
  <Input placeholder="Số điện thoại" />
</div>
```

**Giải thích:**
- **Responsive Grid**: 2 cột trên desktop, 1 cột trên mobile
- **Gap Spacing**: Khoảng cách đều giữa các input

---

## 4.2.3.5.6. PERFORMANCE OPTIMIZATION

### 4.2.3.5.6.1. Lazy Loading

- **Cart Data**: Đã được load từ Context, không cần fetch lại
- **Addresses**: Load một lần khi component mount

---

### 4.2.3.5.6.2. Memoization

```tsx
const formatPrice = (price: number) => {
  return price.toLocaleString('vi-VN') + '₫';
}
```

**Giải thích:**
- **Helper Function**: Tách logic format để tái sử dụng
- **No Memoization Needed**: Function đơn giản, không cần useMemo

---

### 4.2.3.5.6.3. Optimistic Updates

- **Cart Clear**: Xóa giỏ hàng ngay sau khi đặt hàng thành công
- **Address Selection**: Update state ngay khi user chọn

---

## 4.2.3.5.7. ACCESSIBILITY

### 4.2.3.5.7.1. Form Labels

```tsx
<Label htmlFor="receiverName">Họ và tên</Label>
<Input id="receiverName" />
```

**Giải thích:**
- **Label Association**: Label liên kết với input qua `htmlFor` và `id`
- **Screen Reader**: Screen reader có thể đọc label khi focus input

---

### 4.2.3.5.7.2. Keyboard Navigation

```tsx
<Input
  onKeyDown={(e) => e.key === "Enter" && handleApplyCoupon()}
/>
```

**Giải thích:**
- **Enter Key**: Submit form khi nhấn Enter
- **Tab Navigation**: Tab qua các input theo thứ tự

---

### 4.2.3.5.7.3. ARIA Attributes

```tsx
<Button disabled={loading} aria-busy={loading}>
  {loading ? "Đang xử lý..." : "Đặt hàng ngay"}
</Button>
```

**Giải thích:**
- **aria-busy**: Thông báo cho screen reader biết đang xử lý
- **Disabled State**: Disable button khi đang xử lý

---

## 4.2.3.5.8. TESTING CONSIDERATIONS

### 4.2.3.5.8.1. Unit Tests

- Test validation logic
- Test price calculation
- Test coupon application

---

### 4.2.3.5.8.2. Integration Tests

- Test checkout flow với mock API
- Test payment gateway redirect
- Test error handling

---

### 4.2.3.5.8.3. E2E Tests

- Test full checkout flow từ cart đến payment
- Test với các payment methods khác nhau
- Test với coupon và không có coupon

---

## 4.2.3.5.9. TỔNG KẾT

Trang Checkout được thiết kế với các đặc điểm:

✅ **Component Structure**: Client component với React Hooks
✅ **State Management**: Local state + Context API
✅ **Data Flow**: Load addresses, use cart from context
✅ **Price Calculation**: Dynamic calculation với shipping và discount
✅ **Coupon Integration**: Validate và apply coupon
✅ **Payment Gateway**: Redirect đến VNPAY/MoMo hoặc COD
✅ **Error Handling**: Comprehensive error handling với user feedback
✅ **Security**: Idempotency key, input validation
✅ **Responsive**: Mobile-first design với grid layout
✅ **Accessibility**: Labels, keyboard navigation, ARIA attributes

Trang Checkout đảm bảo trải nghiệm người dùng mượt mà, an toàn và dễ sử dụng.
