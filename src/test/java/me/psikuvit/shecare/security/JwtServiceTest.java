package me.psikuvit.shecare.security;

import me.psikuvit.shecare.model.Role;
import me.psikuvit.shecare.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {
    
    @Autowired
    private JwtService jwtService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440003")
                .email("test@example.com")
                .name("Test User")
                .passwordHash("hashed_password")
                .enabled(true)
                .roles(new HashSet<>(Set.of(Role.ROLE_PATIENT)))
                .build();
    }
    
    @Test
    void testGenerateAccessToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testExtractUserId() {
        String token = jwtService.generateAccessToken(testUser);
        String userId = jwtService.extractUserId(token);
        assertEquals(testUser.getId(), userId);
    }
    
    @Test
    void testExtractEmail() {
        String token = jwtService.generateAccessToken(testUser);
        String email = jwtService.extractEmail(token);
        assertEquals(testUser.getEmail(), email);
    }
    
    @Test
    void testIsTokenValid() {
        String token = jwtService.generateAccessToken(testUser);
        assertTrue(jwtService.isTokenValid(token));
    }
    
    @Test
    void testExtractRoles() {
        String token = jwtService.generateAccessToken(testUser);
        Set<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_PATIENT"));
    }
}

