# Backend Capstone Project

A Spring Boot-based e-commerce backend REST API for product management.

## Technology Stack

- **Java 17**
- **Spring Boot 3.4.5**
- **Spring Data JPA**
- **MySQL Database**
- **Flyway** (Database Migration Tool)
- **Lombok**
- **Maven**

## Project Structure

```
src/main/java/com/aditi/backendcapstoneproject/
├── controller/          # REST Controllers
├── service/            # Business Logic Layer
├── repository/         # Data Access Layer
├── model/              # Entity Models
├── dto/                # Data Transfer Objects
├── exception/          # Exception Handling
├── config/             # Configuration Classes
└── component/          # Utility Components
```

## Features

### Product Management
- ✅ Get product by ID
- ✅ Get all products
- ✅ Create new product
- ✅ Update product (full update)
- ✅ Partial update product
- ✅ Category-based product management

### Database
- ✅ MySQL integration
- ✅ Flyway migrations for database schema management
- ✅ JPA/Hibernate for ORM
- ✅ Custom queries support (HQL, Native, Declarative)

### Exception Handling
- ✅ Global exception handler
- ✅ Custom exceptions
- ✅ Standardized error responses

## Database Schema

### Category Table
- `id` (Primary Key)
- `name`
- `description`
- `created_at`
- `last_modified`
- `is_deleted`

### Product Table
- `id` (Primary Key)
- `name`
- `description`
- `image_url`
- `price`
- `category_id` (Foreign Key)
- `created_at`
- `last_modified`
- `is_deleted`

## API Endpoints

| Method | Endpoint | Description | Status Code |
|--------|----------|-------------|-------------|
| GET | `/products/{id}` | Get product by ID | 200 OK / 404 Not Found |
| GET | `/products` | Get all products | 202 Accepted |
| POST | `/products/` | Create a new product | 200 OK |
| PUT | `/products/{id}` | Update entire product | 200 OK |
| PATCH | `/products/{id}` | Partially update product | 200 OK |

## Setup Instructions

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+

### Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE CapstoneBackendProject;
CREATE USER 'CapstoneBackendProject_user'@'localhost' IDENTIFIED BY 'aditimysql@11';
GRANT ALL PRIVILEGES ON CapstoneBackendProject.* TO 'CapstoneBackendProject_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Update `application.properties` if needed with your database credentials.

### Running the Application
1. Clone the repository
2. Navigate to project directory
3. Run the application:
```bash
mvn spring-boot:run
```

Or build and run:
```bash
mvn clean install
java -jar target/backend-capstone-project-0.0.1-SNAPSHOT.jar
```

The application will start on the default port (usually 8080).

## Configuration

### application.properties
- Database connection settings
- Flyway migration configuration
- JPA/Hibernate settings

## Key Components

### Models
- **BaseModel**: Base entity class with common fields (id, name, timestamps, soft delete)
- **Product**: Product entity with price, description, image URL, and category relationship
- **Category**: Category entity with product relationship

### DTOs
- **ProductRequestDto**: Input DTO for creating/updating products
- **ProductResponseDto**: Output DTO for product responses
- **ErrorResponseDto**: Standardized error response format

### Services
- **ProductService**: Interface defining product operations
- **ProductDBService**: Database implementation of product service with category management

### Repositories
- **ProductRepository**: JPA repository with custom query methods
- **CategoryRepository**: JPA repository for category operations

### Exception Handling
- **ProductNotFoundException**: Custom exception for product not found scenarios
- **GlobalExceptionHandler**: Centralized exception handling with `@RestControllerAdvice`

## Testing

Run tests using:
```bash
mvn test
```

## Progress

- ✅ Project setup with Spring Boot 3.4.5
- ✅ Database integration with MySQL
- ✅ Entity models (Product, Category, BaseModel)
- ✅ REST API endpoints implementation
- ✅ Service layer with business logic
- ✅ Repository layer with JPA and custom queries
- ✅ Exception handling
- ✅ Database migrations with Flyway
- ✅ Category management (auto-create if not exists)
- ✅ CRUD operations for products
- ✅ DTOs for request/response mapping

## Future Enhancements

- [ ] Add authentication and authorization
- [ ] Implement pagination for product listing
- [ ] Add filtering and sorting capabilities
- [ ] Add product image upload functionality
- [ ] Implement caching
- [ ] Add unit and integration tests
- [ ] Add API documentation with Swagger/OpenAPI
- [ ] Implement soft delete functionality
- [ ] Add logging and monitoring

