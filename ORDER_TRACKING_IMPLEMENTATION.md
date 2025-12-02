## Step 2 – Order Tracking Implementation

This document explains how **order tracking** is implemented in the backend.

---

### 1. Data Model

- **Entity**: `Order` (`model/Order.java`)
  - `user` – `ManyToOne` relationship to `User`
  - `orderDate` – when the order was placed
  - `status` – `OrderStatus` enum (current state of the order)
  - `totalAmount` – total value of the order
  - `deliveryAddress` – shipping address
  - `orderItems` – list of `OrderItem` entities (products in the order)

- **Enum**: `OrderStatus` (`enums/OrderStatus.java`)
  - Values:
    - `PENDING`
    - `CONFIRMED`
    - `SHIPPED`
    - `DELIVERED`
    - `CANCELLED`

These fields are exposed to clients via `OrderResponseDto`.

---

### 2. Read Endpoints (User-Facing Tracking)

Defined in `OrderController`:

- **`GET /orders`**
  - Returns a list of `OrderResponseDto` for the **authenticated user**.
  - Each item includes:
    - `orderId`
    - `orderDate`
    - `status`
    - `totalAmount`
    - `deliveryAddress`
    - `items` (list of products with quantity/price)
    - `totalItems`
  - **Usage**: Order history + high-level tracking.

- **`GET /orders/{orderId}`**
  - Returns a single `OrderResponseDto` for the given `orderId`, but **only if it belongs to the current user**.
  - **Usage**: Detailed view and up-to-date status for one order.

Together, these endpoints already allow a user to **track the status** of their orders.

---

### 3. Status Update Endpoint (Admin / Internal)

To fully support tracking over the order lifecycle, we added an admin-only endpoint to change order status.

- **Endpoint**
  - **`PATCH /orders/{orderId}/status?status=SHIPPED`**

- **Controller**
  - Implemented in `OrderController`:
    - Annotated with `@PreAuthorize("hasRole('ADMIN')")`  
      → only users with role `ADMIN` can call this endpoint.
    - Accepts `orderId` as a path variable and `status` (an `OrderStatus` enum value) as a request parameter.
    - Returns the updated `OrderResponseDto`.

- **Service Logic**
  - Implemented in `OrderService`:
    - `updateOrderStatus(Long orderId, OrderStatus status)`
      1. Loads the order by `orderId`.
      2. Throws `OrderNotFoundException` if it does not exist.
      3. Updates `order.status` with the new value.
      4. Updates `lastModified` timestamp.
      5. Saves the order and returns a refreshed `OrderResponseDto`.

---

### 4. Typical Status Flow

A recommended status progression for tracking:

```text
PENDING → CONFIRMED → SHIPPED → DELIVERED
    ↓
 CANCELLED (can be reached from any state as needed)
```

Back-office or admin tools would call the **status update endpoint** as the order moves through each stage (e.g., after payment confirmation, packing, shipping, delivery).

---

### 5. How Frontend / Clients Use Tracking

- **For customers**
  - Use `GET /orders` to show an order history page with current statuses.
  - Use `GET /orders/{orderId}` to show a detailed order-tracking view.

- **For admins / internal systems**
  - Use `PATCH /orders/{orderId}/status?status=CONFIRMED` (etc.) to move orders through the lifecycle.
  - Each update is immediately visible in the user-facing endpoints above because they read the latest `OrderStatus`.

This completes the **order tracking** requirements in the PRD: users can see their order status and history, and admins can update statuses as the order progresses.


