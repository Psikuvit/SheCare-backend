package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.SymptomRequest;
import me.psikuvit.shecare.dto.SymptomResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Symptom;
import me.psikuvit.shecare.repository.SymptomRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SymptomService {
    
    private final SymptomRepository symptomRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public SymptomResponse createSymptom(String userId, SymptomRequest request) {
        log.info("Creating symptom for user: {}", userId);
        
        Symptom symptom = Symptom.builder()
                .userId(userId)
                .symptomName(request.getSymptomName())
                .severity(request.getSeverity())
                .build();
        
        symptom = symptomRepository.save(symptom);
        return toSymptomResponse(symptom);
    }
    
    public List<SymptomResponse> getUserSymptoms(String userId) {
        log.info("Getting symptoms for user: {}", userId);
        return symptomRepository.findByUserId(userId).stream()
                .map(this::toSymptomResponse)
                .collect(Collectors.toList());
    }
    
    public SymptomResponse getSymptom(String symptomId) {
        log.info("Getting symptom: {}", symptomId);
        Symptom symptom = symptomRepository.findById(symptomId)
                .orElseThrow(() -> new ResourceNotFoundException("Symptom not found"));
        return toSymptomResponse(symptom);
    }
    
    public SymptomResponse updateSymptom(String userId, String symptomId, SymptomRequest request) {
        log.info("Updating symptom: {} for user: {}", symptomId, userId);
        
        Symptom symptom = symptomRepository.findById(symptomId)
                .orElseThrow(() -> new ResourceNotFoundException("Symptom not found"));
        
        if (!symptom.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot update symptom of another user");
        }
        
        symptom.setSymptomName(request.getSymptomName());
        symptom.setSeverity(request.getSeverity());
        
        symptom = symptomRepository.save(symptom);
        return toSymptomResponse(symptom);
    }
    
    public void deleteSymptom(String userId, String symptomId) {
        log.info("Deleting symptom: {} for user: {}", symptomId, userId);
        
        Symptom symptom = symptomRepository.findById(symptomId)
                .orElseThrow(() -> new ResourceNotFoundException("Symptom not found"));
        
        if (!symptom.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot delete symptom of another user");
        }
        
        symptomRepository.delete(symptom);
    }
    
    private SymptomResponse toSymptomResponse(Symptom symptom) {
        return SymptomResponse.builder()
                .id(symptom.getId())
                .symptomName(symptom.getSymptomName())
                .severity(symptom.getSeverity())
                .createdAt(symptom.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }
}

