package com.example.SaludClick.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SaludClick.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {
	List<Cita> findByPaciente_Email(String email);
    List<Cita> findByMedico_Email(String email);
}
