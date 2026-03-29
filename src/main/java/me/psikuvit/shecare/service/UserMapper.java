package me.psikuvit.shecare.service;

import me.psikuvit.shecare.dto.UserResponse;
import me.psikuvit.shecare.model.User;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class UserMapper {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatar(user.getAvatar())
                .treatmentDay(user.getTreatmentDay())
                .totalTreatmentDays(user.getTotalTreatmentDays())
                .wellnessScore(user.getWellnessScore())
                .nextAppointment(user.getNextAppointment() != null ? 
                        user.getNextAppointment().format(FORMATTER) : null)
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt() != null ? 
                        user.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(user.getUpdatedAt() != null ? 
                        user.getUpdatedAt().format(FORMATTER) : null)
                .roles(user.getRoles().stream()
                        .map(role -> role.name())
                        .collect(Collectors.toSet()))
                .build();
    }
}

