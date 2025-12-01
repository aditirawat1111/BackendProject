# Order Management Implementation - Detailed Explanation

## Overview

Order Management is a critical feature that allows users to:
- Create orders from their shopping cart
- View order history
- Track order status
- View order details

This completes the purchase flow: **Cart → Order → Payment**

---

## Database Design

### Entity Relationships:

```
User (1) ────< (Many) Order (1) ────< (Many) OrderItem (Many) ────> (1) Product
```

- **One User** can have **Many Orders**
- **One Order** has **Many OrderItems**
- **Each OrderItem** references **One Product**

### Why This Design?

1. **Order History**: Users can have multiple orders over time
2. **Order Details**: Each order contains multiple products (OrderItems)
3. **Order Tracking**: Each order has a status that can be updated
4. **Persistence**: Orders are permanent records (unlike cart which can be cleared)

---

## Components to Create

### 1. Enums (NEW Directory!)

#### OrderStatus Enum
**Location**: `src/main/java/com/aditi/backendcapstoneproject/enums/OrderStatus.java`

**Values**:
```java
public enum OrderStatus {
    PENDING,      // Order placed, awaiting confirmation
    CONFIRMED,    // Order confirmed by system
    SHIPPED,      // Order shipped to customer
    DELIVERED,    // Order delivered successfully
    CANCELLED     // Order cancelled
}
```

**Purpose**: Type-safe order status tracking

**Why Enum?**
- Prevents typos (can't use invalid status)
- Type safety
- Better code organization
- Easy to extend later

---

### 2. Entities (Models)

#### A. Order Entity
**Location**: `src/main/java/com/aditi/backendcapstoneproject/model/Order.java`

**Fields**:
```java
- id (Long) - Primary key
- user (User) - Many-to-One relationship
- orderDate (Date) - When order was placed
- status (OrderStatus) - Current order status (enum)
- totalAmount (Double) - Total order value
- deliveryAddress (String) - Delivery address
- orderItems (List<OrderItem>) - One-to-Many relationship
- createdAt (Date)
- lastModified (Date)
- isDeleted (boolean)
```

**Purpose**: Represents a customer order

**Note**: `Order` is a reserved keyword in SQL, so we'll use `@Table(name = "orders")` annotation

#### B. OrderItem Entity
**Location**: `src/main/java/com/aditi/backendcapstoneproject/model/OrderItem.java`

**Fields**:
```java
- id (Long) - Primary key
- order (Order) - Many-to-One relationship with Order
- product (Product) - Many-to-One relationship with Product
- quantity (Integer) - Number of items
- price (Double) - Price at time of order (snapshot)
- createdAt (Date)
- lastModified (Date)
```

**Purpose**: Represents a single product in an order

**Important**: Store `price` snapshot because product prices may change later!

---

### 3. Database Migration

**Location**: `src/main/resources/db/migration/V4__create_order_tables.sql`

**Tables to Create**:

1. **orders** table
   - id, user_id (FK), order_date, status, total_amount, delivery_address
   - created_at, last_modified, is_deleted
   - Foreign key to user table
   
2. **order_item** table
   - id, order_id (FK), product_id (FK), quantity, price
   - created_at, last_modified
   - Foreign keys to orders and product tables

**Note**: `order` is a reserved SQL keyword, so table name is `orders`

---

### 4. Repositories

#### A. OrderRepository
**Location**: `src/main/java/com/aditi/backendcapstoneproject/repository/OrderRepository.java`

**Methods**:
```java
- List<Order> findByUser(User user) - Get all orders for a user
- List<Order> findByUser_Id(Long userId) - Get orders by user ID
- Optional<Order> findById(Long id) - Find order by ID
- Order save(Order order) - Save order
```

**Purpose**: Data access for Order entity

#### B. OrderItemRepository
**Location**: `src/main/java/com/aditi/backendcapstoneproject/repository/OrderItemRepository.java`

**Methods**:
```java
- List<OrderItem> findByOrder(Order order) - Get all items in an order
- OrderItem save(OrderItem orderItem) - Save order item
```

**Purpose**: Data access for OrderItem entity

---

### 5. DTOs (Data Transfer Objects)

#### A. CreateOrderRequestDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/CreateOrderRequestDto.java`

**Fields**:
```java
- deliveryAddress (String) - Required, delivery address
```

**Purpose**: Request body for creating order from cart

**Note**: Cart items are automatically converted to order items

#### B. OrderItemResponseDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/OrderItemResponseDto.java`

**Fields**:
```java
- id (Long)
- productId (Long)
- productName (String)
- quantity (Integer)
- price (Double) - Price at time of order
- subtotal (Double) - quantity * price
```

**Purpose**: Response for individual order item

#### C. OrderResponseDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/OrderResponseDto.java`

**Fields**:
```java
- orderId (Long)
- orderDate (Date)
- status (String) - OrderStatus enum value
- totalAmount (Double)
- deliveryAddress (String)
- items (List<OrderItemResponseDto>)
- totalItems (Integer) - Total quantity of all items
```

**Purpose**: Complete order response

---

### 6. Service Layer

#### OrderService
**Location**: `src/main/java/com/aditi/backendcapstoneproject/service/OrderService.java`

**Methods**:

1. **`createOrder(User user, String deliveryAddress)`**
   - Gets user's cart
   - Validates cart is not empty
   - Creates Order entity
   - Converts CartItems to OrderItems (with price snapshot)
   - Calculates total amount
   - Saves order and order items
   - Clears user's cart (optional - can keep cart for reference)
   - Returns OrderResponseDto

2. **`getOrderById(User user, Long orderId)`**
   - Validates order belongs to user
   - Returns order with all items

3. **`getUserOrders(User user)`**
   - Gets all orders for user
   - Returns list of orders (order history)

4. **`updateOrderStatus(Long orderId, OrderStatus status)`** (Optional - for admin)
   - Updates order status
   - Can be used for tracking (PENDING → CONFIRMED → SHIPPED → DELIVERED)

**Business Logic**:
- Convert cart to order
- Store price snapshot (important!)
- Calculate totals
- Clear cart after order creation
- Validate cart is not empty
- User can only access their own orders

---

### 7. Controller

#### OrderController
**Location**: `src/main/java/com/aditi/backendcapstoneproject/controller/OrderController.java`

**Endpoints**:

1. **`POST /orders`**
   - Create order from cart
   - Request body: `CreateOrderRequestDto` (deliveryAddress)
   - Requires authentication
   - Returns created order

2. **`GET /orders/{orderId}`**
   - Get order details by ID
   - Requires authentication
   - User can only view their own orders
   - Returns order with all items

3. **`GET /orders`**
   - Get user's order history
   - Requires authentication
   - Returns list of all user's orders

4. **`PATCH /orders/{orderId}/status`** (Optional - for admin/future use)
   - Update order status
   - Request body: `{ "status": "SHIPPED" }`
   - Requires authentication (and admin role in future)

**Security**:
- All endpoints require JWT authentication
- Users can only access their own orders
- Validation prevents unauthorized access

---

### 8. Exception Handling

#### Custom Exceptions:
- `OrderNotFoundException` - Order not found
- `EmptyCartException` - Cannot create order from empty cart
- `InvalidOrderStatusException` - Invalid status transition (optional)

#### Update GlobalExceptionHandler:
- Add handlers for new exceptions

---

## Implementation Flow

### Creating Order from Cart:
```
1. User sends: POST /orders { deliveryAddress: "123 Main St" }
   ↓
2. OrderController receives request
   ↓
3. Gets current user from JWT token
   ↓
4. Calls OrderService.createOrder(user, deliveryAddress)
   ↓
5. Service gets user's cart
   ↓
6. Validates cart is not empty
   ├─ Empty → Throw EmptyCartException
   └─ Has items → Continue
   ↓
7. Creates Order entity
   - Sets user, orderDate, status (PENDING), deliveryAddress
   ↓
8. Converts CartItems to OrderItems
   - For each CartItem:
     - Create OrderItem
     - Copy product, quantity
     - Store price snapshot (current product price)
   ↓
9. Calculates totalAmount
   - Sum of all (quantity * price) for each OrderItem
   ↓
10. Saves Order and OrderItems to database
   ↓
11. Clears user's cart (optional)
   ↓
12. Returns OrderResponseDto
```

### Getting Order History:
```
1. User sends: GET /orders
   ↓
2. OrderController gets current user
   ↓
3. Calls OrderService.getUserOrders(user)
   ↓
4. Service queries database for all orders by user
   ↓
5. Returns list of OrderResponseDto
```

---

## Key Design Decisions

### 1. Price Snapshot
**Why store price in OrderItem?**
- Product prices may change over time
- Order should reflect price at time of purchase
- Historical accuracy for records

**Example**:
```
Product price today: $100
User orders today: OrderItem stores $100
Product price tomorrow: $120
User's order still shows $100 (correct!)
```

### 2. Cart Clearing
**Options**:
- **Option A**: Clear cart after order (simpler, cleaner)
- **Option B**: Keep cart (allows reordering, but can be confusing)

**Recommendation**: Clear cart after successful order creation

### 3. Order Status Flow
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
   ↓
CANCELLED (can happen at any stage)
```

### 4. Order vs Cart
| Aspect | Cart | Order |
|--------|-----|-------|
| **Purpose** | Temporary shopping list | Permanent purchase record |
| **Lifecycle** | Can be cleared | Never deleted (soft delete) |
| **Price** | Current product price | Snapshot at order time |
| **Quantity** | Can be updated | Fixed at order time |
| **Status** | N/A | Has status (PENDING, etc.) |

---

## Database Schema Preview

### orders table:
```sql
id (BIGINT PK)
user_id (BIGINT FK → user.id)
order_date (DATETIME)
status (VARCHAR) -- 'PENDING', 'CONFIRMED', etc.
total_amount (DOUBLE)
delivery_address (VARCHAR)
created_at (DATETIME)
last_modified (DATETIME)
is_deleted (BIT)
```

### order_item table:
```sql
id (BIGINT PK)
order_id (BIGINT FK → orders.id)
product_id (BIGINT FK → product.id)
quantity (INT)
price (DOUBLE) -- Snapshot of product price at order time
created_at (DATETIME)
last_modified (DATETIME)
```

---

## API Examples

### Create Order:
```http
POST /orders
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "deliveryAddress": "123 Main Street, City, State 12345"
}

Response: 201 CREATED
{
  "orderId": 1,
  "orderDate": "2024-01-15T10:30:00",
  "status": "PENDING",
  "totalAmount": 2750.00,
  "deliveryAddress": "123 Main Street, City, State 12345",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Gaming Laptop",
      "quantity": 2,
      "price": 1299.99,
      "subtotal": 2599.98
    },
    {
      "id": 2,
      "productId": 5,
      "productName": "Laptop Bag",
      "quantity": 1,
      "price": 150.02,
      "subtotal": 150.02
    }
  ],
  "totalItems": 3
}
```

### Get Order History:
```http
GET /orders
Authorization: Bearer <jwt-token>

Response: 200 OK
[
  {
    "orderId": 1,
    "orderDate": "2024-01-15T10:30:00",
    "status": "DELIVERED",
    "totalAmount": 2750.00,
    ...
  },
  {
    "orderId": 2,
    "orderDate": "2024-01-20T14:20:00",
    "status": "SHIPPED",
    "totalAmount": 899.99,
    ...
  }
]
```

### Get Order Details:
```http
GET /orders/1
Authorization: Bearer <jwt-token>

Response: 200 OK
{
  "orderId": 1,
  "orderDate": "2024-01-15T10:30:00",
  "status": "PENDING",
  "totalAmount": 2750.00,
  "deliveryAddress": "123 Main Street...",
  "items": [...],
  "totalItems": 3
}
```

---

## What We'll Create

**Files to Create:**
1. `enums/OrderStatus.java` - Order status enum ⭐ (NEW DIRECTORY!)
2. `Order.java` - Order entity
3. `OrderItem.java` - OrderItem entity
4. `V4__create_order_tables.sql` - Migration
5. `OrderRepository.java` - Repository
6. `OrderItemRepository.java` - Repository
7. `CreateOrderRequestDto.java` - DTO
8. `OrderItemResponseDto.java` - DTO
9. `OrderResponseDto.java` - DTO
10. `OrderService.java` - Service
11. `OrderController.java` - Controller
12. `OrderNotFoundException.java` - Exception
13. `EmptyCartException.java` - Exception
14. Update `GlobalExceptionHandler.java`

---

## Integration with Existing Features

### Uses:
- ✅ **Cart**: Converts cart items to order items
- ✅ **User**: Links order to authenticated user
- ✅ **Product**: References products in order items
- ✅ **Authentication**: All endpoints protected

### Flow:
```
User Login → Browse Products → Add to Cart → View Cart → 
Create Order → Order History → (Future: Payment)
```

---

## Security Considerations

1. **Authentication Required**: All endpoints protected
2. **User Isolation**: Users can only access their own orders
3. **Cart Validation**: Cannot create order from empty cart
4. **Price Validation**: Store price snapshot to prevent manipulation

---

## Estimated Complexity: Medium-High

**Why Medium-High?**
- Multiple entities and relationships
- Complex business logic (cart to order conversion)
- Price snapshot handling
- Status management
- Integration with cart service
- Calculations for totals

---

## Next Steps After Order Management

1. **Payment Integration** - Process payment for orders
2. **Order Tracking** - Update order status (admin feature)
3. **Order Cancellation** - Allow users to cancel pending orders
4. **Email Notifications** - Send order confirmation emails

---

**Ready to proceed with Order Management implementation?**

This will complete the core purchase flow: **Cart → Order → (Future: Payment)**


