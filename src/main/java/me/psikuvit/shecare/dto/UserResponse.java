package me.psikuvit.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private String id;
    private String email;
    private String name;
    private String avatar;
    private Integer treatmentDay;
    private Integer totalTreatmentDays;
    private Double wellnessScore;
    private String nextAppointment;
    private Boolean enabled;
    private String createdAt;
    private String updatedAt;
    private Set<String> roles;
}

