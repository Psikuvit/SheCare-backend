package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.UserResponse;
import me.psikuvit.shecare.service.AuthService;
import me.psikuvit.shecare.service.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final AuthService authService;
    
    /**
     * Get current user profile
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Get current user profile endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(UserMapper.toUserResponse(user));
    }
}

