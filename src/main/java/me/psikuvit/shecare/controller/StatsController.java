package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {
    
    /**
     * Get user statistics
     * GET /api/v1/stats
     */
    @GetMapping
    public ResponseEntity<?> getStats() {
        log.info("Get stats endpoint called");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSymptoms", 0);
        stats.put("totalAppointments", 0);
        stats.put("wellnessScore", 0.0);

        return ResponseEntity.ok(stats);
    }
}

