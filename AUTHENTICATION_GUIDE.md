# SheCare Authentication & Authorization Guide

Complete guide to authentication, authorization, and security implementation in the SheCare backend.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [JWT Implementation](#jwt-implementation)
3. [Role-Based Access Control](#role-based-access-control)
4. [Data Ownership & Multi-Tenancy](#data-ownership--multi-tenancy)
5. [API Authentication Flow](#api-authentication-flow)
6. [Security Best Practices](#security-best-practices)
7. [Troubleshooting](#troubleshooting)

## Architecture Overview

### Components

```
User Request
    ↓
JwtAuthenticationFilter (Extract & Validate Token)
    ↓
SecurityContext (Set Authentication)
    ↓
@PreAuthorize / Role Checks
    ↓
Service Layer (Ownership Validation)
    ↓
Repository (Data Access)
    ↓
Response
```

### Security Layers

1. **Token Layer** - JWT validation & signature verification
2. **Authentication Layer** - User identity verification
3. **Authorization Layer** - Role-based access control
4. **Data Layer** - Ownership enforcement in service layer

## JWT Implementation

### Token Generation

**Location:** `SecurityConfig.java`, `JwtService.java`

```java
// Access Token (15 min)
String accessToken = jwtService.generateAccessToken(user);

// Refresh Token (7 days)
String refreshToken = jwtService.generateRefreshToken(user);
```

### JWT Structure

**Header:**
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "name": "User Name",
  "roles": ["ROLE_PATIENT"],
  "iat": 1704067200,
  "exp": 1704068100
}
```

**Signature:** HS512(header + payload, secret)

### Configuration

**application.properties:**
```properties
# 15 minutes in milliseconds
jwt.access-token-expiry=900000

# 7 days in milliseconds
jwt.refresh-token-expiry=604800000

# Tolerance for clock drift (seconds)
jwt.clock-skew=60

# Secret key (min 32 chars)
jwt.secret=${JWT_SECRET:your-secret-key-here}
```

### Token Validation

**JwtService.java:**
```java
public boolean isTokenValid(String token) {
    try {
        Jwts.parser()
            .clockSkewSeconds((int) clockSkew)
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
        return true;
    } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
        return false;
    }
}
```

**Validation Checks:**
- Signature is valid (matches secret)
- Token not expired
- Supported JWT format
- All claims present

## Role-Based Access Control

### Roles

Three roles are supported:

```java
public enum Role {
    ROLE_PATIENT,   // Regular users
    ROLE_DOCTOR,    // Healthcare providers
    ROLE_ADMIN      // System administrators
}
```

### Method-Level Security

**Using @PreAuthorize:**

```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<UserResponse>> getAllUsers() {
    // Only admin can access
}

@PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
public ResponseEntity<List<AppointmentResponse>> getAppointments() {
    // Doctors and admins can access
}

@PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
public ResponseEntity<UserResponse> getCurrentUser() {
    // Any authenticated user
}
```

### Endpoint-Level Security

**SecurityConfig.java:**

```java
.authorizeHttpRequests(authz -> authz
    // Public endpoints
    .requestMatchers("/api/v1/auth/register").permitAll()
    .requestMatchers("/api/v1/auth/login").permitAll()
    .requestMatchers("/api/v1/auth/refresh").permitAll()
    
    // Authenticated users
    .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
    
    // Admin only
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    
    // Default: require authentication
    .anyRequest().authenticated()
)
```

## Data Ownership & Multi-Tenancy

### Service Layer Validation

All CRUD operations validate ownership before executing.

**Example - SymptomService.java:**

```java
public SymptomResponse updateSymptom(String userId, String symptomId, SymptomRequest request) {
    Symptom symptom = symptomRepository.findById(symptomId)
        .orElseThrow(() -> new ResourceNotFoundException("Symptom not found"));
    
    // CRITICAL: Verify ownership
    if (!symptom.getUserId().equals(userId)) {
        throw new ForbiddenException("You cannot modify this symptom");
    }
    
    // Safe to update
    symptom.setSymptomName(request.getSymptomName());
    symptom.setSeverity(request.getSeverity());
    return SymptomMapper.toResponse(symptomRepository.save(symptom));
}
```

### Database Constraints

**Schema enforces data isolation:**

```sql
-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    -- ...
);

-- Symptoms belong to single user
CREATE TABLE symptoms (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    -- ...
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexed for performance
CREATE INDEX idx_symptoms_user_id ON symptoms(user_id);
```

### Cascade Delete

When user deleted:
- All posts deleted
- All symptoms deleted
- All appointments deleted (where user is patient)
- All refresh tokens invalidated

## API Authentication Flow

### 1. Registration

```
POST /api/v1/auth/register
├─ Validate password strength
├─ Hash password with BCrypt
├─ Create user with ROLE_PATIENT
├─ Generate access token (15 min)
├─ Generate refresh token (7 days)
├─ Save refresh token hash to DB
└─ Return tokens + user profile
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "new@example.com",
    "password": "SecurePass123!",
    "name": "John Doe"
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
    "id": "uuid",
    "email": "new@example.com",
    "name": "John Doe",
    "roles": ["ROLE_PATIENT"],
    "enabled": true,
    "createdAt": "2026-03-29T15:30:00"
  }
}
```

### 2. Login

```
POST /api/v1/auth/login
├─ Find user by email
├─ Verify password with BCrypt
├─ Check user enabled
├─ Generate tokens
└─ Return tokens + user profile
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@shecare.com",
    "password": "Password123!"
  }'
```

### 3. Use Token

```
GET /api/v1/users/me
Header: Authorization: Bearer eyJhbGc...
├─ JwtAuthenticationFilter extracts token
├─ Validates signature & expiry
├─ Extracts user ID from token
├─ Sets SecurityContext
├─ @PreAuthorize checks pass
└─ Service layer executes
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGc..."
```

### 4. Token Refresh

```
POST /api/v1/auth/refresh
├─ Validate refresh token signature
├─ Extract user ID from token
├─ Hash token & lookup in database
├─ Verify not expired
├─ Generate new access token
├─ Optionally rotate refresh token
└─ Return new tokens
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGc..."
  }'
```

### 5. Logout

```
POST /api/v1/auth/logout
├─ Extract user ID from security context
├─ Delete all refresh tokens for user
└─ Return 204 No Content
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGc..."
```

**Effect:** Invalidates all refresh tokens. User must login again.

## Security Best Practices

### Password Hashing

**BCrypt Configuration:**

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**During Registration:**
```java
User user = User.builder()
    .email(request.getEmail())
    .passwordHash(passwordEncoder.encode(request.getPassword()))
    .build();
```

**Verification:**
```java
if (!passwordEncoder.matches(inputPassword, user.getPasswordHash())) {
    throw new AuthenticationException("Invalid password");
}
```

**Properties:**
- Strength: 10 (default, ~10 iterations)
- Algorithm: BCRYPT
- Hash length: 60 characters

### Refresh Token Management

**Token Hashing (SHA-256):**

```java
private String hashToken(String token) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hash);
}
```

**Why hash?** Even if database compromised, tokens are hashed (not reversible).

**Database Storage:**
```sql
CREATE TABLE refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expiry_time BIGINT NOT NULL
);
```

**Token Rotation (Optional):**
- Each refresh invalidates old token
- New token issued
- Prevents token replay attacks

### CORS Configuration

**Allowed Origins:**
```yaml
spring.web.cors.allowed-origins: http://localhost:5173,http://127.0.0.1:5173
```

**Allowed Methods:**
```yaml
spring.web.cors.allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
```

**Credentials:**
```yaml
spring.web.cors.allow-credentials: true
```

### Session Security

**Stateless Sessions:**
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

**Why stateless?**
- No server-side session storage
- Horizontal scaling
- JWT tokens are self-contained
- Better for microservices

### CSRF Protection

**Disabled for API** (stateless + token-based):
```java
.csrf(csrf -> csrf.disable())
```

**For SPAs:** CSRF is not needed with proper CORS & token storage.

### Exception Handling

**Custom Entry Point (401):**
```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((request, response, authException) -> {
        response.setStatus(401);
        response.getWriter().write("""
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required"
            }
        """);
    })
)
```

**Access Denied (403):**
```java
.accessDeniedHandler((request, response, accessDeniedException) -> {
    response.setStatus(403);
    response.getWriter().write("""
        {
            "status": 403,
            "error": "Forbidden",
            "message": "Insufficient permissions"
        }
    """);
})
```

## Troubleshooting

### Issue: Token Invalid/Expired

**Symptoms:**
```
401 Unauthorized
"Invalid or expired token"
```

**Causes:**
1. Token expired (15 min for access token)
2. Wrong secret key
3. Clock drift (> 60 sec)
4. Corrupted token

**Solutions:**
```bash
# Refresh token
POST /api/v1/auth/refresh
{
  "refreshToken": "..."
}

# Or login again
POST /api/v1/auth/login
```

### Issue: Cannot Access Protected Endpoint

**Symptoms:**
```
401 Unauthorized
"Authentication required"
```

**Cause:** Missing or invalid Authorization header

**Solution:**
```bash
# Add Authorization header
curl -H "Authorization: Bearer TOKEN"

# Token format: "Bearer " + access token (with space)
```

### Issue: Access Denied (403)

**Symptoms:**
```
403 Forbidden
"Access denied"
```

**Cause:** Insufficient role or ownership violation

**Solutions:**
- Check user role: `GET /api/v1/users/me` → `roles`
- Verify resource ownership (own posts/symptoms/appointments only)
- Admin? Must have `ROLE_ADMIN`

### Issue: Wrong Password Accepted

**Symptoms:**
- Login succeeds with wrong password
- Or valid password fails

**Causes:**
1. User account doesn't exist (wrong email)
2. Password changed after registration
3. Database migration failed (password hash empty)

**Solutions:**
```bash
# Check user exists
SELECT * FROM users WHERE email = 'test@example.com';

# Verify password hash exists (60 chars)
SELECT LENGTH(password_hash) FROM users;

# Reset via seed data (dev only)
mvn flyway:clean flyway:migrate
```

### Issue: CORS Error

**Symptoms:**
```
Access to XMLHttpRequest blocked by CORS policy
```

**Frontend Console:**
```
No 'Access-Control-Allow-Origin' header
```

**Solution:**
```bash
# Check allowed origins
set CORS_ALLOWED_ORIGINS=http://localhost:5173

# Restart server
mvn spring-boot:run
```

### Issue: Refresh Token Expired

**Symptoms:**
```
401 Unauthorized
"Invalid or expired refresh token"
```

**Solution:**
```bash
# Must login again
POST /api/v1/auth/login
```

**Note:** Refresh tokens expire after 7 days.

### Issue: JWT Secret Too Short

**Error:**
```
IllegalArgumentException: The key argument cannot be empty
```

**Cause:** JWT secret < 32 characters

**Solution:**
```bash
# Generate 32-character secret
set JWT_SECRET=your-secret-key-min-32-chars-here

# Restart server
```

### Issue: Seeded Users Not Found

**Symptoms:**
```
404 User Not Found
login with admin@shecare.com fails
```

**Solution:**
```bash
# Run migrations
mvn flyway:clean flyway:migrate

# Verify users created
SELECT email, name FROM users;

# Test login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@shecare.com",
    "password": "Password123!"
  }'
```

## Advanced Topics

### Custom Claims in JWT

**Add custom claims:**
```java
Map<String, Object> claims = new HashMap<>();
claims.put("email", user.getEmail());
claims.put("roles", user.getRoles().stream().map(Role::name).collect(toSet()));
claims.put("custom_claim", "value");

return Jwts.builder()
    .subject(user.getId())
    .claims(claims)
    .issuedAt(now)
    .expiration(expiry)
    .signWith(key, SignatureAlgorithm.HS512)
    .compact();
```

**Extract custom claims:**
```java
public String extractCustomClaim(String token) {
    return extractClaim(token, claims -> claims.get("custom_claim", String.class));
}
```

### Token Blacklist Implementation

**If needing immediate token invalidation:**

```java
@Entity
public class TokenBlacklist {
    @Id
    private String id;
    private String tokenHash;
    private Long expiryTime;
}

// Check before processing request
if (tokenBlacklistRepository.existsByTokenHash(tokenHash)) {
    throw new AuthenticationException("Token revoked");
}
```

### API Gateway Integration

**For microservices architecture:**

```
Client → API Gateway (verify JWT) → Microservice (services)
```

Gateway validates token, extracts user ID, adds to request header.

### Multi-Tenancy

**Current approach:**
- Single tenant (user owns their data)
- Scope by `userId` in all tables

**For true multi-tenancy:**
```java
@Entity
public class User {
    @Column
    private String organizationId;  // Tenant identifier
    // ...
}

// Filter all queries by organizationId
```

---

**Last Updated:** March 29, 2026  
**Version:** 1.0.0  
**Maintainer:** SheCare Team

