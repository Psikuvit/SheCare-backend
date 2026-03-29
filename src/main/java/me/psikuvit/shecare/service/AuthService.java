package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.AuthResponse;
import me.psikuvit.shecare.dto.LoginRequest;
import me.psikuvit.shecare.dto.RegisterRequest;
import me.psikuvit.shecare.dto.RefreshTokenRequest;
import me.psikuvit.shecare.exception.AuthenticationException;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.exception.ValidationException;
import me.psikuvit.shecare.model.RefreshToken;
import me.psikuvit.shecare.model.Role;
import me.psikuvit.shecare.model.User;
import me.psikuvit.shecare.repository.RefreshTokenRepository;
import me.psikuvit.shecare.repository.UserRepository;
import me.psikuvit.shecare.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }
        
        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .enabled(true)
                .roles(new HashSet<>())
                .build();
        
        // Add default PATIENT role
        user.addRole(Role.ROLE_PATIENT);
        
        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Save refresh token
        saveRefreshToken(user.getId(), refreshToken);
        
        return buildAuthResponse(accessToken, refreshToken, user);
    }
    
    /**
     * Login user with email and password
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    return new AuthenticationException("Invalid email or password");
                });
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        if (!user.getEnabled()) {
            log.warn("User account is disabled: {}", request.getEmail());
            throw new AuthenticationException("User account is disabled");
        }
        
        log.info("User logged in successfully: {}", user.getId());
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Save refresh token
        saveRefreshToken(user.getId(), refreshToken);
        
        return buildAuthResponse(accessToken, refreshToken, user);
    }
    
    /**
     * Refresh access token
     */
    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");
        
        if (!jwtService.isTokenValid(request.getRefreshToken())) {
            log.warn("Invalid or expired refresh token");
            throw new AuthenticationException("Invalid or expired refresh token");
        }
        
        String userId = jwtService.extractUserId(request.getRefreshToken());
        String tokenHash = hashToken(request.getRefreshToken());
        
        RefreshToken storedToken = refreshTokenRepository.findByUserIdAndTokenHash(userId, tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in database for user: {}", userId);
                    return new AuthenticationException("Invalid refresh token");
                });
        
        if (storedToken.isExpired()) {
            log.warn("Refresh token is expired for user: {}", userId);
            refreshTokenRepository.delete(storedToken);
            throw new AuthenticationException("Refresh token is expired");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        // Delete old refresh token and save new one
        refreshTokenRepository.delete(storedToken);
        saveRefreshToken(user.getId(), newRefreshToken);
        
        log.info("Access token refreshed successfully for user: {}", userId);
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }
    
    /**
     * Logout user (invalidate refresh token)
     */
    public void logout(String userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
        log.info("User logged out successfully: {}", userId);
    }
    
    /**
     * Get current user info
     */
    public User getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    /**
     * Save refresh token
     */
    private void saveRefreshToken(String userId, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .tokenHash(hashToken(token))
                .expiryTime(System.currentTimeMillis() + 604800000L) // 7 days
                .build();
        
        refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Hash token for secure storage
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
    
    /**
     * Build auth response
     */
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L / 1000) // 15 minutes in seconds
                .user(UserMapper.toUserResponse(user))
                .build();
    }
}



