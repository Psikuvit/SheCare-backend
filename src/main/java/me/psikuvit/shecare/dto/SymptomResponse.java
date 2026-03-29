package me.psikuvit.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomResponse {
    private String id;
    private String userId;
    private String symptomName;
    private Integer severity;
    private String notes;
    private String createdAt;
    private String updatedAt;
}

