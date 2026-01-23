# 4.2.1. TRANG HOME (TRANG CHỦ)

## 4.2.1.2. MÔ TẢ

### 4.2.1.2.1. Tổng quan

Trang Home là trang chủ của hệ thống thương mại điện tử, được thiết kế với mục tiêu:
- **Giới thiệu sản phẩm nổi bật** và khuyến mãi
- **Hướng dẫn người dùng** khám phá các danh mục sản phẩm
- **Tăng tỷ lệ chuyển đổi** với các section được tối ưu UX
- **Responsive design** hoạt động tốt trên mọi thiết bị

### 4.2.1.2.2. Cấu trúc trang

Trang Home được chia thành các section theo thứ tự từ trên xuống:

1. **Header** - Thanh điều hướng chính
   - Logo và tìm kiếm
   - Giỏ hàng và tài khoản
   - Menu danh mục

2. **Hero Carousel** - Banner quảng cáo chính
   - Carousel tự động chuyển slide mỗi 5 giây
   - 2 banner chính và 4 banner phụ
   - Navigation buttons và dots indicator

3. **Coupon Section** - Mã giảm giá
   - Hiển thị các mã coupon đang hoạt động
   - Auto-apply coupons

4. **Category Section** - Danh mục sản phẩm
   - Grid layout 2 hàng, scroll ngang
   - Hiển thị icon và tên danh mục
   - Click vào danh mục để xem sản phẩm

5. **Deals Section** - Flash Sale
   - Countdown timer (3 giờ một lần)
   - Sản phẩm giảm giá hot nhất
   - Progress bar hiển thị số lượng đã bán
   - Scroll ngang với navigation

6. **Top Search Section** - Tìm kiếm phổ biến
   - Từ khóa tìm kiếm hot nhất
   - Trending searches

7. **Top Deal List Section** - Top Deal Hot Nhất
   - Grid layout 5 cột (desktop)
   - Sản phẩm có discount cao nhất
   - Rating và review count

8. **Recommended Section** - Gợi ý cho bạn
   - Personalized recommendations (nếu đã đăng nhập)
   - Trending products (fallback)
   - Load more functionality

9. **Recently Viewed Section** - Đã xem gần đây
   - Sản phẩm người dùng đã xem
   - Lưu trong localStorage

10. **Footer** - Chân trang
    - Links, thông tin liên hệ
    - Social media

### 4.2.1.2.3. Đặc điểm nổi bật

- **Server-Side Rendering (SSR)**: Trang được render trên server để tối ưu SEO
- **Lazy Loading**: Images và components được load khi cần
- **Progressive Enhancement**: Hoạt động tốt ngay cả khi JavaScript bị tắt
- **Accessibility**: Tuân thủ WCAG guidelines
- **Performance**: Tối ưu với Next.js Image component

---

## 4.2.1.3. KỸ THUẬT THIẾT KẾ

### 4.2.1.3.1. Layout Structure

#### a) **Flexbox Layout**
```tsx
<div className="min-h-screen flex flex-col">
  <Header />
  <main className="flex-1">
    {/* Sections */}
  </main>
  <Footer />
</div>
```

**Giải thích:**
- `min-h-screen`: Đảm bảo trang luôn cao ít nhất bằng viewport
- `flex flex-col`: Layout dọc, Header ở trên, Footer ở dưới
- `flex-1`: Main content chiếm không gian còn lại

#### b) **Container Pattern**
```tsx
<section className="bg-[#f1f2f4] py-2">
  <div className="container mx-auto max-w-[1320px] px-2.5">
    {/* Content */}
  </div>
</section>
```

**Giải thích:**
- `container mx-auto`: Container căn giữa
- `max-w-[1320px]`: Giới hạn chiều rộng tối đa
- `px-2.5`: Padding ngang responsive

### 4.2.1.3.2. Component Architecture

#### a) **Component Composition**
```
Home Page (app/page.tsx)
├── Header (components/header.tsx)
├── HeroCarousel (components/hero-carousel.tsx)
├── CouponSection (components/coupon-section.tsx)
├── CategorySection (components/category-section.tsx)
├── DealsSection (components/deals-section.tsx)
├── TopSearchSection (components/top-search-section.tsx)
├── TopDealListSection (components/top-deal-list-section.tsx)
├── RecommendedSection (components/recommended-section.tsx)
├── RecentlyViewedSection (components/recently-viewed-section.tsx)
└── Footer (components/footer.tsx)
```

#### b) **Reusable Components**
- Card components từ `@/components/ui/card`
- Button từ `@/components/ui/button`
- Badge từ `@/components/ui/badge`
- Image từ `next/image`

### 4.2.1.3.3. Styling Techniques

#### a) **Tailwind CSS Utility Classes**
- **Spacing**: `py-2`, `px-2.5`, `gap-4`, `mb-6`
- **Colors**: `bg-[#f1f2f4]`, `text-[#cb1c22]`
- **Typography**: `text-xl`, `font-bold`, `tracking-tight`
- **Layout**: `grid`, `flex`, `container`
- **Responsive**: `md:`, `lg:`, `xl:` breakpoints

#### b) **Custom CSS Variables**
```css
:root {
  --primary: 358 76% 45%; /* #cb1c22 */
  --background: 0 0% 100%;
  --foreground: 0 0% 20%;
}
```

#### c) **Gradient Backgrounds**
```tsx
<div className="bg-gradient-to-br from-[#f22c1d] via-[#cb1c22] to-[#8f1317]">
  {/* Content */}
</div>
```

### 4.2.1.3.4. Responsive Design

#### a) **Breakpoints**
- **Mobile**: `< 768px` (default)
- **Tablet**: `md: >= 768px`
- **Desktop**: `lg: >= 1024px`
- **Large Desktop**: `xl: >= 1280px`

#### b) **Grid Responsive**
```tsx
<div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-3">
  {/* Products */}
</div>
```

**Giải thích:**
- Mobile: 2 cột
- Tablet: 4 cột
- Desktop: 5 cột

#### c) **Image Responsive**
```tsx
<Image
  src={imageUrl}
  alt="Product"
  fill
  sizes="(max-width: 768px) 50vw, 20vw"
  className="object-contain"
/>
```

### 4.2.1.3.5. Animation & Transitions

#### a) **CSS Transitions**
```tsx
className="transition-all duration-300 hover:shadow-xl"
```

#### b) **Transform Animations**
```tsx
className="group-hover:scale-105 transition-transform duration-500"
```

#### c) **Opacity Transitions**
```tsx
className="opacity-0 group-hover:opacity-100 transition-opacity"
```

### 4.2.1.3.6. Carousel Implementation

#### a) **Auto-play Carousel**
```tsx
useEffect(() => {
  if (!isAutoPlaying) return
  const interval = setInterval(() => {
    setCurrentSlide((prev) => (prev + 1) % slides.length)
  }, 5000)
  return () => clearInterval(interval)
}, [isAutoPlaying])
```

#### b) **Smooth Transitions**
```tsx
className={`absolute inset-0 transition-opacity duration-700 ease-in-out ${
  index === currentSlide ? "opacity-100 z-10" : "opacity-0 z-0"
}`}
```

#### c) **Navigation Controls**
- Previous/Next buttons
- Dot indicators
- Pause on hover

### 4.2.1.3.7. Scrollable Sections

#### a) **Horizontal Scroll**
```tsx
<div
  ref={scrollRef}
  className="flex gap-4 overflow-x-auto scrollbar-hide snap-x"
>
  {/* Items */}
</div>
```

#### b) **Scroll Buttons**
```tsx
<Button
  onClick={() => scroll("left")}
  className="absolute left-0 opacity-0 group-hover:opacity-100"
>
  <ChevronLeft />
</Button>
```

---

## 4.2.1.4. CODE THIẾT KẾ

### 4.2.1.4.1. File chính: `app/page.tsx`

```tsx
import { Header } from "@/components/header";
import { HeroCarousel } from "@/components/hero-carousel";
import { CategorySection } from "@/components/category-section";
import { DealsSection } from "@/components/deals-section";
import { TopSearchSection } from "@/components/top-search-section";
import { TopDealListSection } from "@/components/top-deal-list-section";
import { RecentlyViewedSection } from "@/components/recently-viewed-section";
import { RecommendedSection } from "@/components/recommended-section";
import { CouponSection } from "@/components/coupon-section";
import { Footer } from "@/components/footer";

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        <HeroCarousel />
        <CouponSection />
        <CategorySection />
        <DealsSection />
        <TopSearchSection />
        <TopDealListSection />
        <RecommendedSection />
        <RecentlyViewedSection />
      </main>
      <Footer />
    </div>
  );
}
```

### 4.2.1.4.2. Component HeroCarousel

**File**: `components/hero-carousel.tsx`

**Code chính:**
```tsx
"use client"

import { ChevronLeft, ChevronRight } from "lucide-react"
import { useState, useEffect } from "react"
import Link from "next/link"

const slides = [
  { image: "/images/hero-2.png", link: "/products" },
  { image: "/images/hero-3.png", link: "/products?category=electronics" }
]

export function HeroCarousel() {
  const [currentSlide, setCurrentSlide] = useState(0)
  const [isAutoPlaying, setIsAutoPlaying] = useState(true)

  useEffect(() => {
    if (!isAutoPlaying) return
    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length)
    }, 5000)
    return () => clearInterval(interval)
  }, [isAutoPlaying])

  return (
    <div className="bg-[#f1f2f4] py-2 md:py-4">
      <div className="container mx-auto max-w-[1320px] px-2.5">
        <div className="rounded-2xl overflow-hidden bg-gradient-to-br from-[#f22c1d] via-[#cb1c22] to-[#8f1317] p-3 md:p-4">
          <div className="relative w-full aspect-[2/1] md:aspect-[1320/370] overflow-hidden">
            {slides.map((slide, index) => (
              <Link
                key={index}
                href={slide.link}
                className={`absolute inset-0 transition-opacity duration-700 ${
                  index === currentSlide ? "opacity-100 z-10" : "opacity-0 z-0"
                }`}
              >
                <img src={slide.image} alt="Hero Banner" className="w-full h-full object-cover" />
              </Link>
            ))}
            <button onClick={() => setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length)}>
              <ChevronLeft />
            </button>
            <button onClick={() => setCurrentSlide((prev) => (prev + 1) % slides.length)}>
              <ChevronRight />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
```

### 4.2.1.4.3. Component DealsSection (Flash Sale)

**File**: `components/deals-section.tsx`

**Code chính:**
```tsx
"use client"

import { useState, useEffect, useRef } from "react"
import { ProductService, type ProductEntity } from "@/services/product.service"
import { Clock, ChevronRight, ChevronLeft } from "lucide-react"
import Link from "next/link"
import Image from "next/image"

export function DealsSection() {
  const [deals, setDeals] = useState<ProductEntity[]>([])
  const [timeLeft, setTimeLeft] = useState({ hours: 0, minutes: 0, seconds: 0 })
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    async function fetchDeals() {
      try {
        const data = await ProductService.getTopDeals(10)
        setDeals(data || [])
      } catch (error) {
        console.error("Failed to fetch deals", error)
      }
    }

    const calculateTimeLeft = () => {
      const now = new Date()
      const currentHour = now.getHours()
      const nextSlot = Math.ceil((currentHour + 1) / 3) * 3
      const target = new Date(now)
      target.setHours(nextSlot, 0, 0, 0)
      
      if (target.getTime() < now.getTime()) {
        target.setDate(target.getDate() + 1)
      }

      const diff = target.getTime() - now.getTime()
      if (diff > 0) {
        setTimeLeft({
          hours: Math.floor((diff / (1000 * 60 * 60)) % 24),
          minutes: Math.floor((diff / 1000 / 60) % 60),
          seconds: Math.floor((diff / 1000) % 60)
        })
      }
    }

    fetchDeals()
    calculateTimeLeft()
    const timer = setInterval(calculateTimeLeft, 1000)
    return () => clearInterval(timer)
  }, [])

  return (
    <section className="bg-[#f1f2f4] py-2">
      <div className="container mx-auto max-w-[1320px] px-2.5">
        <div className="bg-white rounded-2xl p-4 md:p-6 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-4">
              <h2 className="text-2xl font-extrabold text-[#cb1c22] uppercase">FLASH SALE</h2>
              <div className="flex gap-2">
                <div className="bg-black text-white px-1.5 py-0.5 text-sm font-bold">
                  {String(timeLeft.hours).padStart(2, '0')}
                </div>
                <div className="bg-black text-white px-1.5 py-0.5 text-sm font-bold">
                  {String(timeLeft.minutes).padStart(2, '0')}
                </div>
                <div className="bg-black text-white px-1.5 py-0.5 text-sm font-bold">
                  {String(timeLeft.seconds).padStart(2, '0')}
                </div>
              </div>
            </div>
          </div>
          
          <div ref={scrollRef} className="flex gap-4 overflow-x-auto scrollbar-hide">
            {deals.map((deal) => (
              <Link key={deal.id} href={`/products/${deal.id}`} className="flex-none w-[220px]">
                <div className="bg-white border rounded-xl p-3">
                  <div className="relative aspect-square mb-3">
                    <Image
                      src={deal.mainImageUrl || "/placeholder.svg"}
                      alt={deal.name}
                      fill
                      className="object-contain"
                    />
                  </div>
                  <h3 className="text-sm font-bold line-clamp-2 mb-2">{deal.name}</h3>
                  <div className="text-lg font-bold">{deal.price.toLocaleString()}đ</div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
```

### 4.2.1.4.4. Hướng dẫn chụp hình code thiết kế

#### **Hình 4.9. Code thiết kế Trang Home**

**Các bước chụp hình:**

1. **Mở VS Code hoặc IDE**
   - Mở file `app/page.tsx`
   - Đảm bảo code được format đẹp

2. **Chụp màn hình code:**
   - Zoom code đến mức dễ đọc (Ctrl +)
   - Chụp toàn bộ file `app/page.tsx`
   - Hoặc chụp từng section quan trọng

3. **Chú thích:**
   - Đánh số các phần: (1) Import components, (2) Main layout, (3) Sections
   - Highlight các phần quan trọng:
     - `min-h-screen flex flex-col` - Layout structure
     - `<Header />` - Header component
     - `<main className="flex-1">` - Main content area
     - Các section components

4. **Chụp thêm:**
   - Component tree trong React DevTools
   - Browser DevTools Elements tab
   - Network tab khi load trang

**Ví dụ chú thích hình:**

```
Hình 4.9. Code thiết kế Trang Home

(1) Import các components cần thiết
(2) Component Home chính sử dụng Server Component (không có "use client")
(3) Layout structure với Flexbox:
    - min-h-screen: Chiều cao tối thiểu bằng viewport
    - flex flex-col: Layout dọc
    - flex-1: Main content chiếm không gian còn lại
(4) Các section được sắp xếp theo thứ tự:
    - HeroCarousel: Banner quảng cáo
    - CouponSection: Mã giảm giá
    - CategorySection: Danh mục
    - DealsSection: Flash Sale
    - TopDealListSection: Top deals
    - RecommendedSection: Gợi ý
    - RecentlyViewedSection: Đã xem
```

---

## 4.2.1.5. KỸ THUẬT LẬP TRÌNH XỬ LÝ

### 4.2.1.5.1. Data Fetching

#### a) **Server-Side Rendering (SSR)**
```tsx
// app/page.tsx - Server Component (default)
export default function Home() {
  // Không có "use client" - render trên server
  return <div>...</div>
}
```

**Ưu điểm:**
- SEO tốt
- Initial load nhanh
- Không cần JavaScript để hiển thị

#### b) **Client-Side Data Fetching**
```tsx
// components/deals-section.tsx
"use client"

import { useState, useEffect } from "react"

export function DealsSection() {
  const [deals, setDeals] = useState<ProductEntity[]>([])

  useEffect(() => {
    async function fetchDeals() {
      try {
        const data = await ProductService.getTopDeals(10)
        setDeals(data || [])
      } catch (error) {
        console.error("Failed to fetch deals", error)
      }
    }
    fetchDeals()
  }, [])

  return <div>...</div>
}
```

**Giải thích:**
- `"use client"`: Đánh dấu component chạy trên client
- `useState`: Quản lý state của deals
- `useEffect`: Fetch data khi component mount
- `ProductService.getTopDeals()`: API call qua Axios

### 4.2.1.5.2. State Management

#### a) **Local State với useState**
```tsx
const [currentSlide, setCurrentSlide] = useState(0)
const [isAutoPlaying, setIsAutoPlaying] = useState(true)
const [deals, setDeals] = useState<ProductEntity[]>([])
```

#### b) **Refs cho DOM Manipulation**
```tsx
const scrollRef = useRef<HTMLDivElement>(null)

const scroll = (direction: "left" | "right") => {
  if (scrollRef.current) {
    scrollRef.current.scrollBy({
      left: direction === "left" ? -600 : 600,
      behavior: "smooth"
    })
  }
}
```

### 4.2.1.5.3. Side Effects với useEffect

#### a) **Auto-play Carousel**
```tsx
useEffect(() => {
  if (!isAutoPlaying) return
  
  const interval = setInterval(() => {
    setCurrentSlide((prev) => (prev + 1) % slides.length)
  }, 5000)
  
  return () => clearInterval(interval) // Cleanup
}, [isAutoPlaying])
```

**Giải thích:**
- Tạo interval mỗi 5 giây để chuyển slide
- Cleanup function xóa interval khi unmount hoặc dependency thay đổi
- Dependency `[isAutoPlaying]` để pause/resume

#### b) **Countdown Timer**
```tsx
useEffect(() => {
  const calculateTimeLeft = () => {
    const now = new Date()
    const nextSlot = Math.ceil((currentHour + 1) / 3) * 3
    const target = new Date(now)
    target.setHours(nextSlot, 0, 0, 0)
    
    const diff = target.getTime() - now.getTime()
    setTimeLeft({
      hours: Math.floor((diff / (1000 * 60 * 60)) % 24),
      minutes: Math.floor((diff / 1000 / 60) % 60),
      seconds: Math.floor((diff / 1000) % 60)
    })
  }

  calculateTimeLeft()
  const timer = setInterval(calculateTimeLeft, 1000)
  
  return () => clearInterval(timer)
}, [])
```

**Giải thích:**
- Tính toán thời gian còn lại đến slot tiếp theo (0h, 3h, 6h, 9h, 12h...)
- Update mỗi giây
- Format: HH:MM:SS

### 4.2.1.5.4. Event Handlers

#### a) **Click Handlers**
```tsx
const nextSlide = () => {
  setCurrentSlide((prev) => (prev + 1) % slides.length)
}

const prevSlide = () => {
  setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length)
}

<button onClick={nextSlide}>Next</button>
<button onClick={prevSlide}>Prev</button>
```

#### b) **Hover Handlers**
```tsx
<div
  onMouseEnter={() => setIsAutoPlaying(false)}
  onMouseLeave={() => setIsAutoPlaying(true)}
>
  {/* Carousel */}
</div>
```

**Giải thích:**
- Pause carousel khi hover
- Resume khi rời chuột

#### c) **Scroll Handlers**
```tsx
const scroll = (direction: "left" | "right") => {
  if (scrollRef.current) {
    scrollRef.current.scrollBy({
      left: direction === "left" ? -600 : 600,
      behavior: "smooth"
    })
  }
}
```

### 4.2.1.5.5. API Integration

#### a) **Service Layer Pattern**
```typescript
// services/product.service.ts
export class ProductService {
  static async getTopDeals(limit: number): Promise<ProductEntity[]> {
    const response = await api.get(`/products/top-deals?limit=${limit}`)
    return response.data
  }

  static async getCategories(): Promise<CategoryEntity[]> {
    const response = await api.get("/categories")
    return response.data
  }
}
```

#### b) **Error Handling**
```tsx
useEffect(() => {
  async function fetchData() {
    try {
      const data = await ProductService.getTopDeals(10)
      setDeals(data || [])
    } catch (error) {
      console.error("Failed to fetch deals", error)
      // Có thể hiển thị toast notification
      toast.error("Không thể tải dữ liệu")
    }
  }
  fetchData()
}, [])
```

### 4.2.1.5.6. Performance Optimization

#### a) **Lazy Loading Images**
```tsx
<Image
  src={product.mainImageUrl}
  alt={product.name}
  fill
  loading="lazy"
  className="object-contain"
/>
```

#### b) **Code Splitting**
- Mỗi component là một file riêng
- Next.js tự động code splitting
- Chỉ load component khi cần

#### c) **Memoization** (nếu cần)
```tsx
import { useMemo } from "react"

const discountedProducts = useMemo(() => {
  return products.filter(p => p.originalPrice && p.originalPrice > p.price)
}, [products])
```

### 4.2.1.5.7. Conditional Rendering

#### a) **Loading State**
```tsx
if (loading) {
  return (
    <div className="animate-pulse">
      <div className="h-64 bg-gray-200 rounded" />
    </div>
  )
}
```

#### b) **Empty State**
```tsx
if (deals.length === 0) {
  return null // Hoặc hiển thị empty state
}
```

#### c) **Conditional Classes**
```tsx
className={`absolute inset-0 transition-opacity ${
  index === currentSlide ? "opacity-100 z-10" : "opacity-0 z-0"
}`}
```

### 4.2.1.5.8. Responsive Logic

#### a) **Window Size Detection** (nếu cần)
```tsx
const [isMobile, setIsMobile] = useState(false)

useEffect(() => {
  const checkMobile = () => {
    setIsMobile(window.innerWidth < 768)
  }
  checkMobile()
  window.addEventListener('resize', checkMobile)
  return () => window.removeEventListener('resize', checkMobile)
}, [])
```

#### b) **CSS-based Responsive** (Preferred)
```tsx
// Sử dụng Tailwind breakpoints thay vì JavaScript
<div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5">
  {/* Content */}
</div>
```

### 4.2.1.5.9. Local Storage

#### a) **Recently Viewed Products**
```tsx
// Lưu sản phẩm đã xem
useEffect(() => {
  const viewed = localStorage.getItem('recentlyViewed')
  if (viewed) {
    setViewedProducts(JSON.parse(viewed))
  }
}, [])

// Lưu khi xem sản phẩm mới
const addToViewed = (product: ProductEntity) => {
  const viewed = JSON.parse(localStorage.getItem('recentlyViewed') || '[]')
  const updated = [product, ...viewed.filter(p => p.id !== product.id)].slice(0, 10)
  localStorage.setItem('recentlyViewed', JSON.stringify(updated))
}
```

### 4.2.1.5.10. Tóm tắt kỹ thuật xử lý

| Kỹ thuật | Mô tả | Ví dụ |
|----------|-------|-------|
| **useState** | Quản lý state | `const [deals, setDeals] = useState([])` |
| **useEffect** | Side effects | Fetch data, timers, subscriptions |
| **useRef** | DOM references | `const scrollRef = useRef<HTMLDivElement>(null)` |
| **Event Handlers** | Xử lý events | `onClick`, `onMouseEnter`, `onScroll` |
| **API Calls** | Data fetching | `ProductService.getTopDeals()` |
| **Error Handling** | Try-catch | Wrapper cho API calls |
| **Conditional Rendering** | Hiển thị có điều kiện | `{loading ? <Skeleton /> : <Content />}` |
| **Local Storage** | Lưu trữ client-side | Recently viewed products |
| **Timers** | setInterval/setTimeout | Countdown, auto-play |
| **Cleanup** | Prevent memory leaks | Return cleanup function trong useEffect |

---

## TÓM TẮT

### Điểm nổi bật của trang Home:

1. **Component-based Architecture**: Mỗi section là một component độc lập
2. **Server-Side Rendering**: Tối ưu SEO và performance
3. **Responsive Design**: Hoạt động tốt trên mọi thiết bị
4. **Performance**: Lazy loading, code splitting, image optimization
5. **User Experience**: Smooth animations, auto-play carousel, countdown timer
6. **Data Management**: API integration với error handling
7. **State Management**: React hooks (useState, useEffect, useRef)

### Checklist cho đồ án:

- [x] Mô tả chi tiết các section
- [x] Kỹ thuật thiết kế (Layout, Components, Styling)
- [x] Code thiết kế với hướng dẫn chụp hình
- [x] Kỹ thuật lập trình xử lý (State, Effects, API, Events)
