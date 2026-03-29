package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.UserResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Role;
import me.psikuvit.shecare.model.User;
import me.psikuvit.shecare.repository.UserRepository;
import me.psikuvit.shecare.service.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserRepository userRepository;
    
    /**
     * Get all users (admin only)
     * GET /api/v1/admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Get all users endpoint called");
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
     * Update user roles (admin only)
     * PATCH /api/v1/admin/users/{userId}/roles
     */
    @PatchMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRoles(
            @PathVariable String userId,
            @RequestBody Map<String, List<String>> request) {
        log.info("Update user roles endpoint called for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<String> rolesFromRequest = request.get("roles");
        if (rolesFromRequest != null) {
            user.getRoles().clear();
            final User finalUser = user;
            rolesFromRequest.forEach(roleName -> {
                try {
                    Role role = Role.valueOf(roleName);
                    finalUser.addRole(role);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role: {}", roleName);
                }
            });
            user = userRepository.save(user);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User roles updated successfully");
        response.put("user", UserMapper.toUserResponse(user));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable/Disable user (admin only)
     * PATCH /api/v1/admin/users/{userId}/status
     */
    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Map<String, Boolean> request) {
        log.info("Update user status endpoint called for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Boolean enabled = request.get("enabled");
        if (enabled != null) {
            user.setEnabled(enabled);
            user = userRepository.save(user);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User status updated successfully");
        response.put("user", UserMapper.toUserResponse(user));
        
        return ResponseEntity.ok(response);
    }
}



