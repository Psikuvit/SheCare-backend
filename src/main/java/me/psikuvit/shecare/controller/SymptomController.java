package me.psikuvit.shecare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.SymptomRequest;
import me.psikuvit.shecare.dto.SymptomResponse;
import me.psikuvit.shecare.service.SymptomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/symptoms")
@RequiredArgsConstructor
@Slf4j
public class SymptomController {
    
    private final SymptomService symptomService;
    
    /**
     * Get user's symptoms
     * GET /api/v1/symptoms
     */
    @GetMapping
    public ResponseEntity<List<SymptomResponse>> getUserSymptoms() {
        log.info("Get symptoms endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(symptomService.getUserSymptoms(userId));
    }
    
    /**
     * Create a new symptom
     * POST /api/v1/symptoms
     */
    @PostMapping
    public ResponseEntity<SymptomResponse> createSymptom(@Valid @RequestBody SymptomRequest request) {
        log.info("Create symptom endpoint called");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SymptomResponse response = symptomService.createSymptom(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get a symptom by ID
     * GET /api/v1/symptoms/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SymptomResponse> getSymptom(@PathVariable String id) {
        log.info("Get symptom endpoint called for id: {}", id);
        return ResponseEntity.ok(symptomService.getSymptom(id));
    }
    
    /**
     * Update a symptom
     * PUT /api/v1/symptoms/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SymptomResponse> updateSymptom(
            @PathVariable String id,
            @Valid @RequestBody SymptomRequest request) {
        log.info("Update symptom endpoint called for id: {}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SymptomResponse response = symptomService.updateSymptom(userId, id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a symptom
     * DELETE /api/v1/symptoms/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSymptom(@PathVariable String id) {
        log.info("Delete symptom endpoint called for id: {}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        symptomService.deleteSymptom(userId, id);
        return ResponseEntity.noContent().build();
    }
}

