package me.psikuvit.shecare.controller;

import me.psikuvit.shecare.dto.LoginRequest;
import me.psikuvit.shecare.dto.RegisterRequest;
import me.psikuvit.shecare.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class AuthControllerTest {
    
    @Autowired
    private AuthService authService;
    
    @Test
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .name("New User")
                .build();
        
        var response = authService.register(request);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
    
    @Test
    void testLoginSuccess() {
        // First register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("logintest@example.com")
                .password("Password123!")
                .name("Login Test")
                .build();
        
        authService.register(registerRequest);
        
        // Then login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("logintest@example.com")
                .password("Password123!")
                .build();
        
        var response = authService.login(loginRequest);
        assertNotNull(response.getAccessToken());
    }
}


