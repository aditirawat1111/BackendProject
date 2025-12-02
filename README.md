# Backend Capstone Project

A Spring Boot-based **e-commerce backend** that provides end-to-end APIs for user management, product catalog, shopping cart, orders, and payments, secured with JWT-based authentication.

---

## Technology Stack

- **Java 17**
- **Spring Boot 3.4.5**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security (JWT, BCrypt)**
- **MySQL Database**
- **Flyway** (database migrations)
- **Lombok**
- **Maven**

---

## High-Level Features

### 1. User Management & Authentication
- ✅ User registration (`/auth/register`) with email uniqueness and password hashing (BCrypt).
- ✅ User login (`/auth/login`) with JWT token generation.
- ✅ Profile management:
  - `GET /auth/me` – view current user profile.
  - `PUT /auth/profile` – update profile details.
- ✅ Password reset flow:
  - `POST /auth/forgot-password` – request reset token.
  - `POST /auth/reset-password` – reset password using token.
- ✅ Centralized exception handling and validation for all auth flows.

### 2. Product Catalog
- ✅ Product CRUD:
  - Get product by ID.
  - Get all products.
  - Create, update (PUT), and partial update (PATCH) products.
- ✅ Category-based product listing:
  - `GET /products/by-category?category={name}`.
- ✅ Search:
  - `GET /products/search?q={keyword}` – case-insensitive search on name & description.
- ✅ Pagination, filtering, and sorting:
  - `page`, `size`, `sort` query params on `/products`, `/products/search`, and `/products/by-category`.
  - Combined search + category filters via `/products?category=...&q=...`.

### 3. Shopping Cart
- ✅ Authenticated, user-specific cart:
  - `GET /cart` – view cart with items, quantities, and totals.
  - `POST /cart/items` – add item to cart.
  - `PUT /cart/items/{itemId}` – update quantity.
  - `DELETE /cart/items/{itemId}` – remove item.
  - `DELETE /cart` – clear cart.

### 4. Orders
- ✅ Create order from cart:
  - `POST /orders` – converts current user’s cart into an order with delivery address.
- ✅ Order history:
  - `GET /orders` – list all orders for the authenticated user.
- ✅ Order details & tracking:
  - `GET /orders/{orderId}` – full order details including `OrderStatus`.
- ✅ Admin status updates:
  - `PATCH /orders/{orderId}/status?status={PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED}` (admin only).
- ✅ Pagination, filtering, and sorting:
  - `page`, `size`, `sort`, and `status` query params on `/orders`.

### 5. Payments
- ✅ Payment model linked to orders:
  - `POST /payments` – create payment for an order (amount derived from order).
  - `GET /payments/{paymentId}` – view payment details (acts as receipt).
- ✅ Multiple payment methods via `PaymentMethod` enum:
  - `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `NET_BANKING`, `COD`.
- ✅ Payment security:
  - All payment endpoints are JWT-protected.
  - `PaymentStatus` tracks payment lifecycle.

### 6. Infrastructure & Cross-Cutting Concerns
- ✅ MySQL + Flyway migrations for schema management (`category`, `product`, `user`, `cart`, `cart_item`, `order`, `order_item`, `payment`, `password_reset_token`).
- ✅ Layered architecture (Controller → Service → Repository → Database).
- ✅ Global exception handling with standardized error responses.
- ✅ DTO-based request/response mapping.

---

## Project Structure (Overview)

```text
src/main/java/com/aditi/backendcapstoneproject/
├── BackendCapstoneProjectApplication.java  # Spring Boot entry point
├── component/                              # Utility components (e.g., DbConnectionChecker)
├── config/                                 # Security, JWT filter, RestTemplate config
├── controller/                             # REST controllers (Auth, Product, Cart, Order, Payment)
├── dto/                                    # Request/Response DTOs
├── exception/                              # Custom exceptions & GlobalExceptionHandler
├── model/                                  # JPA entities (User, Product, Category, Cart, Order, Payment, etc.)
├── repository/                             # Spring Data JPA repositories
└── service/                                # Business services (Authentication, Product, Cart, Order, Payment, JWT)
```

For a detailed breakdown of each package and class, see `PROJECT_STRUCTURE.md`.

---

## Security

- **Authentication**: JWT-based, stateless sessions.
- **Password hashing**: BCrypt via Spring Security.
- **Public endpoints** (no JWT required):
  - `POST /auth/register`
  - `POST /auth/login`
  - `GET /products`
  - `GET /products/{id}`
  - `GET /products/search`
  - `GET /products/by-category`
- **Protected endpoints**: All cart, order, payment, profile, and most other APIs (require `Authorization: Bearer <token>`).
- **Admin-only**:
  - `PATCH /orders/{orderId}/status`.

---

## API Endpoint Summary

### Authentication & Profile
| Method | Endpoint                | Description                  | Auth |
|--------|-------------------------|------------------------------|------|
| POST   | `/auth/register`        | Register new user           | No   |
| POST   | `/auth/login`           | Login and get JWT           | No   |
| POST   | `/auth/forgot-password` | Request password reset      | No   |
| POST   | `/auth/reset-password`  | Reset password with token   | No   |
| GET    | `/auth/me`              | Get current user profile    | Yes  |
| PUT    | `/auth/profile`         | Update profile              | Yes  |

### Products
| Method | Endpoint                 | Description                                      | Auth |
|--------|--------------------------|--------------------------------------------------|------|
| GET    | `/products`              | List products (with pagination/filter/sort)      | No   |
| GET    | `/products/{id}`         | Get product by ID                                | No   |
| GET    | `/products/search`       | Search products (`q`, `page`, `size`, `sort`)   | No   |
| GET    | `/products/by-category`  | Products by category (with pagination/sort)     | No   |
| POST   | `/products/`             | Create product                                   | Yes* |
| PUT    | `/products/{id}`         | Update product                                   | Yes* |
| PATCH  | `/products/{id}`         | Partially update product                         | Yes* |

> \*Currently configurable via security rules; recommended to keep product writes protected (e.g., admin-only).

### Cart
| Method | Endpoint                     | Description                  | Auth |
|--------|------------------------------|------------------------------|------|
| GET    | `/cart`                      | Get current user cart        | Yes  |
| POST   | `/cart/items`               | Add item to cart             | Yes  |
| PUT    | `/cart/items/{itemId}`      | Update item quantity         | Yes  |
| DELETE | `/cart/items/{itemId}`      | Remove item from cart        | Yes  |
| DELETE | `/cart`                      | Clear cart                   | Yes  |

### Orders
| Method | Endpoint                    | Description                                         | Auth |
|--------|-----------------------------|-----------------------------------------------------|------|
| POST   | `/orders`                   | Create order from cart                             | Yes  |
| GET    | `/orders`                   | Paginated user order history (with status filter)  | Yes  |
| GET    | `/orders/{orderId}`         | Get order details                                  | Yes  |
| PATCH  | `/orders/{orderId}/status`  | Update order status (admin)                        | Yes  |

### Payments
| Method | Endpoint                     | Description                    | Auth |
|--------|------------------------------|--------------------------------|------|
| POST   | `/payments`                  | Create payment for an order   | Yes  |
| GET    | `/payments/{paymentId}`      | Get payment details/receipt   | Yes  |

---

## Setup & Running

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Database Setup

```sql
CREATE DATABASE CapstoneBackendProject;
CREATE USER 'CapstoneBackendProject_user'@'localhost' IDENTIFIED BY 'aditimysql@11';
GRANT ALL PRIVILEGES ON CapstoneBackendProject.* TO 'CapstoneBackendProject_user'@'localhost';
FLUSH PRIVILEGES;
```

Update `application.properties` if you change database name/user/password.

### Run the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean install
java -jar target/backend-capstone-project-0.0.1-SNAPSHOT.jar
```

By default, the app runs on `http://localhost:8080`.

---

## Testing

Run all tests:

```bash
mvn test
```

---

## Current Progress

- ✅ Project setup with Spring Boot 3.4.5.
- ✅ MySQL integration with Flyway migrations.
- ✅ Full product catalog (CRUD, search, category browsing).
- ✅ Pagination, filtering, and sorting for product listing.
- ✅ User registration, login, JWT authentication, and profile management.
- ✅ Shopping cart (user-specific, JWT-protected).
- ✅ Order creation, history, and status tracking (with admin updates).
- ✅ Pagination, filtering, and sorting for order listing.
- ✅ Payment model and basic payment recording with receipt-style responses.
- ✅ Centralized exception handling and validation.

For more implementation details, see:
- `PROJECT_STRUCTURE.md` – current package-level breakdown.
- `PAGINATION_FILTER_SORT_EXPLANATION.md` – deep dive into pagination/filter/sort behavior.

---

## Remaining / Future Enhancements

- [ ] Social login (Google, etc.) using OAuth2.
- [ ] Integration with a real payment gateway (Stripe/Razorpay/etc.) instead of mocked success.
- [ ] Logout / token revocation and refresh token flow.
- [ ] API documentation with Swagger/OpenAPI.
- [ ] Soft delete and audit improvements.
- [ ] Additional logging, metrics, and monitoring.
- [ ] Comprehensive unit and integration test coverage.

---

*This README reflects the current implemented backend as of the latest development updates.*
