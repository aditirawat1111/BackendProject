# Project Structure - Current Implementation

## 📁 Complete Directory Structure

```
BackendProject/
│
├── pom.xml                                    # Maven configuration with dependencies
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/aditi/backendcapstoneproject/
│   │   │       │
│   │   │       ├── BackendCapstoneProjectApplication.java  # Main Spring Boot application
│   │   │       │
│   │   │       ├── component/                 # Utility components
│   │   │       │   └── DbConnectionChecker.java
│   │   │       │
│   │   │       ├── config/                    # Configuration classes
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── RestTemplateConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       │
│   │   │       ├── controller/                # REST Controllers
│   │   │       │   ├── AuthController.java
│   │   │       │   └── ProductController.java
│   │   │       │
│   │   │       ├── dto/                       # Data Transfer Objects
│   │   │       │   ├── AuthResponseDto.java
│   │   │       │   ├── ErrorResponseDto.java
│   │   │       │   ├── FakeStoreProductDto.java
│   │   │       │   ├── FakeStoreProductRequestDto.java
│   │   │       │   ├── LoginRequestDto.java
│   │   │       │   ├── ProductRequestDto.java
│   │   │       │   ├── ProductResponseDto.java
│   │   │       │   └── RegisterRequestDto.java
│   │   │       │
│   │   │       ├── exception/                 # Exception handling
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── InvalidCredentialsException.java
│   │   │       │   ├── ProductNotFoundException.java
│   │   │       │   └── UserAlreadyExistsException.java
│   │   │       │
│   │   │       ├── model/                     # Entity models (JPA)
│   │   │       │   ├── BaseModel.java
│   │   │       │   ├── Category.java
│   │   │       │   ├── Product.java
│   │   │       │   └── User.java
│   │   │       │
│   │   │       ├── repository/                # Data access layer
│   │   │       │   ├── CategoryRepository.java
│   │   │       │   ├── CustomQuery.java
│   │   │       │   ├── ProductRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       │
│   │   │       └── service/                   # Business logic layer
│   │   │           ├── AuthenticationService.java
│   │   │           ├── CustomUserDetailsService.java
│   │   │           ├── FakeStoreProductService.java
│   │   │           ├── JwtService.java
│   │   │           ├── ProductDBService.java
│   │   │           └── ProductService.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties         # Application configuration
│   │       └── db/
│   │           └── migration/                 # Flyway database migrations
│   │               ├── V1__inti.sql
│   │               └── V2__create_user_table.sql
│   │
│   └── test/
│       └── java/
│           └── com/aditi/backendcapstoneproject/
│               └── BackendCapstoneProjectApplicationTests.java
│
└── target/                                    # Compiled classes (generated)
```

---

## 📦 Package Details

### 🎯 **Main Application**
- **BackendCapstoneProjectApplication.java**
  - Spring Boot entry point
  - Auto-configuration and component scanning

---

### 🔧 **component/** - Utility Components
| File | Purpose |
|------|---------|
| `DbConnectionChecker.java` | Verifies database connection on application startup |

---

### ⚙️ **config/** - Configuration Classes
| File | Purpose |
|------|---------|
| `SecurityConfig.java` | Spring Security configuration<br>- Public/protected endpoints<br>- JWT authentication filter<br>- Password encoder (BCrypt)<br>- Session management (stateless) |
| `JwtAuthenticationFilter.java` | Intercepts HTTP requests<br>- Extracts JWT tokens from headers<br>- Validates tokens<br>- Sets authentication context |
| `RestTemplateConfig.java` | Configuration for REST API calls<br>- External API integration (FakeStore) |

---

### 🌐 **controller/** - REST Controllers
| File | Endpoints | Authentication |
|------|-----------|----------------|
| `ProductController.java` | `GET /products`<br>`GET /products/{id}`<br>`POST /products/`<br>`PUT /products/{id}`<br>`PATCH /products/{id}` | Public (can be changed) |
| `AuthController.java` | `POST /auth/register`<br>`POST /auth/login` | Public |

---

### 📋 **dto/** - Data Transfer Objects
| File | Purpose | Used By |
|------|---------|---------|
| `RegisterRequestDto.java` | Registration input validation | AuthController |
| `LoginRequestDto.java` | Login credentials | AuthController |
| `AuthResponseDto.java` | Authentication response with JWT token | AuthController |
| `ProductRequestDto.java` | Product creation/update input | ProductController |
| `ProductResponseDto.java` | Product response output | ProductController |
| `ErrorResponseDto.java` | Standardized error responses | GlobalExceptionHandler |
| `FakeStoreProductDto.java` | External API response mapping | FakeStoreProductService |
| `FakeStoreProductRequestDto.java` | External API request mapping | FakeStoreProductService |

---

### ⚠️ **exception/** - Exception Handling
| File | Purpose | HTTP Status |
|------|---------|-------------|
| `GlobalExceptionHandler.java` | Centralized exception handling<br>- Handles all custom exceptions<br>- Validates request errors | Various |
| `ProductNotFoundException.java` | Product not found exception | 404 NOT FOUND |
| `UserAlreadyExistsException.java` | Email already exists during registration | 409 CONFLICT |
| `InvalidCredentialsException.java` | Invalid login credentials | 401 UNAUTHORIZED |

---

### 🗄️ **model/** - Entity Models (JPA)
| File | Purpose | Extends | Relationships |
|------|---------|---------|---------------|
| `BaseModel.java` | Base entity with common fields<br>- id, name, createdAt, lastModified, isDeleted | - | - |
| `User.java` | User entity<br>- email, password, phoneNumber, address, role | BaseModel | - |
| `Product.java` | Product entity<br>- description, imageUrl, price | BaseModel | ManyToOne → Category |
| `Category.java` | Category entity<br>- description | BaseModel | OneToMany → Product |

---

### 💾 **repository/** - Data Access Layer
| File | Purpose | Key Methods |
|------|---------|-------------|
| `UserRepository.java` | User data access | `findByEmail()`, `existsByEmail()` |
| `ProductRepository.java` | Product data access | `findById()`, `findAll()`, `findByCategory_Name()`, `getProductByCategoryName()`, `getProductByCategoryNameNative()` |
| `CategoryRepository.java` | Category data access | `findByName()`, `save()` |
| `CustomQuery.java` | SQL query constants | Native query strings |

---

### 🔨 **service/** - Business Logic Layer
| File | Purpose | Key Methods |
|------|---------|-------------|
| `AuthenticationService.java` | Authentication business logic | `register()`, `login()` |
| `JwtService.java` | JWT token operations | `generateToken()`, `validateToken()`, `extractEmail()` |
| `CustomUserDetailsService.java` | Spring Security user loading | `loadUserByUsername()` |
| `ProductService.java` | Product service interface | Contract for product operations |
| `ProductDBService.java` | Product service implementation | `getProductsById()`, `getAllProducts()`, `createProduct()`, `updateProduct()`, `partialUpdateProduct()` |
| `FakeStoreProductService.java` | External API integration | Integration with FakeStore API (for practice) |

---

### 📄 **resources/**

#### **application.properties**
```properties
# Application Configuration
spring.application.name=BackendCapstoneProject

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/CapstoneBackendProject
spring.datasource.username=CapstoneBackendProject_user
spring.datasource.password=aditimysql@11

# JPA Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000  # 24 hours
```

#### **db/migration/** - Database Migrations
| File | Purpose | Tables Created |
|------|---------|----------------|
| `V1__inti.sql` | Initial database schema | `category`, `product` |
| `V2__create_user_table.sql` | User table creation | `user` |

---

## 🔐 Security Configuration

### Public Endpoints (No Authentication Required):
- `POST /auth/register`
- `POST /auth/login`
- `GET /products`
- `GET /products/{id}`

### Protected Endpoints (JWT Token Required):
- All other endpoints require valid JWT token in `Authorization: Bearer <token>` header

### Security Features:
- ✅ JWT-based authentication
- ✅ BCrypt password hashing
- ✅ Stateless session management
- ✅ Role-based foundation (ready for authorization)

---

## 📊 Database Schema

### Tables:

1. **user**
   - `id` (PK)
   - `name`
   - `email` (unique)
   - `password` (hashed)
   - `phone_number`
   - `address`
   - `role`
   - `created_at`
   - `last_modified`
   - `is_deleted`

2. **category**
   - `id` (PK)
   - `name`
   - `description`
   - `created_at`
   - `last_modified`
   - `is_deleted`

3. **product**
   - `id` (PK)
   - `name`
   - `description`
   - `image_url`
   - `price`
   - `category_id` (FK → category)
   - `created_at`
   - `last_modified`
   - `is_deleted`

---

## 🚀 API Endpoints Summary

### Authentication Endpoints:
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | ❌ No |
| POST | `/auth/login` | User login | ❌ No |

### Product Endpoints:
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/products` | Get all products | ❌ No |
| GET | `/products/{id}` | Get product by ID | ❌ No |
| POST | `/products/` | Create product | ❌ No (should be protected) |
| PUT | `/products/{id}` | Update product | ❌ No (should be protected) |
| PATCH | `/products/{id}` | Partial update product | ❌ No (should be protected) |

---

## 📦 Dependencies (Maven)

### Core:
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL Connector

### Security:
- JWT (jjwt 0.12.3)
- BCrypt (via Spring Security)

### Database:
- Flyway (database migrations)

### Utilities:
- Lombok
- Validation

---

## ✅ Implemented Features

### Completed:
1. ✅ Project setup with Spring Boot 3.4.5
2. ✅ Database integration with MySQL
3. ✅ Flyway database migrations
4. ✅ Product CRUD operations
5. ✅ Category management
6. ✅ User entity and repository
7. ✅ Spring Security configuration
8. ✅ JWT authentication
9. ✅ User registration
10. ✅ User login
11. ✅ Password encryption (BCrypt)
12. ✅ Exception handling
13. ✅ Request validation

### Pending (Based on PRD):
1. ⏳ Profile management endpoints
2. ⏳ Product search functionality
3. ⏳ Shopping cart
4. ⏳ Order management
5. ⏳ Payment integration
6. ⏳ Password reset functionality

---

## 🏗️ Architecture Pattern

**Layered Architecture:**
```
Controller Layer (REST APIs)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (MySQL)
```

**Security Flow:**
```
HTTP Request
    ↓
JwtAuthenticationFilter (Token Validation)
    ↓
SecurityConfig (Authorization)
    ↓
Controller
    ↓
Service
```

---

## 📝 Notes

- **FakeStoreProductService**: Practice implementation for external API integration (not actively used)
- **BaseModel**: Provides common fields to all entities (DRY principle)
- **Static Factory Methods**: DTOs use `from()` methods for entity conversion
- **Exception Handling**: Centralized in GlobalExceptionHandler
- **Validation**: Bean Validation annotations on DTOs

---

## 🔄 Next Steps

1. Profile management endpoints
2. Product search functionality
3. Shopping cart implementation
4. Order management
5. Payment integration
6. Password reset flow

---

*Last Updated: After Step 4 Implementation (Registration & Login)*

