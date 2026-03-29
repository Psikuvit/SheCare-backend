package me.psikuvit.shecare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomRequest {
    
    @NotBlank(message = "Symptom name is required")
    @Size(min = 2, max = 100, message = "Symptom name must be between 2 and 100 characters")
    @JsonProperty("name")
    private String symptomName;
    
    @NotNull(message = "Severity is required")
    @Min(value = 1, message = "Severity must be between 1 and 10")
    @Max(value = 10, message = "Severity must be between 1 and 10")
    private Integer severity;
}

