# Backend Capstone Project

A Spring Boot-based **e-commerce backend** that provides end-to-end APIs for user management, product catalog, shopping cart, orders, and payments, secured with JWT-based authentication.

This project is designed to look and behave like a **real-world production backend**: it uses layered architecture, strong security practices, database migrations, test coverage, and is **deployment-ready to Microsoft Azure App Service**.

---

## Technology Stack

- **Language & Platform**
  - Java 17
  - Spring Boot 3.4.5

- **Core Frameworks**
  - Spring Web (REST APIs)
  - Spring Data JPA (Hibernate)
  - Spring Security (JWT, BCrypt)
  - Bean Validation (Jakarta Validation)

- **Caching & Performance**
  - Spring Cache abstraction
  - Redis (production cache) for hot data (products, carts, profiles, orders, payments, external FakeStore products)
  - Simple in-memory cache for tests (no Redis required for CI)

- **Database & Migrations**
  - MySQL 8.x
  - Flyway (versioned SQL migrations & seed data)
  - H2 (in-memory DB for tests)

- **Security & Tokens**
  - JJWT (JSON Web Token library)
  - BCrypt password hashing

- **Documentation & Tooling**
  - OpenAPI/Swagger (auto-generated API docs & Swagger UI)
  - Maven (build & dependency management)
  - Lombok (boilerplate reduction)

- **Logging & Observability**
  - SLF4J logging facade
  - Logback (Spring Boot default logger)

- **Testing & Quality**
  - JUnit 5 (Jupiter)
  - Mockito
  - AssertJ, Hamcrest, JSONAssert, JsonPath
  - Spring Boot Test, spring-security-test
  - JaCoCo (code coverage)

- **Scheduling & Background Jobs**
  - Spring Scheduling (`@EnableScheduling`)
  - Quartz-backed CRON job for periodic Stripe payment status synchronization

- **Deployment**
  - Azure App Service (live deployment) with external MySQL and cloud-managed Redis-compatible cache
  - 12-factor style configuration using environment variables

---

## Key Highlights

- **Architecture**: Clean layered architecture (Controller → Service → Repository → Database) with DTO mapping and global exception handling.
- **Security**: JWT-based stateless auth, BCrypt passwords, role-based access (USER / ADMIN), and resource-ownership checks.
- **Domain Coverage**: End-to-end e-commerce flows – products, cart, orders, payments, password reset, and order tracking.
- **Data & Migrations**: MySQL with Flyway migrations, seed data, and soft-delete support via a shared `BaseModel`.
- **Quality**: Unit and integration tests with H2, structured logging across layers, and pagination on all heavy read endpoints.
- **Performance & Caching**: Redis-backed caching for read-heavy endpoints with a clear eviction strategy and a test-friendly, in-memory cache profile.
- **API Documentation**: OpenAPI/Swagger-based documentation and interactive Swagger UI for exploring and testing endpoints.
- **Deployment-Ready**: Built and documented for deployment on Azure App Service with production configuration.

### Live Deployment
- **App Service URL:** `https://app-springboot-dev-eastasia-001.azurewebsites.net`
- **Custom Domain:** `https://aditirawat.me`
- **Swagger UI:** `https://aditirawat.me/swagger-ui/index.html`

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
- ✅ Multi-source product data:
  - Primary product store in MySQL (via `ProductDBService`).
  - Optional integration with the public Fake Store API via `FakeStoreProductService` (used for demonstrations and external data).
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
- ✅ Stripe integration:
  - Stripe Java SDK used for payment intent creation and webhook handling.
  - Webhook endpoint at `/payments/stripe/webhook` for asynchronous confirmation.
  - Scheduled Stripe sync job (configurable via `stripe.sync.*` properties) keeps payment state in sync with Stripe.

### 6. Caching & Performance
- ✅ Redis-based caching in production:
  - Caches product lookups, product lists, category queries, search results, user profiles, carts, orders, and payments.
  - Centralized invalidation strategy using `@CacheEvict` to keep cached data consistent after writes.
- ✅ Transparent caching via Spring Cache:
  - `@Cacheable`, `@CacheEvict`, and `@Caching` used at the service layer with meaningful cache names.
- ✅ Test-friendly cache profile:
  - Test profile (`application-test.properties`) switches to `spring.cache.type=simple` (in-memory) and disables Redis auto-configuration so tests never depend on a running Redis instance.

### 7. Infrastructure & Cross-Cutting Concerns
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

## Advanced Capabilities

- **OpenAPI/Swagger Documentation**
  - Auto-generated OpenAPI specification and Swagger UI for all major endpoints.
  - Supports exploring requests/responses, trying out authenticated calls, and onboarding new consumers quickly.

- **Pagination, Filtering & Sorting**
  - Consistent `page`, `size`, `sort`, and filter query parameters across products and orders.
  - Case-insensitive search on product name/description with combined category + keyword filters.

- **Role-Based Access Control**
  - `USER` and `ADMIN` roles embedded in JWT and enforced via Spring Security and method-level annotations.
  - Resource-ownership checks ensure users can only access their own carts, orders, and payments.

- **Soft Delete & Audit-Friendly Model**
  - Shared `BaseModel` providing `id`, timestamps, and `isDeleted` flag for all entities.
  - Designed to support audit trails and soft delete without losing historical records.

- **Logging & Error Handling**
  - SLF4J/Logback-based logging in services, controllers, filters, and exception handlers.
  - Centralized `GlobalExceptionHandler` returning consistent error responses with appropriate HTTP status codes.

- **Scalability & Deployment**
  - Stateless JWT authentication for easy horizontal scaling behind load balancers.
  - Database migrations and seed data managed via Flyway, suitable for CI/CD and multi-environment setups.
  - Redis-backed caching to reduce database load for common read paths in production.

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
  - `POST /payments/stripe/webhook`
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
- (Optional for local production-like runs) Redis 6+ for caching

### Database Setup (Local)

```sql
CREATE DATABASE CapstoneBackendProject;
CREATE USER 'CapstoneBackendProject_user'@'localhost' IDENTIFIED BY 'aditimysql@11';
GRANT ALL PRIVILEGES ON CapstoneBackendProject.* TO 'CapstoneBackendProject_user'@'localhost';
FLUSH PRIVILEGES;
```

Update `application.properties` if you change database name/user/password.

### Redis Setup (Local, Optional but Recommended)

By default, the main profile is configured to use Redis as the cache provider:

- `spring.cache.type=redis`
- `spring.redis.host=${REDIS_HOST:localhost}`
- `spring.redis.port=${REDIS_PORT:6379}`

For a local Redis instance:

```bash
docker run --name redis -p 6379:6379 -d redis:7
```

Or point `REDIS_HOST` / `REDIS_PORT` to your cloud Redis instance (e.g., Azure Cache for Redis).

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

### Deployment (Azure App Service)

- Build: `mvn clean package`
- Deploy the generated JAR to Azure App Service.
- Configure App Settings (environment variables):
  - `SPRING_DATASOURCE_URL` (Azure MySQL JDBC URL)
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET`, `JWT_EXPIRATION` (optional overrides)
  - **Redis (Azure Cache for Redis)** – required for cache hit/miss metrics and Redis-backed caching:
    - **`SPRING_PROFILES_ACTIVE` = `prod`** – enables Redis cache; without this, the app uses in-memory cache and Redis is not used.
    - `REDIS_HOST` = `<your-cache-name>.redis.cache.windows.net`
    - `REDIS_PORT` = `6380` (TLS) *(or `6379` if you explicitly disabled TLS on the cache)*
    - `REDIS_PASSWORD` = Primary or Secondary **access key** from Azure Portal → your Redis resource → **Access keys** (copy with no leading/trailing spaces).
    - **If you see `WRONGPASS invalid username-password pair`**: (1) Re-copy the access key from Azure Portal (no spaces/newlines). (2) Try adding `REDIS_USERNAME` = `default` in App Settings. (3) Regenerate the key in the portal and update `REDIS_PASSWORD`.
    - Optional: `REDIS_USERNAME` = `default` (only if WRONGPASS persists with password-only).
    - **Note**: This project uses TLS (`spring.redis.ssl=true`) and trims host/password; it supports optional username for Redis 6+.
  - `PORT` is provided by Azure; `server.port` already respects it.
- **Verifying Redis / caching without redis-cli**: Azure App Service and Cloud Shell often don’t have `redis-cli`. To confirm Redis is used: (1) In Azure Portal open your **Redis** resource → **Redis Console** (or **Advanced settings** → **Test**) and run `PING`; (2) Call your app’s `GET /products/{id}` twice – the first request may log a Hibernate query and cache PUT, the second should **not** log a Hibernate query (cache hit).
- Custom domain: DNS (A/CNAME + TXT) pointed to App Service with managed certificate; live at `https://aditirawat.me`.
- Swagger UI (live): `https://aditirawat.me/swagger-ui/index.html`

---

## Testing

This project has both **unit tests (services)** and **integration tests (controllers)** using **JUnit 5, Mockito, Spring Boot Test, spring-security-test, H2**, and **JaCoCo** for coverage.

The tests run under a dedicated **`test` profile** that:

- Uses H2 in-memory database (no MySQL required for tests).
- Disables Flyway (schema is generated automatically).
- Switches caching to **simple in-memory cache** (`spring.cache.type=simple`) so **no Redis instance is required**.
- Excludes Redis auto-configuration to avoid accidental connection attempts.

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

*This README reflects the current implemented backend and highlights the core design and engineering decisions behind it.*
