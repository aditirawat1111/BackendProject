# Backend Capstone Project

A Spring Boot-based **e-commerce backend** that provides end-to-end APIs for user management, product catalog, shopping cart, orders, and payments, secured with JWT-based authentication.

This project is designed to look and behave like a **real-world production backend**: it uses layered architecture, strong security practices, database migrations, test coverage, and is **deployment-ready to AWS**.

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

## Key Highlights (For Interviewers)

- **Architecture**: Clean layered architecture (Controller → Service → Repository → Database) with DTO mapping and global exception handling.
- **Security**: JWT-based stateless auth, BCrypt passwords, role-based access (USER / ADMIN), and resource-ownership checks.
- **Domain Coverage**: End-to-end e-commerce flows – products, cart, orders, payments, password reset, and order tracking.
- **Data & Migrations**: MySQL with Flyway migrations, seed data, and soft-delete support via a shared `BaseModel`.
- **Quality**: Unit and integration tests with H2, structured logging across layers, and pagination on all heavy read endpoints.
- **Deployment-Ready**: Built and documented for deployment on AWS (Elastic Beanstalk / EC2 + RDS) with separate production configuration.

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
- ✅ Comprehensive logging (services, payments, orders, auth, exception handler) using SLF4J/Logback.
- ✅ Soft-delete friendly entity model via a shared `BaseModel` (id, timestamps, `isDeleted`).

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
- **Roles**: `USER` (default) and `ADMIN`, embedded in JWT and enforced via method-level security for admin-only operations.
- **Public endpoints** (no JWT required):
  - `POST /auth/register`
  - `POST /auth/login`
  - `POST /auth/forgot-password`
  - `POST /auth/reset-password`
  - `GET /products`
  - `GET /products/{id}`
  - `GET /products/search`
  - `GET /products/by-category`
  - `GET /categories`
  - API docs (Swagger/OpenAPI) endpoints if enabled (e.g. `/swagger-ui/**`, `/v3/api-docs/**`).
- **Protected endpoints**: All cart, order, payment, profile, and most other APIs (require `Authorization: Bearer <token>`).
- **Admin-only**:
  - `PATCH /orders/{orderId}/status`.

### Demo Admin User (Development / Local Only)

Flyway seed data creates a convenient admin user for local testing:

- **Email**: `admin@example.com`
- **Password**: `Password123!`

Use this account to test admin-only operations (like order status updates). In a real deployment, create secure admin accounts and rotate credentials.

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

### Database Setup (Local)

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

### Deployment (AWS-Friendly)

The project is structured to be easily deployed to **AWS Elastic Beanstalk** or **AWS EC2 + RDS**:

- Build a JAR with `mvn clean package`.
- Use a separate `application-production.properties` (or env variables) for:
  - RDS connection (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`)
  - JWT settings (`jwt.secret`, `jwt.expiration`)
  - Flyway production configuration.
- For Elastic Beanstalk:
  - Upload the JAR, configure environment variables for DB and JWT, and let Beanstalk manage EC2, scaling, and load balancing.
- For EC2:
  - Copy the JAR to the instance and run with  
    `java -jar -Dspring.profiles.active=production backend-capstone-project-0.0.1-SNAPSHOT.jar`.

For a step-by-step, production-grade walkthrough, see `DEPLOYMENT_GUIDE.md`.

---

## Testing

This project has both **unit tests (services)** and **integration tests (controllers)** using **JUnit 5, Mockito, Spring Boot Test, spring-security-test, H2**, and **JaCoCo** for coverage.

### Run All Tests

```bash
mvn clean test
```

### Run with Test Profile (H2, no Flyway on tests)

```bash
mvn clean test -Dspring.profiles.active=test
```

### Run a Specific Test Class

```bash
mvn test -Dtest=ProductDBServiceTest
```

### Generate Coverage Report

```bash
mvn clean test jacoco:report
```

Open `target/site/jacoco/index.html` in a browser to view coverage details.

See `TESTING_SETUP.md` for a full breakdown of test strategy, structure, and annotations.

---

## Current Progress

- ✅ Project setup with Spring Boot 3.4.5.
- ✅ MySQL integration with Flyway migrations.
- ✅ Full product catalog (CRUD, search, category browsing, case-insensitive search).
- ✅ Pagination, filtering, and sorting for product and order listing.
- ✅ User registration, login, JWT authentication, and profile management.
- ✅ Role-based access (USER / ADMIN) with admin-only operations.
- ✅ Shopping cart (user-specific, JWT-protected).
- ✅ Order creation, history, and status tracking (with admin updates).
- ✅ Payment model and payment recording with receipt-style responses and automatic order status update.
- ✅ Password reset flow with token-based reset.
- ✅ Centralized exception handling, validation, and structured logging.

For more implementation details, see:
- `PROJECT_STRUCTURE.md` – package-level breakdown.
- `PROJECT_FLOW.md` – end-to-end flows (auth, cart, orders, payments, security, logging).
- `PAGINATION_FILTER_SORT_EXPLANATION.md` – deep dive into pagination/filter/sort behavior.

---

## Remaining / Future Enhancements

- [ ] Social login (Google, etc.) using OAuth2.
- [ ] Integration with a real payment gateway (Stripe/Razorpay/etc.) instead of mocked success.
- [ ] Logout / token revocation and refresh token flow.
- [ ] Rich API documentation and UI via Swagger/OpenAPI, published in production.
- [ ] Soft delete filtering at repository/query level and audit trail improvements.
- [ ] Centralized, production-grade logging/monitoring (ELK / Prometheus + Grafana).
- [ ] Additional admin-facing endpoints and dashboards (manage users, products, and global orders).
- [ ] Further increase in automated test coverage and performance tests.

---

## How to Explore This Project (Recommended Reading)

- `PROJECT_STRUCTURE.md` – detailed explanation of packages, entities, and repositories.
- `PROJECT_FLOW.md` – complete user journey and architecture analysis (scalability, security, logging).
- `AUTHENTICATION_AUTHORIZATION_EXPLAINED.md` – deep dive into authentication vs authorization decisions.
- `ORDER_MANAGEMENT_EXPLANATION.md`, `SHOPPING_CART_EXPLANATION.md`, `PAYMENT_EXPLANATION.md` – domain-specific design notes.
- `DEPLOYMENT_GUIDE.md` – AWS deployment steps and cloud architecture notes.
- `TESTING_SETUP.md` – test design, tools, and coverage.

---

*This README reflects the current implemented backend as of the latest development updates and is written to highlight design and engineering decisions for reviewers and interviewers.*
