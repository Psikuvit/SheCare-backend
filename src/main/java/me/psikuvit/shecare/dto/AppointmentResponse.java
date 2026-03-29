package me.psikuvit.shecare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private String id;
    
    @JsonProperty("doctor")
    private String doctorName;
    
    private String specialty;
    
    private String date;
    
    private String time;
    
    @JsonProperty("type")
    private String appointmentType;
    
    private String reason;
    
    private String notes;
    
    private String status;
}

