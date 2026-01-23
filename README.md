# 🚀 E-Commerce Backend - Advanced Spring Boot System

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A robust, scalable, and feature-rich e-commerce backend platform built with modern architectural patterns. This system supports multi-vendor operations, advanced promotion engines, and a comprehensive recommendation system.

---

## 🌟 Key Features

### 🔐 Authentication & Security
- **JWT-based Security**: Secure stateless authentication with access and refresh tokens.
- **RBAC (Role-Based Access Control)**: Fine-grained permissions for `ADMIN`, `SELLER`, and `CLIENT`.
- **OAuth2 Integration**: Support for Google Social Login.
- **Session Management**: Session rotation and revocation for enhanced security.

### 📦 Product & Catalog
- **Multi-level Categories**: Deeply nested category trees with high-performance querying.
- **Advanced SKU Management**: Variant-level tracking (Color, Size, etc.) with stock control.
- **Multi-Vendor Support**: Product ownership by different shops/sellers.

### 🛒 Commerce Engine
- **Shopping Cart**: Real-time cart management with price validation.
- **Multi-Vendor Order Splitting**: Automatic splitting of orders during checkout based on sellers.
- **Promotion System**: Flexible coupon engine (Percentage, Fixed Amount, Free Shipping) with JSON-based restrictions.
- **Wishlist**: Save for later features with tracking for recommendations.

### 📊 Analytics & Recommendations
- **Behavioral Tracking**: Tracks user events (VIEW, ADD_TO_CART, PURCHASE) in MongoDB.
- **Personalization**: Content-based and collaborative filtering recommendation endpoints.
- **Admin Dashboard**: Comprehensive statistics for sales, inventory, and user growth.

### 📬 Communication & Payments
- **Email System**: Transactional emails for order confirmation and password resets.
- **Notification Center**: In-app notifications for order status updates.
- **VNPAY Integration**: Ready-to-use payment gateway integration for the Vietnamese market.
- **MoMo Integration**: E-wallet payment support including QR code and app redirect.

---

## 🛠 Tech Stack

| Type | Technology |
|------|------------|
| **Core** | Java 21, Spring Boot 3.4+ |
| **Relational DB** | MySQL 8.0 (Primary records, Orders, Users) |
| **NoSQL** | MongoDB (Product Events, Logs) |
| **Caching** | Redis (Performance optimization) |
| **Migration** | Flyway (Database versioning) |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Maven |

---

## 🚀 Getting Started

### Prerequisites
- **JDK 21** or higher
- **MySQL 8.0**
- **MongoDB**
- **Redis**
- **Maven 3.9+**

### 1. Environment Configuration
Create a `.env` file in the root directory and populate it based on `.env.example`:
```bash
cp .env.example .env
# Edit .env with your local credentials
```

### 2. Database Setup
Initialize the database and run migrations manually:
```powershell
./manual-migrate.ps1
```

### 3. Running the Application
Use the provided script for development mode:
```powershell
./run-dev.ps1
```
The server will start at `http://localhost:8080`.

---

## 📖 API Documentation

The project includes a comprehensive testing guide and Swagger documentation:

- **Interactive UI**: `http://localhost:8080/swagger-ui.html`
- **Testing Guide**: [API_TEST_GUIDE.md](docs/API_TEST_GUIDE.md)
- **Feature Specifics**: Explore the `docs/` folder for detailed system designs.

---

## 📂 Project Structure

```text
src/main/java/com/example/ecommerce/ecommerce_backend/
├── api/             # REST Controllers & DTOs
├── application/     # Business logic (Services)
├── domain/          # Core Domain models & Enums
├── infrastructure/  # Persistence (MySQL/Mongo/Redis), Config & External APIs
└── shared/          # Utilities & Constants
```

---

## 🧪 Testing

Run all test suites including integration tests with Testcontainers:
```bash
mvn test
```
To verify test data integrity:
```powershell
# Execute the verification script against MySQL
mysql -u your_user -p ecommerce < docs/verify-test-data.sql
```

---

## 📝 License
Distributed under the MIT License. See `LICENSE` for more information.

**Happy Coding! 🚀**
