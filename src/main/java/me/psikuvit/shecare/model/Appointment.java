package me.psikuvit.shecare.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String doctorName;
    
    @Column(nullable = false)
    private String specialty;
    
    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;
    
    @Column(nullable = false)
    @Builder.Default
    private String appointmentType = "in-person"; // in-person, teleconsultation
}

