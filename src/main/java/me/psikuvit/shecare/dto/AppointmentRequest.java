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
public class AppointmentRequest {
    
    @NotBlank(message = "Doctor name is required")
    private String doctor;
    
    @NotBlank(message = "Specialty is required")
    private String specialty;
    
    @NotBlank(message = "Date is required")
    private String date;
    
    @NotBlank(message = "Time is required")
    private String time;
    
    @Pattern(regexp = "in-person|teleconsultation", message = "Type must be either 'in-person' or 'teleconsultation'")
    @JsonProperty("type")
    private String appointmentType;
}

