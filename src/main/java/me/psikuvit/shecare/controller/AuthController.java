package me.psikuvit.shecare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.AuthResponse;
import me.psikuvit.shecare.dto.LoginRequest;
import me.psikuvit.shecare.dto.RegisterRequest;
import me.psikuvit.shecare.dto.RefreshTokenRequest;
import me.psikuvit.shecare.service.AuthService;
import me.psikuvit.shecare.service.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Register a new user
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register endpoint called for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Login user
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login endpoint called for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refresh access token
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token endpoint called");
        AuthResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout user
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("Logout endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get current user info
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        log.info("Get current user endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(UserMapper.toUserResponse(user));
    }
}

