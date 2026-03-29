package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.AppointmentRequest;
import me.psikuvit.shecare.dto.AppointmentResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Appointment;
import me.psikuvit.shecare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public AppointmentResponse createAppointment(String userId, AppointmentRequest request) {
        log.info("Creating appointment for user: {} with doctor: {}", userId, request.getDoctor());
        
        // Parse date and time to create appointmentTime
        String dateTimeStr = request.getDate() + "T" + request.getTime();
        LocalDate appointmentDate = LocalDate.parse(request.getDate(), DATE_FORMATTER);
        LocalDateTime appointmentTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        
        Appointment appointment = Appointment.builder()
                .userId(userId)
                .doctorName(request.getDoctor())
                .specialty(request.getSpecialty())
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .appointmentType(request.getAppointmentType() != null ? request.getAppointmentType() : "in-person")
                .build();
        
        appointment = appointmentRepository.save(appointment);
        return toAppointmentResponse(appointment);
    }
    
    public List<AppointmentResponse> getUserAppointments(String userId) {
        log.info("Getting appointments for user: {}", userId);
        return appointmentRepository.findByUserId(userId).stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentResponse> getDoctorAppointments(String doctorName) {
        log.info("Getting appointments for doctor: {}", doctorName);
        return appointmentRepository.findByDoctorName(doctorName).stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());
    }
    
    public AppointmentResponse getAppointment(String appointmentId) {
        log.info("Getting appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return toAppointmentResponse(appointment);
    }
    
    public AppointmentResponse updateAppointment(String userId, String appointmentId, AppointmentRequest request) {
        log.info("Updating appointment: {} for user: {}", appointmentId, userId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot update appointment of another user");
        }
        
        appointment.setDoctorName(request.getDoctor());
        appointment.setSpecialty(request.getSpecialty());
        
        // Parse date and time
        String dateTimeStr = request.getDate() + "T" + request.getTime();
        LocalDate appointmentDate = LocalDate.parse(request.getDate(), DATE_FORMATTER);
        LocalDateTime appointmentTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setAppointmentType(request.getAppointmentType() != null ? request.getAppointmentType() : "in-person");

        appointment = appointmentRepository.save(appointment);
        return toAppointmentResponse(appointment);
    }
    
    public void deleteAppointment(String userId, String appointmentId) {
        log.info("Deleting appointment: {} for user: {}", appointmentId, userId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Cannot delete appointment of another user");
        }
        
        appointmentRepository.delete(appointment);
    }
    
    private AppointmentResponse toAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUserId())
                .doctorName(appointment.getDoctorName())
                .specialty(appointment.getSpecialty())
                .date(appointment.getAppointmentDate().format(DATE_FORMATTER))
                .time(appointment.getAppointmentTime().format(TIME_FORMATTER))
                .appointmentType(appointment.getAppointmentType())
                .build();
    }
}

