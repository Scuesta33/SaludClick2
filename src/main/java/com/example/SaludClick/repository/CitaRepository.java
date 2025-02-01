package com.example.SaludClick.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SaludClick.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {

// metodo para buscar citas por el email del paciente
	List<Cita> findByPaciente_Email(String email);
	// metodo para buscar citas por el email del medico
    List<Cita> findByMedico_Email(String email);
}
