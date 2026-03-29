package me.psikuvit.shecare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.psikuvit.shecare.dto.AppointmentRequest;
import me.psikuvit.shecare.dto.AppointmentResponse;
import me.psikuvit.shecare.exception.ResourceNotFoundException;
import me.psikuvit.shecare.model.Appointment;
import me.psikuvit.shecare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public AppointmentResponse createAppointment(String userId, AppointmentRequest request) {
        log.info("Creating appointment for user: {} with doctor: {}", userId, request.getDoctorId());
        
        Appointment appointment = Appointment.builder()
                .userId(userId)
                .doctorId(request.getDoctorId())
                .appointmentTime(request.getAppointmentTime())
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
        appointment.setAppointmentTime(request.getAppointmentTime());
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
                .userId(appointment.getUserId())
                .doctorId(appointment.getDoctorId())
                .appointmentTime(appointment.getAppointmentTime().format(FORMATTER))
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt().format(FORMATTER))
                .updatedAt(appointment.getUpdatedAt().format(FORMATTER))
                .build();
    }
}

