# LifeSamadhan API - Spring Boot Version

This is a Spring Boot equivalent of the .NET LifeSamadhan API, a comprehensive Service Provider Management System with Lombok annotations and proper three-layered architecture.

## Architecture

**Three-Layered Architecture:**
- **Controller Layer**: REST endpoints and request handling
- **Service Layer**: Business logic and transaction management  
- **Repository Layer**: Data access and persistence

## Features

- **User Management**: Registration and authentication for customers, service providers, and admins
- **Service Management**: Categories, services, and pricing
- **Provider Management**: Skills, locations, and availability
- **Admin Functions**: Approve provider skills and locations
- **Clean Architecture**: Proper separation of concerns

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **MySQL Database**
- **Lombok** (for reducing boilerplate code)
- **BCrypt** (for password hashing)
- **OpenAPI/Swagger** (for API documentation)
- **Maven** (build tool)

## Key Lombok Annotations Used

- `@Data` - Generates getters, setters, toString, equals, and hashCode
- `@NoArgsConstructor` - Generates no-argument constructor
- `@AllArgsConstructor` - Generates constructor with all fields
- `@Builder` - Implements builder pattern
- `@RequiredArgsConstructor` - Constructor for final/non-null fields
- `@Slf4j` - Logging support

## Project Structure

```
src/main/java/com/lifesamadhan/api/
├── controller/      # REST controllers (Presentation Layer)
├── service/         # Business logic (Service Layer)
├── repository/      # Data access (Repository Layer)
├── model/          # JPA entities
├── dto/            # Data Transfer Objects
├── config/         # Configuration classes
└── LifeSamadhanApiApplication.java
```

## Three-Layered Architecture Flow

```
Controller → Service → Repository → Database
    ↓         ↓          ↓
  HTTP      Business   Data
Handling    Logic     Access
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Admin Functions
- `GET /api/admin/users` - Get all users
- `PUT /api/admin/skill/{id}/approve` - Approve provider skill
- `PUT /api/admin/location/{id}/approve` - Approve provider location
- `GET /api/admin/skills?status=PENDING` - Get skills by status
- `GET /api/admin/locations?status=PENDING` - Get locations by status

### Service Categories
- `GET /api/service-categories` - Get all categories
- `POST /api/service-categories` - Create category
- `PUT /api/service-categories/{id}` - Update category
- `DELETE /api/service-categories/{id}` - Delete category

### Services
- `GET /api/service` - Get all services
- `GET /api/service/{id}` - Get service by ID
- `GET /api/service/category/{categoryId}` - Get services by category
- `POST /api/service` - Create service
- `PUT /api/service/{id}` - Update service
- `DELETE /api/service/{id}` - Delete service

### Locations
- `GET /api/location` - Get all locations
- `GET /api/location/{id}` - Get location by ID
- `POST /api/location` - Create location
- `PUT /api/location/{id}` - Update location
- `DELETE /api/location/{id}` - Delete location
- `GET /api/location/countries` - Get all countries
- `GET /api/location/states/{country}` - Get states by country
- `GET /api/location/districts/{state}` - Get districts by state
- `GET /api/location/pincodes/{district}` - Get pincodes by district

### Customer Functions
- `POST /api/customer/request` - Create service request
- `POST /api/customer/cancel/{assignmentId}` - Cancel service request
- `POST /api/customer/rating/{assignmentId}` - Submit rating

### Provider Functions
- `POST /api/provider/assignment/{assignmentId}/accept` - Accept assignment
- `POST /api/provider/assignment/{assignmentId}/reject` - Reject assignment
- `POST /api/provider/assignment/{assignmentId}/start` - Start service (with OTP)
- `POST /api/provider/assignment/{assignmentId}/complete` - Complete service

### Provider Management
- `POST /api/provider/location/add` - Add provider location
- `POST /api/provider/skill/add` - Add provider skill

### Payment Management
- `POST /api/payment/create` - Create payment
- `PUT /api/payment/{id}/success` - Mark payment as successful
- `PUT /api/payment/{id}/failed` - Mark payment as failed

### Rating Management
- `POST /api/rating/submit` - Submit rating

## Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lifesamadhan_db
spring.datasource.username=root
spring.datasource.password=password
```

### JWT Configuration
```properties
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000
```

## Key Differences from .NET Version

1. **Annotations**: Uses Lombok annotations instead of manual getters/setters
2. **Dependency Injection**: Uses Spring's `@RequiredArgsConstructor` with Lombok
3. **Validation**: Uses Jakarta Bean Validation instead of .NET Data Annotations
4. **Database**: Uses JPA/Hibernate instead of Entity Framework
5. **Configuration**: Uses `application.properties` instead of `appsettings.json`

## Development Notes

- All entities use Lombok `@Data` for automatic getter/setter generation
- Controllers use `@RequiredArgsConstructor` for dependency injection
- Validation is handled through Jakarta Bean Validation annotations
- JWT implementation uses the `jjwt` library
- OpenAPI documentation is automatically generated

## Future Enhancements

- Add Spring Security for comprehensive authentication/authorization
- Implement caching with Redis
- Add comprehensive unit and integration tests
- Implement audit logging
- Add rate limiting and API throttling