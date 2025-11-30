# Project Structure Plan - Additional Directories & Components

## Current Structure
```
src/main/java/com/aditi/backendcapstoneproject/
├── component/          # Utility components (DbConnectionChecker)
├── config/             # Configuration classes (Security, RestTemplate)
├── controller/         # REST Controllers
├── dto/                # Data Transfer Objects
├── exception/          # Custom exceptions & GlobalExceptionHandler
├── model/              # Entity models
├── repository/         # JPA Repositories
└── service/            # Business logic services
```

---

## Additional Directories & Components Needed

### ✅ 1. **enums/** - Yes, We'll Need This

**When**: Order Management (Phase 3) and Payment (Phase 4)

**Why**: Type safety, better code organization, prevents typos

**Enums to Create**:

#### OrderStatus.java
```java
public enum OrderStatus {
    PENDING,      // Order placed, awaiting confirmation
    CONFIRMED,    // Order confirmed by system
    SHIPPED,      // Order shipped to customer
    DELIVERED,    // Order delivered successfully
    CANCELLED     // Order cancelled
}
```
**Used in**: Order entity, OrderService

#### PaymentStatus.java
```java
public enum PaymentStatus {
    PENDING,      // Payment initiated
    SUCCESS,      // Payment successful
    FAILED,       // Payment failed
    REFUNDED      // Payment refunded
}
```
**Used in**: Payment entity, PaymentService

#### PaymentMethod.java
```java
public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    NET_BANKING,
    COD              // Cash on Delivery
}
```
**Used in**: Payment entity, PaymentRequestDto

#### Role.java (Optional)
```java
public enum Role {
    USER,           // Regular user
    ADMIN           // Administrator
}
```
**Alternative**: Keep as String in User entity (current approach is fine)

**Location**: `src/main/java/com/aditi/backendcapstoneproject/enums/`

---

### ❌ 2. **schedule/** or **scheduled/** - Probably NOT Needed Initially

**When**: Only if PRD requires scheduled tasks

**Why NOT in Initial Implementation**:
- PRD doesn't mention scheduled tasks
- No requirement for automated order status updates
- No cleanup jobs mentioned

**If Needed Later** (Optional Enhancements):
- Scheduled order status updates
- Email reminders for abandoned carts
- Database cleanup jobs
- Report generation

**Implementation** (if needed):
```java
@Component
public class OrderScheduledTasks {
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void updateOrderStatuses() {
        // Auto-update order statuses
    }
}
```

**Location**: `src/main/java/com/aditi/backendcapstoneproject/scheduled/`

**Maven Dependency**: Already included with `spring-boot-starter`

---

### ✅ 3. **advice/** - Already Covered (Current Approach is Good)

**Current**: We have `GlobalExceptionHandler` in `exception/` package

**Status**: ✅ Good as-is, no separate `advice/` directory needed

**Why**:
- `@RestControllerAdvice` is already in `GlobalExceptionHandler`
- It's an exception handler, so `exception/` package makes sense
- Common practice: Keep exception handling in `exception/` package

**Alternative Structure** (if you prefer):
```
exception/
├── GlobalExceptionHandler.java (RestControllerAdvice)
├── ProductNotFoundException.java
└── ...
```

**Recommendation**: Keep current structure - it's cleaner and more logical.

---

### ❌ 4. **builder/** - Not Needed (Lombok Handles This)

**Why NOT Needed**:
- Lombok's `@Builder` annotation provides builder pattern
- We're using DTOs with static `from()` methods (see ProductResponseDto)
- Separate builder directory is overkill for this project

**If We Needed Builders**:
```java
// Using Lombok (no separate directory needed)
@Builder
public class Product {
    // fields
}

// Usage
Product product = Product.builder()
    .name("Laptop")
    .price(999.99)
    .build();
```

**Current Pattern**: Static factory methods work fine
```java
ProductResponseDto.from(product)  // This is our pattern
```

**Recommendation**: Stick with current DTO pattern (static `from()` methods).

---

### ✅ 5. **util/** - Maybe Needed

**When**: If we need utility classes

**Possible Utilities**:
- DateUtils (date formatting, calculations)
- EmailValidator (custom validation logic)
- TokenGenerator (password reset tokens, etc.)
- ResponseHelper (standardized response formatting)

**Example**:
```java
// util/DateUtils.java
public class DateUtils {
    public static Date addDays(Date date, int days) { ... }
    public static String formatDate(Date date) { ... }
}
```

**Recommendation**: Create only if we actually need shared utilities. Don't create empty directory.

---

### ✅ 6. **constants/** - Maybe Needed

**When**: If we need to store constant values

**Possible Constants**:
- Default values
- Configuration constants
- API endpoints
- Error messages

**Example**:
```java
// constants/Constants.java
public class Constants {
    public static final String DEFAULT_USER_ROLE = "USER";
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
}
```

**Current Approach**: Using `application.properties` for configuration (better practice)

**Recommendation**: Only create if we have constants that don't belong in properties/config.

---

### ❌ 7. **mapper/** - Not Needed (Current Pattern Works)

**Current Pattern**: Static factory methods in DTOs
```java
public static ProductResponseDto from(Product product) {
    // conversion logic
}
```

**Alternative**: MapStruct library (separate mapper classes)
- Adds complexity
- Requires additional dependency
- Our current pattern is simpler and sufficient

**Recommendation**: Stick with current DTO pattern.

---

### ✅ 8. **validation/** - Maybe Needed (Custom Validators)

**When**: If we need custom validation annotations

**Examples**:
```java
// validation/ValidEmail.java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface ValidEmail {
    String message() default "Invalid email";
    // ...
}
```

**Current Approach**: Using standard Bean Validation (`@Email`, `@NotBlank`, etc.)

**Recommendation**: Only create if we need custom validation logic beyond standard annotations.

---

## Summary - What We'll Actually Add

### ✅ **Will Create**:

1. **enums/** - Yes
   - `OrderStatus.java` (when implementing orders)
   - `PaymentStatus.java` (when implementing payments)
   - `PaymentMethod.java` (when implementing payments)
   - Maybe `Role.java` (optional improvement)

### ❓ **Might Create** (if needed):

2. **util/** - Only if we need utility classes
   - DateUtils, EmailValidator, etc.

3. **constants/** - Only if we need constant values
   - Configuration constants not suitable for properties file

4. **validation/** - Only if we need custom validators
   - Custom validation annotations

### ❌ **Won't Create**:

5. **schedule/** - Not in PRD requirements
   - Can add later if needed

6. **builder/** - Lombok handles this
   - Current DTO pattern works fine

7. **advice/** - Already covered
   - GlobalExceptionHandler in `exception/` package

8. **mapper/** - Not needed
   - Static factory methods in DTOs work fine

---

## Recommended Project Structure (Final)

```
src/main/java/com/aditi/backendcapstoneproject/
├── component/          # Utility components
├── config/             # Configuration (Security, etc.)
├── constants/          # Constants (only if needed)
├── controller/         # REST Controllers
├── dto/                # Data Transfer Objects
├── enums/              # Enums (OrderStatus, PaymentStatus, etc.) ⭐
├── exception/          # Exceptions & GlobalExceptionHandler
├── model/              # Entity models
├── repository/         # JPA Repositories
├── scheduled/          # Scheduled tasks (only if needed later)
├── service/            # Business logic services
├── util/               # Utility classes (only if needed)
└── validation/         # Custom validators (only if needed)
```

---

## Implementation Timeline

### Phase 1 (Current - User Management):
- ✅ No additional directories needed yet

### Phase 2 (Shopping Cart):
- ✅ No additional directories needed

### Phase 3 (Order Management):
- ⭐ **Create `enums/`** → Add `OrderStatus.java`

### Phase 4 (Payment):
- ⭐ **Update `enums/`** → Add `PaymentStatus.java`, `PaymentMethod.java`

### Phase 5 (Enhancements):
- Maybe add `util/` or `constants/` if needed
- Maybe add `scheduled/` for background jobs

---

## Conclusion

**For Step 4 (Registration & Login)**: No new directories needed

**For Future Steps**:
- **Phase 3**: Create `enums/` directory for OrderStatus
- **Phase 4**: Add PaymentStatus, PaymentMethod to `enums/`
- Others: Only add if actually needed (don't create empty directories)

**Best Practice**: Only create directories when you actually need them. Don't create empty directories "just in case."

