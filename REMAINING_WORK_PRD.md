## PRD vs Backend Implementation – Remaining Work

This document maps the original Product Requirements Document (PRD) to the current backend implementation and highlights what is still pending.

---

### 1. User Management

- **1.1 Registration**
  - **Implemented**: Email + password registration via `/auth/register` with validation, duplicate-email checks, password hashing (BCrypt), and JWT issuance.
  - **Missing**: Social login (Google/Facebook/etc.) flows, including OAuth2 configuration and dedicated callbacks.

- **1.2 Login**
  - **Implemented**: Secure login via `/auth/login` (Spring Security + JWT). Invalid credentials are handled with proper errors.

- **1.3 Profile Management**
  - **Implemented**: View and update profile via `/auth/me` (GET) and `/auth/profile` (PUT), returning/accepting profile DTOs.

- **1.4 Password Reset**
  - **Missing**:
    - Endpoint to request password reset (e.g. `/auth/forgot-password`) that issues a time-limited token.
    - Endpoint to actually reset the password with that token (e.g. `/auth/reset-password`).
    - Optional email integration to send the reset link.

---

### 2. Product Catalog

- **2.1 Browsing by Category**
  - **Partially implemented**:
    - Backend has `Category` entity and repository methods to fetch products by category.
  - **Missing**:
    - Public REST endpoints to browse/filter products by category (e.g. `/products?category=Electronics` or `/categories/{name}/products`).

- **2.2 Product Details**
  - **Implemented**: `/products/{id}` returns detailed product information (name, description, image URL, price, category).

- **2.3 Search**
  - **Implemented**: `/products/search?q=...` with case-insensitive search on name and description using `lower()` in the repository query.

---

### 3. Cart & Checkout

- **3.1 Add to Cart**
  - **Implemented**:
    - `POST /cart/items` to add products with quantity.
    - Uses authenticated user (from JWT) and cart/cart-item entities.

- **3.2 Cart Review**
  - **Implemented**:
    - `GET /cart` to view cart contents, quantities, subtotals, and totals.
    - `PUT /cart/items/{itemId}` and `DELETE /cart/items/{itemId}` to update/remove items.
    - `DELETE /cart` to clear the cart.

- **3.3 Checkout**
  - **Implemented**:
    - `POST /orders` converts the authenticated user’s cart into an order and accepts a delivery address.
    - `POST /payments` records a payment for a specific order using a selected payment method.
  - **Notes**: Checkout is modeled as two steps (order creation + payment) rather than a single “checkout” endpoint, but functionally satisfies the PRD.

---

### 4. Order Management

- **4.1 Order Confirmation**
  - **Implemented**: `POST /orders` returns an `OrderResponseDto` with full order details (acts as confirmation).

- **4.2 Order History**
  - **Implemented**: `GET /orders` returns all orders for the authenticated user.

- **4.3 Order Tracking**
  - **Partially implemented**:
    - Orders include an `OrderStatus` field (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`) and are exposed through order responses.
    - `GET /orders/{orderId}` effectively lets the user see current status.
  - **Missing**:
    - Admin or internal endpoints to update order status over time (e.g. `/orders/{id}/status` with PATCH).
    - Any additional tracking metadata (timestamps per status, tracking numbers) if required by the product.

---

### 5. Payment

- **5.1 Multiple Payment Options**
  - **Implemented**:
    - `PaymentMethod` enum (`CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `NET_BANKING`, `COD`).
    - `POST /payments` accepts a method and associates it with an order.

- **5.2 Secure Transactions**
  - **Implemented (backend-side)**:
    - All payment endpoints are JWT-protected.
    - `amount` is derived from `order.totalAmount` on the server, not from client input.
    - Payment records store method, status, transaction ID, and payment date.
  - **Missing (if PRD expects real gateway)**:
    - Integration with an actual payment provider (Stripe/Razorpay/etc.), including API calls, callbacks/webhooks, and mapping provider statuses to `PaymentStatus`.

- **5.3 Payment Receipt**
  - **Implemented**:
    - `PaymentResponseDto` (returned from `/payments` and `/payments/{id}`) includes payment ID, order ID, amount, method, status, transaction ID, and date – this serves as a payment receipt.

---

### 6. Authentication & Session Management

- **6.1 Secure Authentication**
  - **Implemented**:
    - Spring Security with JWT-based authentication and BCrypt password hashing.
    - Public endpoints: `/auth/register`, `/auth/login`, product reads; all other protected endpoints require a valid JWT.

- **6.2 Session Management**
  - **Implemented**:
    - Stateless sessions using JWT with an expiration window configured via `jwt.expiration`.
    - Users remain logged in until the token expires or the client discards it.
  - **Missing / Optional Enhancements**:
    - Explicit logout/token invalidation (e.g. blacklist or token revocation).
    - Refresh-token flow for rotating short-lived access tokens.

---

### Summary of Remaining Work (Strictly vs PRD)

- **Must-have gaps (directly from PRD wording)**:
  - Add **password reset** flow (request reset + token-based reset + secure password update).
  - Implement **social login** using OAuth2 providers if “social media profiles” is a hard requirement.
  - Expose **category-based product browsing** endpoints using the existing category infrastructure.

- **Recommended enhancements (for a production-quality system)**:
  - Admin/internal APIs to **update order status** and support richer tracking.
  - Integrate with a **real payment gateway** instead of the current mocked `SUCCESS`.
  - Add **logout** and optionally **refresh tokens** for better session handling.


