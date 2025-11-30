# Step 7: Shopping Cart Implementation - Detailed Explanation

## Overview

Shopping Cart is a critical feature that allows users to:
- Add products to their cart
- View cart contents
- Update quantities
- Remove items
- Clear the entire cart

---

## Database Design

### Entity Relationships:

```
User (1) ────< (1) Cart (1) ────< (Many) CartItem (Many) ────> (1) Product
```

- **One User** has **One Cart**
- **One Cart** has **Many CartItems**
- **Each CartItem** references **One Product**

### Why This Design?

1. **One Cart Per User**: Each user has a single active cart
2. **Cart Persistence**: Cart survives logout (stored in database)
3. **Multiple Items**: Cart can contain multiple different products
4. **Quantity Tracking**: Each item can have a quantity > 1

---

## Components to Create

### 1. Entities (Models)

#### A. Cart Entity
**Location**: `src/main/java/com/aditi/backendcapstoneproject/model/Cart.java`

**Fields**:
```java
- id (Long) - Primary key
- user (User) - One-to-One relationship with User
- createdAt (Date)
- lastModified (Date)
- isDeleted (boolean)
- cartItems (List<CartItem>) - One-to-Many relationship
```

**Purpose**: Represents a user's shopping cart

#### B. CartItem Entity
**Location**: `src/main/java/com/aditi/backendcapstoneproject/model/CartItem.java`

**Fields**:
```java
- id (Long) - Primary key
- cart (Cart) - Many-to-One relationship with Cart
- product (Product) - Many-to-One relationship with Product
- quantity (Integer) - Number of items
- createdAt (Date)
- lastModified (Date)
```

**Purpose**: Represents a single item in the cart (product + quantity)

**Constraints**:
- One CartItem per Product in a Cart (can't add same product twice - update quantity instead)
- Quantity must be > 0

---

### 2. Database Migration

**Location**: `src/main/resources/db/migration/V3__create_cart_tables.sql`

**Tables to Create**:
1. **cart** table
   - id, user_id (FK), created_at, last_modified, is_deleted
   - Foreign key to user table
   
2. **cart_item** table
   - id, cart_id (FK), product_id (FK), quantity, created_at, last_modified
   - Foreign keys to cart and product tables
   - Unique constraint: (cart_id, product_id) - one item per product per cart

---

### 3. Repositories

#### A. CartRepository
**Location**: `src/main/java/com/aditi/backendcapstoneproject/repository/CartRepository.java`

**Methods**:
```java
- Optional<Cart> findByUser(User user)
- Optional<Cart> findByUser_Id(Long userId)
- Cart save(Cart cart)
```

**Purpose**: Data access for Cart entity

#### B. CartItemRepository
**Location**: `src/main/java/com/aditi/backendcapstoneproject/repository/CartItemRepository.java`

**Methods**:
```java
- List<CartItem> findByCart(Cart cart)
- Optional<CartItem> findByCartAndProduct(Cart cart, Product product)
- void deleteByCart(Cart cart)
- CartItem save(CartItem cartItem)
- void delete(CartItem cartItem)
```

**Purpose**: Data access for CartItem entity

---

### 4. DTOs (Data Transfer Objects)

#### A. AddToCartRequestDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/AddToCartRequestDto.java`

**Fields**:
```java
- productId (Long) - Required
- quantity (Integer) - Required, min 1
```

**Purpose**: Request body for adding items to cart

#### B. UpdateCartItemRequestDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/UpdateCartItemRequestDto.java`

**Fields**:
```java
- quantity (Integer) - Required, min 1
```

**Purpose**: Request body for updating cart item quantity

#### C. CartItemResponseDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/CartItemResponseDto.java`

**Fields**:
```java
- id (Long)
- productId (Long)
- productName (String)
- productPrice (Double)
- productImageUrl (String)
- quantity (Integer)
- subtotal (Double) - quantity * price
```

**Purpose**: Response for individual cart item

#### D. CartResponseDto
**Location**: `src/main/java/com/aditi/backendcapstoneproject/dto/CartResponseDto.java`

**Fields**:
```java
- cartId (Long)
- items (List<CartItemResponseDto>)
- totalItems (Integer) - Total quantity of all items
- totalAmount (Double) - Sum of all item subtotals
```

**Purpose**: Complete cart response with totals

---

### 5. Service Layer

#### CartService
**Location**: `src/main/java/com/aditi/backendcapstoneproject/service/CartService.java`

**Methods**:

1. **`getCart(User user)`**
   - Gets or creates cart for user
   - Returns cart with all items loaded

2. **`addItemToCart(User user, Long productId, Integer quantity)`**
   - Finds or creates cart
   - Checks if product already in cart
   - If exists: Updates quantity
   - If new: Creates new CartItem
   - Returns updated cart

3. **`updateCartItem(User user, Long itemId, Integer quantity)`**
   - Validates cart belongs to user
   - Updates cart item quantity
   - Returns updated cart

4. **`removeCartItem(User user, Long itemId)`**
   - Validates cart belongs to user
   - Removes cart item
   - Returns updated cart

5. **`clearCart(User user)`**
   - Validates cart belongs to user
   - Deletes all cart items
   - Returns empty cart

**Business Logic**:
- Auto-create cart if doesn't exist
- Handle duplicate products (update quantity, don't create duplicate)
- Calculate totals
- Validate quantities (must be > 0)

---

### 6. Controller

#### CartController
**Location**: `src/main/java/com/aditi/backendcapstoneproject/controller/CartController.java`

**Endpoints**:

1. **`GET /cart`**
   - Get current user's cart
   - Requires authentication
   - Returns cart with all items and totals

2. **`POST /cart/items`**
   - Add item to cart
   - Request body: `AddToCartRequestDto`
   - Requires authentication
   - Returns updated cart

3. **`PUT /cart/items/{itemId}`**
   - Update cart item quantity
   - Request body: `UpdateCartItemRequestDto`
   - Requires authentication
   - Returns updated cart

4. **`DELETE /cart/items/{itemId}`**
   - Remove item from cart
   - Requires authentication
   - Returns updated cart

5. **`DELETE /cart`**
   - Clear entire cart
   - Requires authentication
   - Returns empty cart

**Security**:
- All endpoints require JWT authentication
- Uses `@AuthenticationPrincipal` to get current user
- Only user can access their own cart

---

### 7. Exception Handling

#### Custom Exceptions:
- `CartItemNotFoundException` - Item not found in cart
- `ProductNotFoundException` - Already exists (reuse from Product)
- `InvalidQuantityException` - Quantity must be > 0

#### Update GlobalExceptionHandler:
- Add handlers for new exceptions

---

## Implementation Flow

### Adding Item to Cart:
```
1. User sends: POST /cart/items {productId: 1, quantity: 2}
   ↓
2. CartController receives request
   ↓
3. Gets current user from JWT token
   ↓
4. Calls CartService.addItemToCart(user, productId, quantity)
   ↓
5. Service checks if user has cart
   ├─ NO → Creates new cart
   └─ YES → Uses existing cart
   ↓
6. Service checks if product already in cart
   ├─ YES → Updates existing CartItem quantity
   └─ NO → Creates new CartItem
   ↓
7. Service calculates totals
   ↓
8. Returns CartResponseDto with updated cart
```

### Getting Cart:
```
1. User sends: GET /cart
   ↓
2. CartController gets current user
   ↓
3. Calls CartService.getCart(user)
   ↓
4. Service finds or creates cart
   ↓
5. Loads all cart items with products
   ↓
6. Calculates totals
   ↓
7. Returns CartResponseDto
```

---

## Security Considerations

1. **Authentication Required**: All endpoints protected
2. **User Isolation**: Users can only access their own cart
3. **Validation**: Product existence, quantity validation
4. **Ownership Check**: Verify cart belongs to user before operations

---

## Key Design Decisions

1. **One Cart Per User**: Simpler than multiple carts per user
2. **Persistent Cart**: Cart survives logout (stored in DB)
3. **Quantity Update**: Adding same product updates quantity (no duplicates)
4. **Calculated Totals**: Total calculated on-the-fly (not stored)
5. **Cart Auto-Creation**: Cart created automatically when first item added

---

## Database Schema Preview

### cart table:
```sql
id (BIGINT PK)
user_id (BIGINT FK → user.id, UNIQUE)
created_at (DATETIME)
last_modified (DATETIME)
is_deleted (BIT)
```

### cart_item table:
```sql
id (BIGINT PK)
cart_id (BIGINT FK → cart.id)
product_id (BIGINT FK → product.id)
quantity (INT)
created_at (DATETIME)
last_modified (DATETIME)
UNIQUE(cart_id, product_id) -- One item per product per cart
```

---

## API Examples

### Add Item to Cart:
```http
POST /cart/items
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}

Response: 200 OK
{
  "cartId": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Gaming Laptop",
      "productPrice": 1299.99,
      "productImageUrl": "https://...",
      "quantity": 2,
      "subtotal": 2599.98
    }
  ],
  "totalItems": 2,
  "totalAmount": 2599.98
}
```

### Get Cart:
```http
GET /cart
Authorization: Bearer <jwt-token>

Response: 200 OK
{
  "cartId": 1,
  "items": [...],
  "totalItems": 3,
  "totalAmount": 2750.00
}
```

---

## What We'll Create

**Files to Create:**
1. `Cart.java` - Entity
2. `CartItem.java` - Entity
3. `V3__create_cart_tables.sql` - Migration
4. `CartRepository.java` - Repository
5. `CartItemRepository.java` - Repository
6. `AddToCartRequestDto.java` - DTO
7. `UpdateCartItemRequestDto.java` - DTO
8. `CartItemResponseDto.java` - DTO
9. `CartResponseDto.java` - DTO
10. `CartService.java` - Service
11. `CartController.java` - Controller
12. `CartItemNotFoundException.java` - Exception
13. Update `GlobalExceptionHandler.java`

---

## Estimated Complexity: Medium

**Why Medium?**
- Multiple entities and relationships
- Business logic for cart operations
- Calculations for totals
- Security validation
- Edge cases (duplicate products, etc.)

---

**Ready to proceed with Shopping Cart implementation?**

