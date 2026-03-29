package me.psikuvit.shecare.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.model.Role;
import me.psikuvit.shecare.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {
    
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;
    private final long clockSkew;
    
    private final SecretKey key;
    
    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry,
            @Value("${jwt.clock-skew:60}") long clockSkew
    ) {
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.clockSkew = clockSkew;
        // Ensure the secret is at least 64 bytes (512 bits) for HS512
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        if (decodedKey.length < 64) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 64 bytes (512 bits) when Base64 decoded. " +
                    "Current length: " + decodedKey.length + " bytes. " +
                    "Generate a new one using: Keys.secretKeyFor(SignatureAlgorithm.HS512)"
            );
        }
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }
    
    /**
     * Generate access token (short-lived)
     */
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiry);
    }
    
    /**
     * Generate refresh token (long-lived)
     */
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiry);
    }
    
    private String generateToken(User user, long expiryMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("roles", user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet()));
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMillis);
        
        return Jwts.builder()
                .subject(user.getId())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }
    
    /**
     * Extract roles from token
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Object rolesObj = extractClaim(token, claims -> claims.get("roles"));
        if (rolesObj instanceof Set) {
            return (Set<String>) rolesObj;
        } else if (rolesObj instanceof Collection) {
            return new HashSet<>((Collection<String>) rolesObj);
        }
        return new HashSet<>();
    }
    
    /**
     * Check if token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .clockSkewSeconds((int) clockSkew)
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
    
    /**
     * Extract a specific claim from token
     */
    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Get token expiry time in milliseconds from now
     */
    public long getTokenExpiryIn(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.getTime() - System.currentTimeMillis();
    }
}

