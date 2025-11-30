# Step 7: Shopping Cart Implementation - Detailed Explanation

## Overview

Implementing a shopping cart system that allows authenticated users to:
- Add products to their cart
- View their cart items
- Update quantities
- Remove items
- Clear the entire cart

---

## Entities to Create

### 1. Cart Entity

**Purpose**: Represents a user's shopping cart

**Fields**:
- `id` (from BaseModel)
- `user` (ManyToOne relationship with User)
- `createdAt`, `lastModified`, `isDeleted` (from BaseModel)

**Key Features**:
- One Cart per User (One-to-One relationship)
- Contains multiple CartItems

**Database Table**: `cart`
```
- id (PK)
- user_id (FK → user)
- created_at
- last_modified
- is_deleted
```

### 2. CartItem Entity

**Purpose**: Represents a single product in a cart with quantity

**Fields**:
- `id` (from BaseModel)
- `cart` (ManyToOne relationship with Cart)
- `product` (ManyToOne relationship with Product)
- `quantity` (Integer)
- `createdAt`, `lastModified` (from BaseModel)

**Key Features**:
- Links Cart to Product
- Stores quantity of each product
- Same product can be in cart multiple times (will handle merging logic in service)

**Database Table**: `cart_item`
```
- id (PK)
- cart_id (FK → cart)
- product_id (FK → product)
- quantity (INT)
- created_at
- last_modified
- is_deleted
```

---

## Relationships

```
User (1) ──────── (1) Cart
                         │
                         │ (1)
                         │
                         │
                    CartItem (Many)
                         │
                         │ (Many)
                         │
                    Product (1)
```

**Relationship Explanation**:
- **User ↔ Cart**: One-to-One (each user has one cart)
- **Cart ↔ CartItem**: One-to-Many (one cart has many items)
- **Product ↔ CartItem**: One-to-Many (one product can be in many carts)

---

## DTOs to Create

### 1. AddToCartRequestDto
**Purpose**: Request DTO for adding item to cart

**Fields**:
- `productId` (Long) - Required
- `quantity` (Integer) - Required, min = 1

### 2. UpdateCartItemRequestDto
**Purpose**: Request DTO for updating cart item quantity

**Fields**:
- `quantity` (Integer) - Required, min = 1

### 3. CartItemResponseDto
**Purpose**: Response DTO for cart item

**Fields**:
- `id` (Long)
- `productId` (Long)
- `productName` (String)
- `productPrice` (Double)
- `quantity` (Integer)
- `subtotal` (Double) - quantity × price

### 4. CartResponseDto
**Purpose**: Response DTO for entire cart

**Fields**:
- `cartId` (Long)
- `items` (List<CartItemResponseDto>)
- `totalAmount` (Double) - sum of all item subtotals
- `totalItems` (Integer) - total quantity of items

---

## Repositories to Create

### 1. CartRepository
**Methods**:
- `Optional<Cart> findByUser(User user)` - Find cart by user
- `Optional<Cart> findByUser_Id(Long userId)` - Find cart by user ID
- `Cart save(Cart cart)` - Save cart

### 2. CartItemRepository
**Methods**:
- `Optional<CartItem> findByCartAndProduct(Cart cart, Product product)` - Find item by cart and product
- `List<CartItem> findByCart(Cart cart)` - Find all items in cart
- `void deleteByCart(Cart cart)` - Delete all items in cart (for clear cart)
- `CartItem save(CartItem item)` - Save item

---

## Service Layer

### CartService Interface

**Methods**:
1. `CartResponseDto getCart(String userEmail)` - Get user's cart
2. `CartResponseDto addToCart(String userEmail, AddToCartRequestDto request)` - Add/update item
3. `CartResponseDto updateCartItem(String userEmail, Long itemId, UpdateCartItemRequestDto request)` - Update quantity
4. `CartResponseDto removeFromCart(String userEmail, Long itemId)` - Remove item
5. `void clearCart(String userEmail)` - Clear entire cart

### Business Logic:

#### Add to Cart:
```
1. Get or create user's cart
2. Check if product exists
3. Check if product already in cart
   - If yes: Update quantity (add to existing)
   - If no: Create new cart item
4. Calculate totals
5. Return updated cart
```

#### Update Cart Item:
```
1. Verify cart item belongs to user
2. Update quantity
3. If quantity <= 0, remove item
4. Return updated cart
```

#### Remove from Cart:
```
1. Verify cart item belongs to user
2. Delete cart item
3. Return updated cart
```

---

## Controller Layer

### CartController

**Endpoints**:

1. **GET /cart**
   - Get current user's cart
   - Returns: `CartResponseDto`
   - Auth: Required (JWT)

2. **POST /cart/items**
   - Add product to cart
   - Body: `AddToCartRequestDto`
   - Returns: `CartResponseDto`
   - Auth: Required

3. **PUT /cart/items/{itemId}**
   - Update cart item quantity
   - Body: `UpdateCartItemRequestDto`
   - Returns: `CartResponseDto`
   - Auth: Required

4. **DELETE /cart/items/{itemId}**
   - Remove item from cart
   - Returns: `CartResponseDto`
   - Auth: Required

5. **DELETE /cart**
   - Clear entire cart
   - Returns: `204 No Content` or `200 OK`
   - Auth: Required

---

## Security Considerations

### All endpoints require authentication:
- User must be logged in (JWT token required)
- Users can only access their own cart
- Validation: Ensure cart items belong to the authenticated user

### Security Implementation:
```java
// Get user from JWT token
String email = ((UserDetails) authentication.getPrincipal()).getUsername();

// Verify cart belongs to user
Cart cart = cartRepository.findByUser(user)
    .orElseThrow(() -> new CartNotFoundException("Cart not found"));

if (!cart.getUser().getEmail().equals(email)) {
    throw new AccessDeniedException("Cannot access other user's cart");
}
```

---

## Database Migration

### V3__create_cart_tables.sql

```sql
CREATE TABLE cart
(
    id            BIGINT       NOT NULL,
    created_at    datetime     NULL,
    last_modified datetime     NULL,
    is_deleted    BIT(1)       NOT NULL,
    user_id       BIGINT       NULL,
    CONSTRAINT pk_cart PRIMARY KEY (id)
);

CREATE TABLE cart_item
(
    id            BIGINT       NOT NULL,
    created_at    datetime     NULL,
    last_modified datetime     NULL,
    is_deleted    BIT(1)       NOT NULL,
    cart_id       BIGINT       NULL,
    product_id    BIGINT       NULL,
    quantity      INT          NOT NULL,
    CONSTRAINT pk_cart_item PRIMARY KEY (id)
);

ALTER TABLE cart
    ADD CONSTRAINT FK_CART_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE cart_item
    ADD CONSTRAINT FK_CART_ITEM_ON_CART FOREIGN KEY (cart_id) REFERENCES cart (id);

ALTER TABLE cart_item
    ADD CONSTRAINT FK_CART_ITEM_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);
```

---

## Exceptions to Create

### 1. CartNotFoundException
- Thrown when cart doesn't exist
- HTTP Status: 404 NOT FOUND

### 2. CartItemNotFoundException
- Thrown when cart item doesn't exist
- HTTP Status: 404 NOT FOUND

### 3. InvalidQuantityException (Optional)
- Thrown when quantity is invalid (<= 0)
- HTTP Status: 400 BAD REQUEST

---

## Important Design Decisions

### 1. Cart Creation Strategy
**Option A: Lazy Creation**
- Create cart when first item is added
- Pros: No empty carts in database
- Cons: Need to check/create on every operation

**Option B: Eager Creation**
- Create cart when user registers
- Pros: Always exists
- Cons: Empty carts for inactive users

**We'll use: Lazy Creation** (create when first item added)

### 2. Duplicate Product Handling
**When same product added twice:**
- Merge quantities instead of creating duplicate items
- Example: Add "Laptop" (qty 1) + Add "Laptop" (qty 2) = One item with qty 3

### 3. Cart Totals Calculation
- Calculate on-the-fly from cart items
- `totalAmount = sum(item.quantity × item.product.price)`
- Store in response DTO (not in database)

---

## API Examples

### Get Cart
```
GET /cart
Authorization: Bearer <token>

Response (200 OK):
{
  "cartId": 1,
  "items": [
    {
      "id": 1,
      "productId": 5,
      "productName": "Gaming Laptop",
      "productPrice": 1299.99,
      "quantity": 2,
      "subtotal": 2599.98
    }
  ],
  "totalAmount": 2599.98,
  "totalItems": 2
}
```

### Add to Cart
```
POST /cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 5,
  "quantity": 1
}

Response (200 OK):
{
  "cartId": 1,
  "items": [...],
  "totalAmount": 2599.98,
  "totalItems": 2
}
```

### Update Cart Item
```
PUT /cart/items/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}

Response (200 OK):
{
  "cartId": 1,
  "items": [...],
  "totalAmount": 3899.97,
  "totalItems": 3
}
```

### Remove from Cart
```
DELETE /cart/items/1
Authorization: Bearer <token>

Response (200 OK):
{
  "cartId": 1,
  "items": [],
  "totalAmount": 0.0,
  "totalItems": 0
}
```

---

## Implementation Steps

1. **Create Cart Entity** (extends BaseModel)
2. **Create CartItem Entity** (extends BaseModel)
3. **Create Database Migration** (V3__create_cart_tables.sql)
4. **Create Repositories** (CartRepository, CartItemRepository)
5. **Create DTOs** (Request & Response DTOs)
6. **Create Exceptions** (CartNotFoundException, etc.)
7. **Create CartService** (Interface & Implementation)
8. **Create CartController** (REST endpoints)
9. **Update SecurityConfig** (Protect /cart/** endpoints)
10. **Update GlobalExceptionHandler** (Handle cart exceptions)
11. **Test endpoints**

---

## Files to Create/Modify

### New Files:
- `model/Cart.java`
- `model/CartItem.java`
- `repository/CartRepository.java`
- `repository/CartItemRepository.java`
- `dto/AddToCartRequestDto.java`
- `dto/UpdateCartItemRequestDto.java`
- `dto/CartItemResponseDto.java`
- `dto/CartResponseDto.java`
- `service/CartService.java`
- `service/CartServiceImpl.java` (or `CartDBService.java`)
- `controller/CartController.java`
- `exception/CartNotFoundException.java`
- `exception/CartItemNotFoundException.java`
- `db/migration/V3__create_cart_tables.sql`

### Modified Files:
- `config/SecurityConfig.java` - Add /cart/** to protected endpoints
- `exception/GlobalExceptionHandler.java` - Add cart exception handlers

---

## Summary

This step implements a complete shopping cart system with:
- ✅ Cart and CartItem entities
- ✅ Full CRUD operations
- ✅ Quantity management
- ✅ Automatic total calculation
- ✅ Security (JWT authentication)
- ✅ User isolation (users can only access their own cart)

Ready to proceed with implementation!

