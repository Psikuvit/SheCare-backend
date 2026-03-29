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
    
    @NotBlank(message = "Doctor ID is required")
    private String doctorId;
    
    @NotBlank(message = "Doctor name is required")
    private String doctorName;
    
    @NotBlank(message = "Specialty is required")
    private String specialty;
    
    @NotBlank(message = "Date is required")
    private String date;
    
    @NotBlank(message = "Time is required")
    private String time;
    
    @Pattern(regexp = "in-person|teleconsultation", message = "Type must be either 'in-person' or 'teleconsultation'")
    @JsonProperty("type")
    private String appointmentType;
    
    @Size(max = 200, message = "Reason must not exceed 200 characters")
    private String reason;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}

