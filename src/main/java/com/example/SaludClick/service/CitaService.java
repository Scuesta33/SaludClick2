package com.example.SaludClick.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SaludClick.model.Cita;
import com.example.SaludClick.repository.CitaRepository;

@Service
public class CitaService implements ICitaService {

    private final CitaRepository citaRepository;

    @Autowired
    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    
    @Override
    public Cita crearCita(Cita cita) {
        Cita citaGuardada = citaRepository.save(cita);
        return citaGuardada;
    }

    
    @Override
    public Optional<Cita> obtenerCitaPorId(Long id) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (!citaOpt.isPresent()) {
            System.out.println("no se encontró ninguna cita con el ID " + id);
        }
       return citaOpt;
    }

    
    @Override
    public List<Cita> listarCitas() {
        List<Cita> citas = citaRepository.findAll();
        if (citas.isEmpty()) {
            System.out.println("no hay citas registradas :(");
        }
        return citas;
    }

    
    @Override
    public Cita actualizarCita(Cita cita) {
        return citaRepository.save(cita);
    }

    
    @Override
    public void eliminarCita(Long id) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (!citaOpt.isPresent()) {
            System.out.println("no se encontró ninguna cita con el ID " + id);
            return;
        }
        try {
            citaRepository.deleteById(id);
            System.out.println("Cita eliminada :)");
        } catch (Exception e) {
            System.out.println("error al eliminar la cita: " + e.getMessage());
        }
    }

    
    @Override
    public List<Cita> listarCitasPorPaciente(String emailPaciente) {
        List<Cita> citas = citaRepository.findByPaciente_Email(emailPaciente);
        if (citas.isEmpty()) {
            System.out.println("el paciente " + emailPaciente + " no tiene citas registradas");
        }
        return citas;
    }

    
    @Override
    public List<Cita> listarCitasPorMedico(String emailMedico) {
        List<Cita> citas = citaRepository.findByMedico_Email(emailMedico);
        if (citas.isEmpty()) {
            System.out.println("el médico " + emailMedico + " no tiene citas asignadas.");
        }
        return citas;
    }
}
