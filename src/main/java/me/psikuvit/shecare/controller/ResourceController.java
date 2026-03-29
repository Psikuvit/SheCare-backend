package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {
    
    /**
     * Get resources (public endpoint)
     * GET /api/v1/resources
     */
    @GetMapping
    public ResponseEntity<?> getResources() {
        log.info("Get resources endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to SheCare Resources");
        response.put("resources", new String[]{"Articles", "Videos", "FAQs"});
        return ResponseEntity.ok(response);
    }
}

