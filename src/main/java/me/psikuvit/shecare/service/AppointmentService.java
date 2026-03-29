package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.AppointmentRequest;
import me.psikuvit.shecare.dto.AppointmentResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Appointment;
import me.psikuvit.shecare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

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
        log.info("Creating appointment for user: {} with doctor: {}", userId, request.getDoctorId());
        
        // Parse date and time to create appointmentTime
        String dateTimeStr = request.getDate() + "T" + request.getTime();
        LocalDateTime appointmentTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        
        Appointment appointment = Appointment.builder()
                .userId(userId)
                .doctorId(request.getDoctorId())
                .doctorName(request.getDoctorName())
                .specialty(request.getSpecialty())
                .appointmentTime(appointmentTime)
                .appointmentType(request.getAppointmentType() != null ? request.getAppointmentType() : "in-person")
                .reason(request.getReason())
                .notes(request.getNotes())
                .status("SCHEDULED")
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
    
    public List<AppointmentResponse> getDoctorAppointments(String doctorId) {
        log.info("Getting appointments for doctor: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId).stream()
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
        
        appointment.setDoctorId(request.getDoctorId());
        appointment.setDoctorName(request.getDoctorName());
        appointment.setSpecialty(request.getSpecialty());
        
        // Parse date and time
        String dateTimeStr = request.getDate() + "T" + request.getTime();
        LocalDateTime appointmentTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        appointment.setAppointmentTime(appointmentTime);
        
        appointment.setAppointmentType(request.getAppointmentType() != null ? request.getAppointmentType() : "in-person");
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        
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
                .doctorName(appointment.getDoctorName())
                .specialty(appointment.getSpecialty())
                .date(appointment.getAppointmentTime().format(DATE_FORMATTER))
                .time(appointment.getAppointmentTime().format(TIME_FORMATTER))
                .appointmentType(appointment.getAppointmentType())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus())
                .build();
    }
}

