# SheCare Backend - Production-Grade Authentication & Authorization

A comprehensive Spring Boot 3.x backend for the SheCare women's health platform with JWT-based authentication, role-based access control, and data ownership enforcement.

## 📋 Project Structure

```
SheCare/
├── src/
│   ├── main/
│   │   ├── java/me/psikuvit/shecare/
│   │   │   ├── controller/           # REST API endpoints
│   │   │   ├── service/              # Business logic
│   │   │   ├── repository/           # Data access layer
│   │   │   ├── model/                # JPA entities
│   │   │   ├── dto/                  # Data transfer objects
│   │   │   ├── security/             # JWT & auth config
│   │   │   ├── exception/            # Custom exceptions & handlers
│   │   │   └── SheCareApplication.java
│   │   ├── resources/
│   │   │   ├── application.yml       # Configuration
│   │   │   └── db/migration/         # Flyway migrations
│   │   └── test/
│   │       └── java/me/psikuvit/shecare/
│   │           ├── security/         # JWT tests
│   │           └── controller/       # Integration tests
│   └── pom.xml                       # Maven dependencies
├── README.md                          # This file
└── HELP.md
```

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+ (for production) or H2 (for development)
- Git

### Setup & Run

1. **Clone & Navigate**
   ```bash
   cd C:\Users\twins\IdeaProjects\SheCare
   ```

2. **Install Dependencies**
   ```bash
   mvn clean install
   ```

3. **Run Application**
   ```bash
   mvn spring-boot:run
   ```
   
   The server starts on `http://localhost:8080`

4. **Access H2 Console (Dev)**
   ```
   http://localhost:8080/h2-console
   JDBC URL: jdbc:h2:mem:shcaredb
   ```

## 🔐 Authentication Flow

### 1. Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "SecurePass123!",
    "name": "Jane Patient"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid-...",
    "email": "patient@example.com",
    "name": "Jane Patient",
    "roles": ["ROLE_PATIENT"],
    "enabled": true,
    "createdAt": "2026-03-29T14:30:00"
  }
}
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "SecurePass123!"
  }'
```

### 3. Use Access Token
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <accessToken>"
```

### 4. Refresh Access Token (when expired)
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refreshToken>"
  }'
```

### 5. Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <accessToken>"
```

## 👥 Default Seeded Users

After running migrations, three default users are created:

| Email | Password | Role |
|-------|----------|------|
| admin@shecare.com | Password123! | ROLE_ADMIN |
| doctor@shecare.com | Password123! | ROLE_DOCTOR |
| patient@shecare.com | Password123! | ROLE_PATIENT |

**Note:** Change these in production! See [Environment Configuration](#🔧-environment-configuration) section.

## 📡 API Endpoints

### Authentication (Public)
- `POST /api/v1/auth/register` - Create account
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token
- `POST /api/v1/auth/logout` - Logout (authenticated)
- `GET /api/v1/auth/me` - Current user (authenticated)

### Users (Authenticated)
- `GET /api/v1/users/me` - Get profile

### Posts (Authenticated)
- `GET /api/v1/posts` - Get all posts
- `POST /api/v1/posts` - Create post (own only)
- `GET /api/v1/posts/{id}` - Get post
- `PUT /api/v1/posts/{id}` - Update post (own only)
- `DELETE /api/v1/posts/{id}` - Delete post (own only)

### Symptoms (Authenticated)
- `GET /api/v1/symptoms` - Get user's symptoms
- `POST /api/v1/symptoms` - Create symptom
- `GET /api/v1/symptoms/{id}` - Get symptom
- `PUT /api/v1/symptoms/{id}` - Update symptom (own only)
- `DELETE /api/v1/symptoms/{id}` - Delete symptom (own only)

### Appointments (Authenticated)
- `GET /api/v1/appointments` - Get user's appointments
- `POST /api/v1/appointments` - Create appointment
- `GET /api/v1/appointments/{id}` - Get appointment
- `PUT /api/v1/appointments/{id}` - Update appointment (own only)
- `DELETE /api/v1/appointments/{id}` - Delete appointment (own only)

### Public Endpoints
- `GET /api/v1/resources` - Get resources
- `POST /api/v1/predict` - Prediction (ML placeholder)

### Admin Only
- `GET /api/v1/admin/users` - Get all users
- `PATCH /api/v1/admin/users/{userId}/roles` - Update roles
- `PATCH /api/v1/admin/users/{userId}/status` - Enable/disable user

## 🔑 Security Features

### JWT (JSON Web Tokens)
- **Access Token TTL:** 15 minutes (900 seconds)
- **Refresh Token TTL:** 7 days
- **Algorithm:** HS512
- **Claims:** user ID, email, roles
- **Clock Skew:** 60 seconds (tolerance for time drift)

### Password Policy
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (@$!%*?&)

Example valid: `SecurePass123!`

### CORS Configuration
- Allowed Origins: `http://localhost:5173`, `http://127.0.0.1:5173`
- Allowed Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Allow Credentials: ✓

### Data Ownership
- Users can only access their own posts, symptoms, appointments
- Service layer validates ownership before CRUD operations
- 401 Unauthorized for missing token
- 403 Forbidden for ownership violations

## 🔧 Environment Configuration

### Development (Default - H2)
Uses in-memory H2 database. Set in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:shcaredb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
```

### Production (PostgreSQL)
Create `.env` or set environment variables:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/shecare
DB_USERNAME=shecare_user
DB_PASSWORD=SecurePassword123!
DB_DRIVER=org.postgresql.Driver

# Security
JWT_SECRET=your-secret-key-minimum-32-characters-long
JWT_ACCESS_EXPIRY=900000
JWT_REFRESH_EXPIRY=604800000

# CORS
CORS_ALLOWED_ORIGINS=https://shecare.example.com,https://app.shecare.example.com

# Server
SERVER_PORT=8080
SHOW_SQL=false
```

**Add to `application.yml`:**
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: ${DB_DRIVER}
```

### Generate Strong JWT Secret
```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell (Windows)
[Convert]::ToBase64String((1..32 | ForEach-Object { [byte](Get-Random -Minimum 0 -Maximum 256) }))
```

## 📦 Database Schema

### Users
- **id** (UUID PK)
- **email** (UNIQUE)
- **passwordHash** (BCrypt)
- **name**, **avatar**
- **treatmentDay**, **totalTreatmentDays**, **wellnessScore**, **nextAppointment**
- **enabled** (soft delete via boolean)
- **createdAt**, **updatedAt** (timestamps)

### User Roles (Many-to-Many)
- **user_id** (FK)
- **role** (ENUM: ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN)

### Refresh Tokens
- **id** (UUID PK)
- **userId** (FK)
- **tokenHash** (SHA-256)
- **expiryTime** (long milliseconds)

### Posts, Symptoms, Appointments
- All linked to **userId** (FK)
- Soft delete via status column (appointments)
- Indexed for query performance

See `src/main/resources/db/migration/V1__initial_schema.sql` for full DDL.

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=JwtServiceTest
```

### Test Coverage
Tests include:
- JWT token generation/validation
- User registration/login
- Token refresh flow
- Invalid credentials handling
- Expired token rejection
- Ownership restrictions

## 🚨 Error Handling

All errors return consistent JSON response:

```json
{
  "timestamp": "2026-03-29T14:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

### Status Codes
- **200 OK** - Success
- **201 Created** - Resource created
- **204 No Content** - Successful delete
- **400 Bad Request** - Validation error
- **401 Unauthorized** - Auth required / invalid credentials
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

### Validation Errors
```json
{
  "status": 400,
  "error": "Validation Failed",
  "validationErrors": {
    "email": "Email must be valid",
    "password": "Password must contain..."
  }
}
```

## 🐳 Docker Setup (Optional)

### Docker Compose (PostgreSQL + App)
```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: shecare
      POSTGRES_USER: shecare_user
      POSTGRES_PASSWORD: SecurePassword123!
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  shecare-api:
    build: .
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/shecare
      DB_USERNAME: shecare_user
      DB_PASSWORD: SecurePassword123!
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      - postgres

volumes:
  postgres_data:
```

Build & run:
```bash
docker-compose up -d
```

## 📚 Technology Stack

- **Framework:** Spring Boot 3.x
- **Security:** Spring Security 6 + JWT (JJWT 0.12.3)
- **Database:** JPA/Hibernate, Flyway migrations
- **Validation:** Jakarta Bean Validation
- **Password Hashing:** BCrypt
- **Testing:** JUnit 5, Mockito
- **Build:** Maven
- **Logging:** SLF4J

## 🔄 Request/Response Examples

### Create Symptom
```bash
POST /api/v1/symptoms
Content-Type: application/json
Authorization: Bearer <token>

{
  "symptomName": "Headache",
  "severity": 7,
  "notes": "Mild tension headache"
}
```

**Response (201):**
```json
{
  "id": "uuid-...",
  "userId": "uuid-...",
  "symptomName": "Headache",
  "severity": 7,
  "notes": "Mild tension headache",
  "createdAt": "2026-03-29T14:30:00",
  "updatedAt": "2026-03-29T14:30:00"
}
```

### Create Appointment
```bash
POST /api/v1/appointments
Content-Type: application/json
Authorization: Bearer <token>

{
  "doctorId": "550e8400-e29b-41d4-a716-446655440002",
  "appointmentTime": "2026-04-15T10:30:00",
  "reason": "Regular checkup",
  "notes": "Annual physical"
}
```

## 📖 Logging Configuration

View logs with level control in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    me.psikuvit.shecare: DEBUG
    org.springframework.security: DEBUG
```

## 🤝 Integration with Frontend

Frontend should:

1. **Store tokens** (secure http-only cookies recommended)
2. **Attach token** to requests:
   ```javascript
   headers: {
     'Authorization': `Bearer ${accessToken}`
   }
   ```
3. **Handle 401:** Redirect to login / refresh token
4. **Handle 403:** Show "Access Denied" message
5. **Refresh token** before expiry or on 401 response

### Vue.js Example
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err.response?.status === 401) {
      // Refresh token or redirect to login
    }
    return Promise.reject(err);
  }
);
```

## 🚀 Production Deployment Checklist

- [ ] Change default JWT secret (minimum 32 characters)
- [ ] Change default seeded user passwords
- [ ] Enable HTTPS/TLS
- [ ] Set `spring.jpa.hibernate.ddl-auto: validate` (never `create-drop`)
- [ ] Configure PostgreSQL (not H2) for production
- [ ] Set `SHOW_SQL: false`
- [ ] Configure proper CORS origins (not `*`)
- [ ] Enable request logging & monitoring
- [ ] Set up database backups
- [ ] Configure email for password reset (not yet implemented)
- [ ] Add rate limiting for auth endpoints
- [ ] Set up SSL certificates
- [ ] Enable HSTS headers
- [ ] Implement audit logging

## 📝 Roadmap

- [ ] Email verification on registration
- [ ] Password reset flow
- [ ] Two-factor authentication (2FA)
- [ ] Audit logging
- [ ] Rate limiting
- [ ] OAuth2 integration (Google, GitHub)
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Performance optimization (caching, pagination)
- [ ] Multi-tenancy support

## 🐛 Troubleshooting

### Flyway Migration Fails
```bash
# Clear and reset migrations (dev only)
mvn flyway:clean flyway:migrate
```

### JWT Token Invalid
- Check JWT_SECRET matches between token generation and verification
- Verify token not expired (15 min for access token)
- Ensure clock sync between servers

### CORS Issues
- Check `CORS_ALLOWED_ORIGINS` includes your frontend URL
- Ensure credentials mode is set on frontend

### H2 Console Access Denied
- H2 console only works in development mode (`H2_CONSOLE_ENABLED: true`)
- Disabled automatically in production

## 📧 Support

For issues, create a GitHub issue or contact the development team.

## 📄 License

This project is licensed under the MIT License.

---

**Last Updated:** March 29, 2026
**Version:** 1.0.0
**Status:** Production Ready ✅

