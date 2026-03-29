package me.psikuvit.shecare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.AppointmentRequest;
import me.psikuvit.shecare.dto.AppointmentResponse;
import me.psikuvit.shecare.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    /**
     * Get user's appointments
     * GET /api/v1/appointments
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getUserAppointments() {
        log.info("Get appointments endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
    }
    
    /**
     * Create a new appointment
     * POST /api/v1/appointments
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        log.info("Create appointment endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        AppointmentResponse response = appointmentService.createAppointment(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get an appointment by ID
     * GET /api/v1/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable String id) {
        log.info("Get appointment endpoint called for id: {}", id);
        return ResponseEntity.ok(appointmentService.getAppointment(id));
    }
    
    /**
     * Update an appointment
     * PUT /api/v1/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable String id,
            @Valid @RequestBody AppointmentRequest request) {
        log.info("Update appointment endpoint called for id: {}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        AppointmentResponse response = appointmentService.updateAppointment(userId, id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete an appointment
     * DELETE /api/v1/appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        log.info("Delete appointment endpoint called for id: {}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        appointmentService.deleteAppointment(userId, id);
        return ResponseEntity.noContent().build();
    }
}

