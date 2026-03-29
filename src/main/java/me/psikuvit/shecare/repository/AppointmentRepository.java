package me.psikuvit.shecare.repository;

import me.psikuvit.shecare.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByUserId(String userId);

    List<Appointment> findByDoctorName(String doctorName);
}

