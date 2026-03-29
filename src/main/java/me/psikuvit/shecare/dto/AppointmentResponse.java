package me.psikuvit.shecare.dto;

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
    private String userId;
    private String doctorId;
    private String appointmentTime;
    private String reason;
    private String notes;
    private String status;
    private String createdAt;
    private String updatedAt;
}

