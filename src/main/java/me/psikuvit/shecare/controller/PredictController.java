package me.psikuvit.shecare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PredictController {
    
    /**
     * Predict endpoint (public)
     * POST /api/v1/predict
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, Object> request) {
        log.info("Predict endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("prediction", "Sample prediction result");
        response.put("confidence", 0.85);
        response.put("message", "Prediction endpoint - implement your ML logic here");
        
        return ResponseEntity.ok(response);
    }
}

