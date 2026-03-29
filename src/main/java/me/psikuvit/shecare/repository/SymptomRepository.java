package me.psikuvit.shecare.repository;

import me.psikuvit.shecare.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, String> {
    List<Symptom> findByUserId(String userId);
}

