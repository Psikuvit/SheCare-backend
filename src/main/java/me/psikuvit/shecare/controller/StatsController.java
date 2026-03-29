package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.StatResponse;
import me.psikuvit.shecare.service.AppointmentService;
import me.psikuvit.shecare.service.SymptomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final SymptomService symptomService;
    private final AppointmentService appointmentService;

    
    /**
     * Get user statistics
     * GET /api/v1/stats
     */
    @GetMapping
    public ResponseEntity<List<StatResponse>> getStats() {
        log.info("Get stats endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<StatResponse> stats = new ArrayList<>();
        
        // Total Symptoms
        stats.add(StatResponse.builder()
                .label("Total Symptoms")
                .value(symptomService.getUserSymptoms(userId).size())
                .icon("activity")
                .build());
        
        // Total Appointments
        stats.add(StatResponse.builder()
                .label("Total Appointments")
                .value(appointmentService.getUserAppointments(userId).size())
                .icon("calendar")
                .build());
        
        // Wellness Score
        stats.add(StatResponse.builder()
                .label("Wellness Score")
                .value(75)
                .change("+5%")
                .icon("heart")
                .build());
        
        // Treatment Progress
        stats.add(StatResponse.builder()
                .label("Treatment Progress")
                .value("60%")
                .icon("zap")
                .build());

        return ResponseEntity.ok(stats);
    }
}



