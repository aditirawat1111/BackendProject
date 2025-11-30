# Implementation Plan - Ecommerce Backend

## Current Status vs PRD Requirements

### ✅ Completed Features

#### Product Catalog (Partially Complete)
- ✅ Product browsing by category (via repository queries)
- ✅ Product details endpoint
- ⚠️ **Missing**: Product search functionality

---

### ❌ Not Implemented - PRD Requirements

## Phase 1: Foundation - User Management & Authentication (HIGH PRIORITY)

### 1.1 User Management
**Status**: ❌ Not Started  
**Dependencies**: None  
**Estimated Complexity**: Medium

**Requirements**:
- User Registration (email-based)
- User Login (JWT-based authentication)
- Profile Management (view/update)
- Password Reset (email token-based)

**Entities Needed**:
- `User` model (email, password, name, phone, address, etc.)
- `Role` model (optional - for admin/user distinction)

**API Endpoints**:
```
POST   /auth/register       - Register new user
POST   /auth/login          - User login
POST   /auth/logout         - User logout
GET    /auth/me             - Get current user profile
PUT    /auth/profile        - Update profile
POST   /auth/password/reset - Request password reset
POST   /auth/password/reset/{token} - Reset password with token
```

**Technologies to Add**:
- Spring Security
- JWT (JSON Web Tokens) - `io.jsonwebtoken:jjwt`
- BCrypt for password hashing
- Email service (JavaMailSender) for password reset

**Database Changes**:
- New `user` table
- New `role` table (optional)
- New `password_reset_token` table (optional, or store in user table)

---

### 1.2 Search Functionality
**Status**: ❌ Not Started  
**Dependencies**: Product catalog (already exists)  
**Estimated Complexity**: Low

**Requirements**:
- Search products by keywords (name, description)

**API Endpoints**:
```
GET    /products/search?q={keyword} - Search products
```

**Implementation**:
- Add search method to `ProductRepository`
- Update `ProductService` interface and implementation
- Add endpoint to `ProductController`

---

## Phase 2: Shopping Cart (HIGH PRIORITY)

### 2.1 Cart Management
**Status**: ❌ Not Started  
**Dependencies**: User Management, Product Catalog  
**Estimated Complexity**: Medium

**Requirements**:
- Add product to cart
- View cart items
- Update cart item quantity
- Remove item from cart
- Clear cart

**Entities Needed**:
- `Cart` model (linked to User)
- `CartItem` model (product, quantity, cart)

**API Endpoints**:
```
GET    /cart                - Get current user's cart
POST   /cart/items          - Add item to cart
PUT    /cart/items/{itemId} - Update cart item quantity
DELETE /cart/items/{itemId} - Remove item from cart
DELETE /cart                - Clear entire cart
```

**Database Changes**:
- New `cart` table (user_id, created_at, last_modified)
- New `cart_item` table (cart_id, product_id, quantity)

**Security**:
- All cart endpoints should require authentication
- Users can only access their own cart

---

## Phase 3: Order Management (HIGH PRIORITY)

### 3.1 Order Processing
**Status**: ❌ Not Started  
**Dependencies**: Cart, User Management  
**Estimated Complexity**: High

**Requirements**:
- Create order from cart
- Order confirmation
- Order history
- Order tracking (status updates)

**Entities Needed**:
- `Order` model (user, orderDate, status, totalAmount, deliveryAddress)
- `OrderItem` model (order, product, quantity, price)
- `OrderStatus` enum (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)

**API Endpoints**:
```
POST   /orders              - Create order from cart
GET    /orders/{id}         - Get order details
GET    /orders              - Get user's order history
PATCH  /orders/{id}/status  - Update order status (admin only)
GET    /orders/{id}/track   - Track order status
```

**Database Changes**:
- New `order` table (user_id, order_date, status, total_amount, delivery_address, etc.)
- New `order_item` table (order_id, product_id, quantity, price)

**Business Logic**:
- Convert cart to order
- Clear cart after order creation
- Calculate total amount
- Handle inventory (optional: check stock availability)

---

### 3.2 Delivery Address Management
**Status**: ❌ Not Started  
**Dependencies**: User Management  
**Estimated Complexity**: Low

**Requirements**:
- Add delivery address
- View saved addresses
- Update address
- Delete address
- Set default address

**Entities Needed**:
- `Address` model (or embed in User, or separate table)

**API Endpoints**:
```
GET    /addresses           - Get user's addresses
POST   /addresses           - Add new address
PUT    /addresses/{id}      - Update address
DELETE /addresses/{id}      - Delete address
PATCH  /addresses/{id}/default - Set default address
```

---

## Phase 4: Payment Integration (MEDIUM PRIORITY)

### 4.1 Payment Processing
**Status**: ❌ Not Started  
**Dependencies**: Order Management  
**Estimated Complexity**: High

**Requirements**:
- Multiple payment methods support
- Payment receipt generation
- Secure transaction handling

**Entities Needed**:
- `Payment` model (order_id, payment_method, amount, status, transaction_id, payment_date)
- `PaymentMethod` enum (CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, COD)
- `PaymentStatus` enum (PENDING, SUCCESS, FAILED, REFUNDED)

**API Endpoints**:
```
POST   /payments            - Initiate payment
GET    /payments/{id}       - Get payment details
POST   /payments/{id}/verify - Verify payment (webhook handler)
GET    /payments/{id}/receipt - Get payment receipt
```

**Integration Options**:
- Stripe
- Razorpay
- PayPal
- PayU
- Or mock payment gateway for development

**Security Considerations**:
- Never store full card numbers
- Use HTTPS only
- Implement payment gateway webhooks for verification
- Store transaction IDs securely

---

## Recommended Implementation Order

### Sprint 1: User Foundation (Week 1-2)
1. ✅ **User Registration & Login** 
   - Create User entity and repository
   - Implement Spring Security with JWT
   - Registration and login endpoints
   - Password hashing with BCrypt

2. ✅ **Profile Management**
   - Profile view/update endpoints
   - Add authentication middleware

3. ✅ **Product Search**
   - Add search functionality to ProductRepository
   - Create search endpoint

### Sprint 2: Shopping Cart (Week 3)
1. ✅ **Cart Entity & Repository**
   - Create Cart and CartItem entities
   - Database migration
   - Cart repository

2. ✅ **Cart Service & Controller**
   - Add/remove/update cart items
   - View cart with totals
   - Secure endpoints (require authentication)

### Sprint 3: Order Management (Week 4)
1. ✅ **Order Entity & Repository**
   - Create Order and OrderItem entities
   - Database migration
   - Order repository

2. ✅ **Order Service & Controller**
   - Create order from cart
   - Order history
   - Order status tracking

3. ✅ **Address Management**
   - Address CRUD operations

### Sprint 4: Payment Integration (Week 5)
1. ✅ **Payment Entity & Repository**
   - Create Payment entity
   - Payment repository

2. ✅ **Payment Service**
   - Payment gateway integration (or mock)
   - Payment receipt generation
   - Payment verification

### Sprint 5: Password Reset & Enhancements (Week 6)
1. ✅ **Password Reset Flow**
   - Token generation
   - Email service integration
   - Reset password endpoint

2. ✅ **Testing & Refinement**
   - Unit tests
   - Integration tests
   - Bug fixes
   - API documentation

---

## Technical Dependencies to Add

### Maven Dependencies Needed

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Email Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## Next Immediate Steps

1. **Start with User Management** (Most Critical)
   - This is the foundation for everything else
   - Cart, Orders, and Payments all depend on authenticated users

2. **Create User Entity and Migration**
   - Design User table schema
   - Create Flyway migration V2

3. **Set up Spring Security**
   - Configure security filters
   - Implement JWT token generation and validation
   - Create authentication endpoints

4. **Implement Registration & Login**
   - User registration with email validation
   - Login with JWT token response
   - Password encryption

---

## Notes

- All user-related endpoints should be protected with authentication
- Use JWT tokens for stateless authentication
- Implement proper error handling for all new features
- Follow the existing code patterns (DTOs, Services, Repositories)
- Add appropriate exceptions (UserNotFoundException, etc.)
- Update GlobalExceptionHandler for new exceptions
- Consider adding pagination for order history and product listing
- Add validation for all request DTOs

