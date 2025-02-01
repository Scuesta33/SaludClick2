package com.example.SaludClick.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SaludClick.model.DisponibilidadMedico;

public interface DisponibilidadRepository extends JpaRepository<DisponibilidadMedico, Long> {
   //metodo para buscar disponibilidad por el id  
	List<DisponibilidadMedico> findByMedico_IdUsuario(Long idMedico);
    
}
