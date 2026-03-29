# SheCare Backend - Architecture Documentation

## 🏗️ System Architecture Overview

SheCare is a production-grade, stateless REST API backend built with Spring Boot following a layered hexagonal (clean) architecture pattern. The system is designed for scalability, security, and maintainability.

---

## 📐 Architecture Layers

### 1. **Controller Layer** (REST API Entry Point)
Responsible for handling HTTP requests and responses.

```
HTTPRequest → Controller → Service → Repository → Database
                ↓              ↓           ↓
          Request Validation   Business   Data
          Response Formatting  Logic      Access
          Error Handling
```

**Files:** `controller/*.java`
- `AuthController` - Authentication endpoints
- `UserController` - User profile endpoints
- `PostController` - Posts management
- `SymptomController` - Symptom tracking
- `AppointmentController` - Appointment management
- `AdminController` - Admin operations
- `ResourceController` - Public resources
- `StatsController` - Statistics
- `PredictController` - ML predictions

**Responsibilities:**
- Parse HTTP requests
- Validate input (delegated to Spring Validation)
- Call appropriate services
- Return HTTP responses
- Map entities to DTOs

---

### 2. **Service Layer** (Business Logic)
Contains all business logic and orchestrates operations.

**Files:** `service/*.java`
- `AuthService` - Authentication logic, token generation/validation
- `PostService` - Post CRUD operations
- `SymptomService` - Symptom CRUD operations
- `AppointmentService` - Appointment CRUD operations
- `UserMapper` - DTO mapping utility

**Responsibilities:**
- Implement business rules
- Enforce data ownership
- Coordinate multiple repositories
- Handle transactional logic
- Convert entities to DTOs
- Validate business constraints

**Key Pattern:**
```java
Service receives Request DTO
    ↓
Validate business rules
    ↓
Call repository/repositories
    ↓
Perform operations
    ↓
Convert to Response DTO
    ↓
Return to Controller
```

---

### 3. **Repository Layer** (Data Access)
Abstracts database operations using Spring Data JPA.

**Files:** `repository/*.java`
- `UserRepository` - User queries
- `RefreshTokenRepository` - Token storage queries
- `PostRepository` - Post queries
- `SymptomRepository` - Symptom queries
- `AppointmentRepository` - Appointment queries

**Responsibilities:**
- Provide CRUD operations
- Define custom queries
- Abstract database details
- Provide query optimization

**Features:**
- Spring Data JPA auto-implementation
- Custom query methods
- Pagination support (extensible)
- Foreign key relationships

---

### 4. **Entity/Model Layer** (Domain Objects)
JPA entities representing database tables.

**Files:** `model/*.java`
- `User` - User with roles
- `Role` - Role enumeration
- `RefreshToken` - Token storage
- `Post` - Blog posts
- `Symptom` - Health symptoms
- `Appointment` - Doctor appointments

**Key Features:**
- JPA/Hibernate mapped to database tables
- Audit timestamps (createdAt, updatedAt)
- Relationships (One-to-Many, Many-to-Many)
- Validation annotations (partial)
- Lombok annotations (reduce boilerplate)

---

### 5. **DTO Layer** (Data Transfer Objects)
Request/Response objects for API contracts.

**Files:** `dto/*.java`
- `RegisterRequest` / `AuthResponse`
- `LoginRequest`
- `RefreshTokenRequest`
- `UserResponse`
- `PostRequest` / `PostResponse`
- `SymptomRequest` / `SymptomResponse`
- `AppointmentRequest` / `AppointmentResponse`

**Responsibilities:**
- Define API contracts
- Validate input
- Decouple API from entity structure
- Type-safe data exchange

---

### 6. **Security Layer** (Authentication & Authorization)
Manages JWT authentication and Spring Security.

**Files:** `security/*.java`
- `SecurityConfig` - Spring Security configuration
- `JwtService` - Token generation/validation
- `JwtAuthenticationFilter` - Request interceptor
- `CustomUserDetailsService` - User loading

**Architecture:**
```
Request
    ↓
JwtAuthenticationFilter
    ↓
Extract JWT from header
    ↓
Validate JWT signature & expiry
    ↓
Load user from database
    ↓
Set SecurityContext
    ↓
Controller (with @PreAuthorize checks)
    ↓
Response
```

**Security Features:**
- Stateless JWT authentication
- HS512 signing algorithm
- Access token (15 min) + Refresh token (7 days)
- BCrypt password hashing
- Role-based access control

---

### 7. **Exception Handling Layer** (Error Management)
Global exception handler with standardized responses.

**Files:** `exception/*.java`
- `GlobalExceptionHandler` - Catches all exceptions
- `AuthenticationException` - Auth errors
- `ValidationException` - Validation errors
- `ResourceNotFoundException` - 404 errors
- `ErrorResponse` - Standard error DTO

**Response Format:**
```json
{
  "timestamp": "2026-03-29T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/v1/auth/login",
  "validationErrors": { }
}
```

---

## 🔄 Request Flow Diagram

### Authentication Flow
```
1. Client submits credentials
   ↓
2. AuthController.login() receives LoginRequest
   ↓
3. AuthService.login() performs:
   - Find user by email
   - Verify password with BCrypt
   - Check if user enabled
   ↓
4. JwtService generates:
   - Access token (15 min)
   - Refresh token (7 days)
   ↓
5. RefreshTokenRepository saves token hash
   ↓
6. AuthResponse returns to client
   {accessToken, refreshToken, user}
   ↓
7. Client stores tokens (localStorage or cookie)
```

### Protected Request Flow
```
1. Client sends request with Authorization header
   GET /api/v1/users/me
   Authorization: Bearer eyJhbGc...
   ↓
2. JwtAuthenticationFilter intercepts
   ↓
3. Extract token from header
   ↓
4. JwtService.isTokenValid() checks:
   - Signature (HS512)
   - Expiry time
   - Clock skew tolerance
   ↓
5. Extract userId and set SecurityContext
   ↓
6. Controller executes with authenticated user
   ↓
7. Service enforces data ownership
   ↓
8. Response sent to client
```

### Post Creation Flow (with Ownership)
```
1. POST /api/v1/posts
   Headers: Authorization: Bearer <token>
   Body: {title, content}
   ↓
2. PostController.createPost()
   - Get userId from SecurityContext
   ↓
3. PostService.createPost(userId, request)
   - Create Post entity
   - Set userId (ownership)
   - Save to repository
   ↓
4. PostRepository.save()
   - Execute INSERT statement
   ↓
5. Convert Post to PostResponse DTO
   ↓
6. Return 201 Created
```

---

## 🗄️ Database Design

### Schema Architecture
```
Users (Core)
├── id (PK, UUID)
├── email (UK)
├── passwordHash (BCrypt)
├── name, avatar, profile fields
├── createdAt, updatedAt
└── enabled (soft delete)
    ↓
User_Roles (Many-to-Many)
├── user_id (FK → Users)
└── role (ENUM: PATIENT, DOCTOR, ADMIN)

RefreshTokens (Security)
├── id (PK, UUID)
├── userId (FK → Users)
├── tokenHash (SHA-256)
└── expiryTime (Long)

Posts (Domain)
├── id (PK, UUID)
├── userId (FK → Users) ← Ownership
├── title, content
└── createdAt, updatedAt

Symptoms (Domain)
├── id (PK, UUID)
├── userId (FK → Users) ← Ownership
├── symptomName, severity, notes
└── createdAt, updatedAt

Appointments (Domain)
├── id (PK, UUID)
├── userId (FK → Users) ← Patient
├── doctorId (FK → Users) ← Doctor
├── appointmentTime, reason, notes, status
└── createdAt, updatedAt
```

### Index Strategy
```
Users: email (UK), id (PK)
User_Roles: user_id (FK)
RefreshTokens: user_id (FK), (user_id, tokenHash) UK
Posts: user_id (FK)
Symptoms: user_id (FK)
Appointments: user_id (FK), doctor_id (FK)
```

### Data Ownership Pattern
```
Every domain entity has a userId field pointing to Users
Service layer validates:
  if (entity.userId != SecurityContext.userId) {
    throw new RuntimeException("Unauthorized")
  }
```

---

## 🔐 Security Architecture

### Authentication Model
```
┌──────────────────────────────────────────┐
│           JWT Authentication             │
├──────────────────────────────────────────┤
│ Access Token:   15 minutes, stateless    │
│ Refresh Token:  7 days, stored in DB     │
│ Algorithm:      HS512 (symmetric)        │
│ Claims:         userId, email, roles     │
│ Storage:        In-memory + DB hash      │
└──────────────────────────────────────────┘
```

### Authorization Model
```
┌──────────────────────────────────────────┐
│      Role-Based Access Control (RBAC)    │
├──────────────────────────────────────────┤
│ ROLE_PATIENT  → Access own data only    │
│ ROLE_DOCTOR   → Access patient data     │
│ ROLE_ADMIN    → Access all data         │
│                                          │
│ Implementation:                          │
│ - Method-level: @PreAuthorize           │
│ - Route-level: SecurityConfig.java      │
│ - Service-level: Ownership checks       │
└──────────────────────────────────────────┘
```

### Password Security
```
Plain Text Password → BCrypt Hashing → Database
                      ↓
                    $2a$10$...hash...
                    (Random salt included)
```

### Token Lifecycle
```
1. Generate (Login)
   ├─ Access Token: 15 min
   └─ Refresh Token: 7 days (hash stored in DB)

2. Use (Protected Request)
   ├─ Verify signature (HS512)
   ├─ Check expiry
   └─ Extract claims

3. Refresh
   ├─ Client sends refresh token
   ├─ Service validates hash in DB
   ├─ Generate new access token
   ├─ Optionally rotate refresh token
   └─ Delete old refresh token

4. Logout
   └─ Delete refresh token from DB
```

---

## 📊 Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      REST Client                             │
│                   (Frontend/Mobile)                          │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/JSON
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                  Controller Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ AuthController  UserController  PostController  ...  │  │
│  │  + Input validation                                 │  │
│  │  + Response formatting                              │  │
│  │  + Error handling                                   │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
              ┌──────────┴──────────┐
              ↓                     ↓
┌─────────────────────────┐  ┌──────────────────────────────┐
│ Security Layer          │  │ Service Layer               │
├─────────────────────────┤  ├──────────────────────────────┤
│ JwtAuthenticationFilter │  │ AuthService                 │
│ SecurityConfig          │  │ PostService                 │
│ JwtService              │  │ SymptomService              │
│ CustomUserDetailsService│  │ AppointmentService          │
│                         │  │ UserMapper                  │
│ + Token validation      │  │ + Business logic            │
│ + CORS handling         │  │ + Ownership checks          │
│ + Role authorization    │  │ + DTO mapping               │
└────────────┬────────────┘  └──────────┬───────────────────┘
             │                          │
             └──────────────┬───────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ UserRepository    PostRepository                      │  │
│  │ RefreshTokenRepository                               │  │
│  │ SymptomRepository AppointmentRepository              │  │
│  │ + Database queries (JPA)                             │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
         ┌───────────────────────────────┐
         │     Entity/Model Layer        │
         ├───────────────────────────────┤
         │ User  Role  RefreshToken      │
         │ Post  Symptom  Appointment    │
         │ (JPA Entities)                │
         └───────────────────────┬───────┘
                                 │
                                 ↓
         ┌───────────────────────────────┐
         │     Database Layer            │
         ├───────────────────────────────┤
         │ SQL (H2/PostgreSQL)           │
         │ Tables, indexes, constraints  │
         └───────────────────────────────┘
```

---

## 🔄 Design Patterns Used

### 1. **Layered Architecture**
- Clear separation of concerns
- Each layer has single responsibility
- Easy to test and maintain

### 2. **Repository Pattern**
- Abstract data access logic
- Testable with mock repositories
- Database-independent interface

### 3. **DTO Pattern**
- Decouple API from entity structure
- Version API independently
- Control what's exposed

### 4. **Service Locator Pattern**
- Spring dependency injection
- Loose coupling between layers
- Easy to swap implementations

### 5. **Filter Pattern**
- JwtAuthenticationFilter intercepts requests
- Security logic centralized
- Reusable across all endpoints

### 6. **Global Exception Handler**
- Centralized error handling
- Consistent error format
- Single place to update

### 7. **Builder Pattern**
- Lombok generates builders
- Fluent API for entity creation
- Easy test data construction

---

## 📈 Scalability Considerations

### Horizontal Scaling
```
Client Requests
    ↓
Load Balancer (nginx/HAProxy)
    ├→ Instance 1 (Spring Boot)
    ├→ Instance 2 (Spring Boot)
    ├→ Instance 3 (Spring Boot)
    └→ Instance N (Spring Boot)
    ↓
PostgreSQL Database (Replicated)
```

**Stateless Design Benefits:**
- No session affinity needed
- Any instance can serve any request
- Easy to add/remove instances
- Horizontal scaling without complexity

### Caching Strategy (Future Enhancement)
```
GET /api/v1/users/{id}
    ↓
Check Redis cache
    ├→ HIT: Return cached data
    └→ MISS: Query database
                ↓
             Cache result (TTL)
                ↓
             Return to client
```

### Database Optimization
```
Current:
- Connection pooling (HikariCP)
- Query optimization with indexes
- Lazy loading with FetchType

Future:
- Query result caching (Redis)
- Read replicas for scaling
- Sharding for large datasets
```

---

## 🧪 Testing Architecture

### Unit Testing
```
Service Layer Tests
    ├─ JwtServiceTest
    │  └─ Token generation/validation
    └─ AuthServiceTest
       └─ Login/register logic
```

### Integration Testing
```
Controller Layer Tests
    └─ AuthControllerTest
       └─ Full auth flow (controller → service → repo)
```

### Test Database
```
H2 In-Memory Database
    ├─ No external dependencies
    ├─ Fast test execution
    ├─ Automatic cleanup
    └─ File: application-test.properties
```

---

## 🚀 Deployment Architecture

### Development
```
Local Machine
    ├─ Spring Boot (mvn spring-boot:run)
    ├─ H2 In-Memory DB
    └─ IDE (IntelliJ IDEA)
```

### Production
```
Docker Container (kubernetes-ready)
    ├─ Java 21 JDK
    ├─ Spring Boot executable JAR
    └─ Exposed port 8080
    
PostgreSQL Container/Service
    ├─ Persistent volume
    ├─ Connection pooling
    └─ Replication support
```

### Environment Configuration
```
application.properties
    ├─ Default values (H2, localhost)
    └─ Environment variables override:
       
       DB_URL → jdbc:postgresql://prod-db:5432/shecare
       DB_USERNAME → prod_user
       DB_PASSWORD → encrypted_password
       JWT_SECRET → random_32_char_key
       etc.
```

---

## 📋 Configuration Management

### Environment Variables
```
Development:
  DB_URL=jdbc:h2:mem:shcaredb
  DB_DRIVER=org.h2.Driver
  SHOW_SQL=true

Production:
  DB_URL=jdbc:postgresql://db.example.com:5432/shecare
  DB_USERNAME=prod_user
  DB_PASSWORD=secure_password
  JWT_SECRET=random_secure_key_min_32_chars
  CORS_ALLOWED_ORIGINS=https://app.example.com
```

### Spring Profiles (Extensible)
```
application.properties       (base config)
application-dev.properties   (development)
application-prod.properties  (production)
application-test.properties  (testing)
```

---

## 🔗 External Integrations (Future Ready)

### Email Service
```
AuthService → EmailService
    ├─ Password reset
    ├─ Email verification
    └─ Notifications
```

### SMS/Notification Service
```
AppointmentService → NotificationService
    └─ Appointment reminders
```

### Payment Processing
```
(Future) PaymentController → PaymentService
    └─ Subscription billing
```

### ML/Analytics
```
PredictController → MLService
    └─ Health predictions
```

---

## 🎯 Architectural Principles

### 1. **Single Responsibility Principle**
- Each class has one reason to change
- Controllers handle HTTP, Services handle logic

### 2. **Dependency Inversion**
- Depend on abstractions (interfaces)
- Spring provides dependency injection

### 3. **Open/Closed Principle**
- Open for extension (add new services/controllers)
- Closed for modification (existing code stable)

### 4. **Interface Segregation**
- Repositories only expose needed methods
- Services only use required dependencies

### 5. **DRY (Don't Repeat Yourself)**
- Shared logic in base classes
- Utility methods in helpers
- DTOs for consistent response format

---

## 📚 Architecture Evolution

### Current State (v1.0)
- Monolithic Spring Boot application
- Single database
- Synchronous request/response

### Future Enhancements (v2.0+)
- Microservices decomposition
- Message queue (RabbitMQ/Kafka)
- API Gateway (Kong/AWS API Gateway)
- Caching layer (Redis)
- Search engine (Elasticsearch)
- Event sourcing & CQRS patterns

---

## 🔍 Code Organization Standards

```
me.psikuvit.shecare/
├── controller/          (HTTP entry points)
├── service/            (Business logic)
├── repository/         (Data access)
├── model/              (JPA entities)
├── dto/                (API contracts)
├── security/           (Auth & authz)
├── exception/          (Error handling)
└── SheCareApplication  (Entry point)

Resources:
├── application.properties  (Configuration)
└── db/migration/          (Flyway migrations)
```

---

## 📞 Architecture Decision Records (ADR)

### ADR-001: JWT vs Session-Based Auth
**Decision:** Use JWT  
**Rationale:** Stateless, scalable, mobile-friendly

### ADR-002: Layered Architecture
**Decision:** Controller → Service → Repository → Database  
**Rationale:** Clear separation, testable, maintainable

### ADR-003: DTO Pattern
**Decision:** Use DTOs for all API contracts  
**Rationale:** Decouple API from entity structure

### ADR-004: RBAC Authorization
**Decision:** 3-tier role system (PATIENT, DOCTOR, ADMIN)  
**Rationale:** Supports multi-user medical scenario

### ADR-005: Ownership Enforcement
**Decision:** Service-layer validation  
**Rationale:** Prevents data leakage bugs

---

**Architecture Version:** 1.0  
**Last Updated:** March 29, 2026  
**Status:** Production Ready

