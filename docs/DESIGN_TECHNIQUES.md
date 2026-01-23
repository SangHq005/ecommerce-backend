# 4.1.3. CÁC KỸ THUẬT THIẾT KẾ

## 1. LAYOUT - SỬ DỤNG THẺ `<div>` HAY `<table>`?

### 1.1. Phương pháp Layout

**Dự án sử dụng: `<div>` với Flexbox và Grid Layout**

- **Không sử dụng `<table>`** cho layout chính
- **Sử dụng `<div>`** kết hợp với:
  - **Tailwind CSS** (Utility-first CSS framework)
  - **Flexbox** (display: flex)
  - **CSS Grid** (display: grid)
  - **Next.js App Router** với component-based architecture

### 1.2. Lý do chọn `<div>` thay vì `<table>`

| Tiêu chí | `<div>` | `<table>` |
|----------|---------|-----------|
| Responsive | ✅ Tốt | ❌ Khó |
| SEO | ✅ Tốt | ⚠️ Trung bình |
| Maintainability | ✅ Dễ bảo trì | ❌ Khó bảo trì |
| Performance | ✅ Nhanh | ⚠️ Chậm hơn |
| Modern Standards | ✅ Chuẩn hiện đại | ❌ Lỗi thời |

### 1.3. Ví dụ Layout Structure

```tsx
// app/layout.tsx - Root Layout
<div className="min-h-screen flex flex-col">
  <Header /> {/* Fixed header */}
  <main className="flex-1">
    {children} {/* Dynamic content */}
  </main>
  <Footer /> {/* Fixed footer */}
</div>
```

```tsx
// components/seller-sidebar.tsx - Sidebar Layout
<div className="w-[230px] bg-white border-r border-gray-200 h-[calc(100vh-64px)] fixed left-0 top-16 z-30 flex flex-col">
  <ScrollArea className="flex-1 h-full">
    <div className="py-2 pb-20">
      {/* Menu items */}
    </div>
  </ScrollArea>
</div>
```

### 1.4. Layout Patterns được sử dụng

#### a) **Header Layout** (components/header.tsx)
```tsx
<header className="sticky top-0 z-50 bg-white border-b">
  <div className="container mx-auto px-4">
    <div className="flex items-center justify-between h-16">
      {/* Logo */}
      <div className="flex items-center">...</div>
      {/* Search */}
      <div className="flex-1 max-w-2xl mx-4">...</div>
      {/* Actions */}
      <div className="flex items-center gap-2">...</div>
    </div>
  </div>
</header>
```

#### b) **Grid Layout** (Product listing)
```tsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-4">
  {products.map(product => (
    <ProductCard key={product.id} product={product} />
  ))}
</div>
```

#### c) **Flexbox Layout** (Form layout)
```tsx
<div className="flex flex-col gap-4">
  <div className="flex gap-4">
    <div className="flex-1">...</div>
    <div className="flex-1">...</div>
  </div>
</div>
```

---

## 2. KỸ THUẬT THIẾT KẾ CÁC THÀNH PHẦN

### 2.1. Hình ảnh (Images)

#### a) **Next.js Image Component**
- Sử dụng `next/image` để tối ưu hiệu năng
- Tự động lazy loading
- Responsive images
- WebP format support

**Ví dụ:**
```tsx
import Image from "next/image"

<Image
  src={product.mainImageUrl || "/placeholder.png"}
  alt={product.name}
  width={300}
  height={300}
  className="object-cover rounded-lg"
  loading="lazy"
/>
```

#### b) **Image Upload với Preview**
```tsx
// app/seller/products/new/page.tsx
const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
  const file = e.target.files?.[0]
  if (file) {
    // Validate
    if (!file.type.startsWith('image/')) {
      toast.error("Vui lòng chọn file ảnh hợp lệ")
      return
    }
    // Create preview URL
    const url = URL.createObjectURL(file)
    setCoverImage(url)
    setCoverImageFile(file)
  }
}
```

#### c) **Image Gallery với Carousel**
- Sử dụng **Embla Carousel** (`embla-carousel-react`)
- Hỗ trợ swipe gestures
- Auto-play và navigation controls

**Ví dụ:**
```tsx
import useEmblaCarousel from 'embla-carousel-react'

const [emblaRef] = useEmblaCarousel({ loop: true })
```

### 2.2. Danh sách (Lists)

#### a) **Unordered List với Tailwind**
```tsx
<ul className="space-y-2">
  {items.map(item => (
    <li key={item.id} className="flex items-center gap-2">
      <span>{item.name}</span>
    </li>
  ))}
</ul>
```

#### b) **Product List với Grid**
```tsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
  {products.map(product => (
    <ProductCard key={product.id} product={product} />
  ))}
</div>
```

#### c) **Scrollable List**
```tsx
import { ScrollArea } from "@/components/ui/scroll-area"

<ScrollArea className="h-[400px]">
  <div className="space-y-2">
    {items.map(item => (
      <div key={item.id}>{item.content}</div>
    ))}
  </div>
</ScrollArea>
```

### 2.3. Liên kết (Links)

#### a) **Next.js Link Component**
- Client-side navigation
- Prefetching
- Active state handling

**Ví dụ:**
```tsx
import Link from "next/link"
import { usePathname } from "next/navigation"

const pathname = usePathname()

<Link
  href="/seller/products"
  className={cn(
    "block px-4 py-2",
    pathname === "/seller/products" && "bg-orange-50 text-[#ee4d2d]"
  )}
>
  Sản phẩm
</Link>
```

#### b) **External Links**
```tsx
<a 
  href="https://example.com" 
  target="_blank" 
  rel="noopener noreferrer"
  className="text-blue-600 hover:underline"
>
  Liên kết ngoài
</a>
```

### 2.4. Bảng biểu (Tables)

#### a) **HTML Table với Tailwind Styling**
```tsx
<table className="w-full border-collapse">
  <thead>
    <tr className="bg-gray-50 border-b">
      <th className="px-4 py-3 text-left font-semibold">STT</th>
      <th className="px-4 py-3 text-left font-semibold">Tên sản phẩm</th>
      <th className="px-4 py-3 text-left font-semibold">Giá</th>
      <th className="px-4 py-3 text-left font-semibold">Hành động</th>
    </tr>
  </thead>
  <tbody>
    {products.map((product, index) => (
      <tr key={product.id} className="border-b hover:bg-gray-50">
        <td className="px-4 py-3">{index + 1}</td>
        <td className="px-4 py-3">{product.name}</td>
        <td className="px-4 py-3">{formatCurrency(product.price)}</td>
        <td className="px-4 py-3">
          <Button onClick={() => handleEdit(product.id)}>Sửa</Button>
        </td>
      </tr>
    ))}
  </tbody>
</table>
```

#### b) **Responsive Table với Card View**
```tsx
{/* Desktop: Table */}
<div className="hidden md:block">
  <table>...</table>
</div>

{/* Mobile: Card */}
<div className="md:hidden space-y-4">
  {products.map(product => (
    <Card key={product.id}>
      <CardContent>...</CardContent>
    </Card>
  ))}
</div>
```

### 2.5. Form (Biểu mẫu)

#### a) **React Hook Form với Validation**
```tsx
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"

const schema = z.object({
  name: z.string().min(1, "Tên sản phẩm không được để trống"),
  price: z.number().min(0, "Giá phải lớn hơn 0"),
  email: z.string().email("Email không hợp lệ")
})

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema)
})

<form onSubmit={handleSubmit(onSubmit)}>
  <div>
    <Label>Tên sản phẩm</Label>
    <Input {...register("name")} />
    {errors.name && <span className="text-red-500">{errors.name.message}</span>}
  </div>
</form>
```

#### b) **Form Components từ Radix UI**
- Input, Textarea, Select, Checkbox, Radio
- Tất cả từ `@radix-ui/react-*`
- Styled với Tailwind CSS

**Ví dụ:**
```tsx
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"

<Input type="text" placeholder="Nhập tên..." />
<Textarea placeholder="Mô tả..." rows={5} />
<Select>
  <SelectTrigger>
    <SelectValue placeholder="Chọn danh mục" />
  </SelectTrigger>
  <SelectContent>
    <SelectItem value="1">Điện thoại</SelectItem>
    <SelectItem value="2">Laptop</SelectItem>
  </SelectContent>
</Select>
```

#### c) **File Upload Form**
```tsx
<input
  type="file"
  accept="image/*"
  onChange={handleImageUpload}
  className="hidden"
  id="image-upload"
/>
<label htmlFor="image-upload">
  <Button type="button" variant="outline">
    Chọn ảnh
  </Button>
</label>
```

#### d) **Multi-step Form**
```tsx
const [step, setStep] = useState(1)

{step === 1 && <BasicInfoForm />}
{step === 2 && <ProductDetailsForm />}
{step === 3 && <PricingForm />}

<div className="flex justify-between">
  <Button onClick={() => setStep(step - 1)} disabled={step === 1}>
    Quay lại
  </Button>
  <Button onClick={() => setStep(step + 1)}>
    Tiếp theo
  </Button>
</div>
```

### 2.6. Modal/Dialog

#### a) **Radix UI Dialog**
```tsx
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"

<Dialog open={isOpen} onOpenChange={setIsOpen}>
  <DialogTrigger>Mở dialog</DialogTrigger>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Tiêu đề</DialogTitle>
      <DialogDescription>Mô tả</DialogDescription>
    </DialogHeader>
    {/* Content */}
  </DialogContent>
</Dialog>
```

### 2.7. Dropdown Menu

```tsx
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

<DropdownMenu>
  <DropdownMenuTrigger>
    <Button variant="ghost">Menu</Button>
  </DropdownMenuTrigger>
  <DropdownMenuContent>
    <DropdownMenuItem onClick={handleEdit}>Sửa</DropdownMenuItem>
    <DropdownMenuItem onClick={handleDelete}>Xóa</DropdownMenuItem>
  </DropdownMenuContent>
</DropdownMenu>
```

### 2.8. Tabs

```tsx
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"

<Tabs defaultValue="all">
  <TabsList>
    <TabsTrigger value="all">Tất cả</TabsTrigger>
    <TabsTrigger value="active">Đang hoạt động</TabsTrigger>
    <TabsTrigger value="draft">Bản nháp</TabsTrigger>
  </TabsList>
  <TabsContent value="all">...</TabsContent>
  <TabsContent value="active">...</TabsContent>
</Tabs>
```

---

## 3. KỸ THUẬT KHÁC

### 3.1. JavaScript/TypeScript

#### a) **React Hooks**
- `useState`: Quản lý state
- `useEffect`: Side effects, API calls
- `useContext`: Global state
- `useRef`: DOM references
- `useRouter`: Next.js navigation
- `usePathname`: Current route

**Ví dụ:**
```tsx
const [products, setProducts] = useState<Product[]>([])
const [loading, setLoading] = useState(true)

useEffect(() => {
  const loadProducts = async () => {
    setLoading(true)
    try {
      const data = await ProductService.getProducts()
      setProducts(data)
    } finally {
      setLoading(false)
    }
  }
  loadProducts()
}, [])
```

#### b) **Custom Hooks**
```tsx
// hooks/useAuth.ts
export function useAuth() {
  const [user, setUser] = useState<User | null>(null)
  // ... logic
  return { user, login, logout, isAuthenticated }
}
```

### 3.2. API Calls - Axios

#### a) **Axios Configuration**
```typescript
// lib/axios.ts
import axios from "axios"

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json"
  }
})

// Request interceptor
api.interceptors.request.use(config => {
  const token = localStorage.getItem("token")
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
```

#### b) **Service Layer Pattern**
```typescript
// services/product.service.ts
import api from "@/lib/axios"

export class ProductService {
  static async getProducts(): Promise<ProductEntity[]> {
    const response = await api.get("/products")
    return response.data
  }

  static async getProductById(id: number): Promise<ProductEntity> {
    const response = await api.get(`/products/${id}`)
    return response.data
  }

  static async createProduct(data: CreateProductDto): Promise<ProductEntity> {
    const response = await api.post("/products", data)
    return response.data
  }
}
```

#### c) **API Call với Error Handling**
```typescript
try {
  const products = await ProductService.getProducts()
  setProducts(products)
} catch (error) {
  if (error instanceof ApiError) {
    toast.error(error.message)
  } else {
    toast.error("Đã xảy ra lỗi")
  }
}
```

### 3.3. AJAX/Fetch Requests

#### a) **Real-time Updates với Polling**
```tsx
useEffect(() => {
  const interval = setInterval(async () => {
    const count = await NotificationService.getUnreadCount()
    setUnreadCount(count)
  }, 30000) // Poll every 30 seconds

  return () => clearInterval(interval)
}, [])
```

#### b) **WebSocket cho Chat** (Sử dụng STOMP)
```typescript
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const client = new Client({
  webSocketFactory: () => new SockJS('/ws'),
  onConnect: () => {
    client.subscribe('/topic/chat', (message) => {
      const chatMessage = JSON.parse(message.body)
      // Handle message
    })
  }
})
```

### 3.4. Animations

#### a) **Framer Motion**
```tsx
import { motion } from "framer-motion"

<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  exit={{ opacity: 0, y: -20 }}
  transition={{ duration: 0.3 }}
>
  Content
</motion.div>
```

#### b) **CSS Animations với Tailwind**
```tsx
<div className="animate-fade-in">
  {/* Content */}
</div>
```

```css
@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}
```

### 3.5. State Management

#### a) **React Context API**
```tsx
// lib/cart-context.tsx
const CartContext = createContext<CartContextType | undefined>(undefined)

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([])
  
  const addToCart = (product: Product) => {
    setItems(prev => [...prev, { product, quantity: 1 }])
  }

  return (
    <CartContext.Provider value={{ items, addToCart }}>
      {children}
    </CartContext.Provider>
  )
}
```

#### b) **Local Storage**
```tsx
// Save to localStorage
localStorage.setItem('rememberedAccounts', JSON.stringify(accounts))

// Load from localStorage
const saved = localStorage.getItem('rememberedAccounts')
const accounts = saved ? JSON.parse(saved) : []
```

### 3.6. Third-party Libraries

| Thư viện | Mục đích | Nguồn |
|----------|----------|-------|
| **Next.js** | React framework | Vercel (Open source) |
| **React** | UI library | Meta (Open source) |
| **Tailwind CSS** | CSS framework | Tailwind Labs (Open source) |
| **Radix UI** | Component primitives | Radix UI (Open source) |
| **Axios** | HTTP client | GitHub (Open source) |
| **React Hook Form** | Form handling | GitHub (Open source) |
| **Zod** | Schema validation | GitHub (Open source) |
| **Framer Motion** | Animations | Framer (Open source) |
| **Lucide React** | Icons | Lucide (Open source) |
| **Sonner** | Toast notifications | GitHub (Open source) |
| **Recharts** | Charts | GitHub (Open source) |

**Tất cả đều là mã nguồn mở, không có code tự viết từ đầu cho các chức năng cơ bản.**

---

## 4. EDITABLE REGIONS (VÙNG CÓ THỂ CHỈNH SỬA)

### 4.1. Next.js App Router Structure

Trong Next.js App Router, **không có khái niệm "Editable Region"** như Dreamweaver template. Thay vào đó:

#### a) **Layout Components** (Editable)
- `app/layout.tsx`: Root layout (Header, Footer, Providers)
- `app/seller/layout.tsx`: Seller layout (Sidebar + Content)
- `app/admin/layout.tsx`: Admin layout
- `app/profile/layout.tsx`: Profile layout

**Ví dụ:**
```tsx
// app/seller/layout.tsx
export default function SellerLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex">
      <SellerSidebar /> {/* Fixed sidebar */}
      <main className="flex-1 ml-[230px]">
        {children} {/* Editable content area */}
      </main>
    </div>
  )
}
```

#### b) **Page Components** (Editable)
- Mỗi file `page.tsx` là một trang có thể chỉnh sửa
- `app/page.tsx`: Trang chủ
- `app/seller/products/page.tsx`: Trang quản lý sản phẩm
- `app/products/[slug]/page.tsx`: Trang chi tiết sản phẩm

#### c) **Component Structure**
```
app/
├── layout.tsx          ← Root layout (Header, Footer)
├── page.tsx           ← Homepage (Editable)
├── seller/
│   ├── layout.tsx    ← Seller layout (Sidebar)
│   └── products/
│       └── page.tsx   ← Products page (Editable)
└── components/        ← Reusable components
    ├── header.tsx     ← Header component
    └── footer.tsx     ← Footer component
```

### 4.2. Template Pattern

**Pattern tương tự Editable Region:**

```tsx
// Template structure
<Layout>
  <Header /> {/* Fixed */}
  <Sidebar /> {/* Fixed */}
  <MainContent>
    {children} {/* Editable - changes per page */}
  </MainContent>
  <Footer /> {/* Fixed */}
</Layout>
```

### 4.3. Dynamic Content Areas

#### a) **Server Components** (Default)
- Render trên server
- Không có JavaScript client-side
- Tốt cho SEO

#### b) **Client Components** (`"use client"`)
- Render trên client
- Có thể sử dụng hooks, state, events
- Tương tác người dùng

**Ví dụ:**
```tsx
"use client" // Mark as client component

export default function ProductPage() {
  const [quantity, setQuantity] = useState(1)
  // ... interactive logic
}
```

---

## 5. HƯỚNG DẪN CHỤP HÌNH MINH HỌA

### 5.1. Layout Structure

#### Hình 1: Cấu trúc Layout tổng thể
**Chụp màn hình:**
1. Mở DevTools (F12)
2. Vào tab "Elements" hoặc "Inspector"
3. Highlight các phần:
   - `<header>` - Header component
   - `<main>` - Main content area
   - `<footer>` - Footer component
4. Chụp màn hình và chú thích:
   - "Header: Fixed top, chứa logo, search, cart"
   - "Main: Dynamic content area, thay đổi theo route"
   - "Footer: Fixed bottom, chứa links và thông tin"

**Source code:**
```tsx
// app/layout.tsx
<div className="min-h-screen flex flex-col">
  <Header />
  <main className="flex-1">{children}</main>
  <Footer />
</div>
```

### 5.2. Component Structure

#### Hình 2: Seller Sidebar Layout
**Chụp màn hình:**
1. Vào trang `/seller/products`
2. Highlight sidebar bên trái
3. Chụp và chú thích:
   - "Sidebar: Fixed left, width 230px"
   - "Menu items: Collapsible groups"
   - "Active state: Highlighted với màu #ee4d2d"

**Source code:**
```tsx
// components/seller-sidebar.tsx
<div className="w-[230px] bg-white border-r border-gray-200 h-[calc(100vh-64px)] fixed left-0 top-16 z-30">
  {/* Menu items */}
</div>
```

### 5.3. Form Design

#### Hình 3: Product Form với Validation
**Chụp màn hình:**
1. Vào `/seller/products/new`
2. Chụp form với các field:
   - Input fields
   - Select dropdowns
   - File upload
   - Validation errors (nếu có)
3. Chú thích:
   - "Form sử dụng React Hook Form"
   - "Validation với Zod schema"
   - "Error messages hiển thị dưới mỗi field"

**Source code:**
```tsx
// app/seller/products/new/page.tsx
const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema)
})
```

### 5.4. API Call Flow

#### Hình 4: Network Tab - API Calls
**Chụp màn hình:**
1. Mở DevTools → Network tab
2. Reload trang hoặc thực hiện action (thêm sản phẩm)
3. Chụp các request:
   - GET `/api/products`
   - POST `/api/products`
4. Chú thích:
   - "Axios được sử dụng cho HTTP requests"
   - "Base URL: http://localhost:8080/api"
   - "Headers: Authorization Bearer token"

**Source code:**
```typescript
// services/product.service.ts
static async getProducts(): Promise<ProductEntity[]> {
  const response = await api.get("/products")
  return response.data
}
```

### 5.5. Responsive Design

#### Hình 5: Responsive Breakpoints
**Chụp màn hình:**
1. Mở DevTools → Toggle device toolbar (Ctrl+Shift+M)
2. Chụp ở các kích thước:
   - Mobile (375px)
   - Tablet (768px)
   - Desktop (1920px)
3. Chú thích:
   - "Grid layout thay đổi: 1 cột → 2 cột → 4 cột"
   - "Sidebar ẩn trên mobile, hiện trên desktop"

**Source code:**
```tsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
  {/* Products */}
</div>
```

### 5.6. State Management

#### Hình 6: React DevTools - Component State
**Chụp màn hình:**
1. Cài React DevTools extension
2. Mở React DevTools
3. Chọn component có state
4. Chụp state tree
5. Chú thích:
   - "useState hooks quản lý local state"
   - "Context API cho global state (Cart, Auth)"

**Source code:**
```tsx
const [products, setProducts] = useState<Product[]>([])
const { user } = useAuth() // Context
const { items } = useCart() // Context
```

---

## 6. TÓM TẮT KỸ THUẬT

### 6.1. Layout
- ✅ Sử dụng `<div>` với Flexbox/Grid
- ❌ Không sử dụng `<table>` cho layout
- ✅ Tailwind CSS utility classes
- ✅ Responsive với breakpoints

### 6.2. Components
- ✅ React functional components
- ✅ TypeScript cho type safety
- ✅ Component composition pattern
- ✅ Reusable UI components từ Radix UI

### 6.3. Forms
- ✅ React Hook Form
- ✅ Zod validation
- ✅ Real-time error messages
- ✅ File upload với preview

### 6.4. API & Data
- ✅ Axios cho HTTP requests
- ✅ Service layer pattern
- ✅ Error handling với try-catch
- ✅ Loading states

### 6.5. State Management
- ✅ React Context API
- ✅ Local Storage
- ✅ useState, useEffect hooks

### 6.6. Libraries
- ✅ Tất cả từ nguồn mở (GitHub, npm)
- ✅ Không có code tự viết từ đầu cho chức năng cơ bản
- ✅ Custom code chỉ cho business logic

---

## 7. CHECKLIST CHO ĐỒ ÁN

- [ ] Chụp hình layout structure (Header, Main, Footer)
- [ ] Chụp hình component tree (React DevTools)
- [ ] Chụp hình form với validation
- [ ] Chụp hình API calls (Network tab)
- [ ] Chụp hình responsive design (3 breakpoints)
- [ ] Chụp hình source code của các component chính
- [ ] Ghi chú về các thư viện sử dụng
- [ ] Giải thích về Editable Regions (Layout/Page pattern)

---

**Lưu ý**: Khi chụp hình, đảm bảo:
1. Code rõ ràng, dễ đọc
2. Chú thích đầy đủ
3. Highlight các phần quan trọng
4. Sử dụng số thứ tự cho các hình (Hình 1, Hình 2, ...)
