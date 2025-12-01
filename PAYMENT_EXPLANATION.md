# Payment Module - Design & Implementation Plan

## 1. Goal

Add a **Payment module** that:
- Records payments against orders
- Tracks payment method and status
- (For now) Mocks the payment process instead of integrating a real gateway

Flow:
```text
User → Cart → Order (already implemented) → Payment (this step)
```

---

## 2. High-Level Design

### 2.1 Entities & Relationships

```text
User (1) ───< (Many) Order (1) ───< (Many) Payment
```

- One **Order** can have **one or more Payments** (e.g., refunds, retries)
- For MVP, we will create **one successful Payment per Order**

We will introduce:
- `Payment` entity
- `PaymentMethod` enum
- `PaymentStatus` enum

---

## 3. Enums (in `enums/`)

### 3.1 PaymentMethod

**File**: `enums/PaymentMethod.java`

```java
public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    NET_BANKING,
    COD
}
```

### 3.2 PaymentStatus

**File**: `enums/PaymentStatus.java`

```java
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
```

**Why enums?**
- Type-safe values
- Avoids magic strings
- Easy to extend and validate

---

## 4. Payment Entity

### 4.1 `Payment` Model

**File**: `model/Payment.java`

Extends `BaseModel` so it inherits:
- `id`, `name` (unused here), `createdAt`, `lastModified`, `isDeleted`

**Fields:**
```java
@Entity
public class Payment extends BaseModel {

    @ManyToOne
    private Order order;              // Which order this payment is for

    private Double amount;           // Amount paid (should match order.totalAmount)

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;    // UPI / CARD / etc.

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;    // PENDING / SUCCESS / FAILED / REFUNDED

    private String transactionId;    // Mocked or from real gateway

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;        // When payment was made
}
```

**Notes:**
- Use `@Enumerated(EnumType.STRING)` for human-readable values in DB
- `transactionId` lets you link to external gateway if needed later

---

## 5. Database Migration

### 5.1 Flyway Script

**File**: `src/main/resources/db/migration/V5__create_payment_table.sql`

**Table: `payment`**
```sql
CREATE TABLE payment
(
    id             BIGINT       NOT NULL,
    name           VARCHAR(255) NULL,
    created_at     datetime     NULL,
    last_modified  datetime     NULL,
    is_deleted     BIT(1)       NOT NULL,
    amount         DOUBLE       NULL,
    method         VARCHAR(50)  NULL,
    status         VARCHAR(50)  NULL,
    transaction_id VARCHAR(255) NULL,
    payment_date   datetime     NULL,
    order_id       BIGINT       NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);
```

**Key Points:**
- `order_id` links payment to a specific order
- `status` and `method` stored as strings (match enums)
- Follows same style as other migrations

---

## 6. Repository

### 6.1 PaymentRepository

**File**: `repository/PaymentRepository.java`

```java
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrder(Order order);

    Optional<Payment> findByOrder_Id(Long orderId);
}
```

**Responsibilities:**
- Find payments for a given order
- Optionally get payment by orderId

---

## 7. DTOs

### 7.1 PaymentRequestDto (input)

**File**: `dto/PaymentRequestDto.java`

```java
public class PaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}
```

**Notes:**
- We do not accept `amount` from client → we trust server’s `order.totalAmount`
- method is enum for validation

### 7.2 PaymentResponseDto (output)

**File**: `dto/PaymentResponseDto.java`

```java
public class PaymentResponseDto {

    private Long paymentId;
    private Long orderId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private Date paymentDate;
}
```

**Notes:**
- Represents what the client sees after making a payment
- Includes transaction id and status

---

## 8. Service Layer: PaymentService

**File**: `service/PaymentService.java`

### 8.1 Responsibilities

1. Create payment for an order
2. Validate that:
   - Order exists
   - Order belongs to current user
   - Order has a positive totalAmount
3. Simulate payment success (for now)
4. Update order status (e.g., PENDING → CONFIRMED)
5. Return PaymentResponseDto

### 8.2 Methods

#### `createPayment(User user, PaymentRequestDto request)`

Flow:
```text
1. Load order by request.orderId
2. Verify order.user == current user
3. Check order.totalAmount > 0
4. Create Payment:
   - amount = order.totalAmount
   - method = request.method
   - status = SUCCESS (mock)
   - transactionId = random UUID
   - paymentDate = now
5. Save Payment
6. Optionally set order.status = CONFIRMED
7. Return PaymentResponseDto
```

#### `getPayment(User user, Long paymentId)`

Flow:
```text
1. Load Payment
2. Ensure payment.order.user == current user
3. Return PaymentResponseDto
```

---

## 9. Controller: PaymentController

**File**: `controller/PaymentController.java`

### 9.1 Endpoints

#### `POST /payments`

**Request Body:**
```json
{
  "orderId": 1,
  "method": "UPI"
}
```

**Behavior:**
- Requires JWT authentication
- Retrieves user from JWT
- Calls `PaymentService.createPayment(user, request)`
- Returns `201 CREATED` with `PaymentResponseDto`

#### `GET /payments/{id}`

**Behavior:**
- Requires JWT
- Ensures payment belongs to current user
- Returns `200 OK` with `PaymentResponseDto`

---

## 10. Exceptions & Error Handling

### 10.1 New Exceptions

- `PaymentNotFoundException` – Payment not found
- Reuse `OrderNotFoundException` for invalid `orderId`

### 10.2 GlobalExceptionHandler Updates

Add:
- `@ExceptionHandler(PaymentNotFoundException.class)` → 404 NOT FOUND
- Optionally, handle invalid payment method or invalid order state

---

## 11. Security

- All payment endpoints require JWT authentication
- User can only pay for **their own orders**
- Amount is derived from server-side `order.totalAmount`
- Method is enum → prevents invalid values

---

## 12. Mock vs Real Gateway

### 12.1 Current Plan: Mock Payments

- No real external API
- Always set `status = SUCCESS` for valid inputs
- Random `transactionId` (e.g., UUID)
- Easy to test and reason about

### 12.2 Future: Real Integration

To integrate a real provider (Stripe, Razorpay, etc.):
- Use provider’s SDK in `PaymentService`
- Redirect / call their APIs
- Handle webhooks to confirm payments
- Map external statuses to `PaymentStatus` enum

---

## 13. Flow Summary

```text
1. User logs in
2. User creates cart and places order (already implemented)
3. User sends POST /payments with orderId + method
4. Backend:
   a. Validates order and user
   b. Creates Payment (mock success)
   c. Updates order status to CONFIRMED
   d. Returns PaymentResponseDto
5. User can GET /payments/{id} to view payment
6. Later: payment history per user or per order
```

---

## 14. Files to Create for Payment Module

1. `enums/PaymentMethod.java`
2. `enums/PaymentStatus.java`
3. `model/Payment.java`
4. `repository/PaymentRepository.java`
5. `dto/PaymentRequestDto.java`
6. `dto/PaymentResponseDto.java`
7. `exception/PaymentNotFoundException.java`
8. `service/PaymentService.java`
9. `controller/PaymentController.java`
10. `db/migration/V5__create_payment_table.sql`
11. Update `GlobalExceptionHandler.java`

---

## 15. Next Steps

1. Implement the payment enums and entity
2. Add the Flyway migration for `payment` table
3. Implement repository, DTOs, service, and controller
4. Wire it into the existing security and order modules
5. Test the flow:
   - Create cart → order → payment

This will complete the core e-commerce flow from **browsing → cart → order → payment**.


