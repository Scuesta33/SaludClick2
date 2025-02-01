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
        System.out.println("Guardando nueva cita en la base de datos...");
        Cita citaGuardada = citaRepository.save(cita);
        return citaGuardada;
    }

    @Override
    public Optional<Cita> obtenerCitaPorId(Long id) {
        System.out.println("Buscando cita con ID: " + id);
        Optional<Cita> citaOpt = citaRepository.findById(id);

        if (!citaOpt.isPresent()) {
            System.out.println("Advertencia: No se encontró ninguna cita con el ID " + id);
        }

        return citaOpt;
    }

    @Override
    public List<Cita> listarCitas() {
        System.out.println("Recuperando todas las citas...");
        List<Cita> citas = citaRepository.findAll();

        if (citas.isEmpty()) {
            System.out.println("No hay citas registradas en la base de datos.");
        }

        return citas;
    }

    @Override
    public Cita actualizarCita(Cita cita) {
        System.out.println("Actualizando información de la cita con ID: " + cita.getIdCita());
        return citaRepository.save(cita);
    }

    @Override
    public void eliminarCita(Long id) {
        System.out.println("Intentando eliminar cita con ID: " + id);

        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (!citaOpt.isPresent()) {
            System.out.println("Error: No se encontró ninguna cita con el ID " + id);
            return;
        }

        try {
            citaRepository.deleteById(id);
            System.out.println("Cita eliminada exitosamente.");
        } catch (Exception e) {
            System.out.println("Error al eliminar la cita: " + e.getMessage());
        }
    }

    @Override
    public List<Cita> listarCitasPorPaciente(String emailPaciente) {
        System.out.println("Buscando citas del paciente con email: " + emailPaciente);
        List<Cita> citas = citaRepository.findByPaciente_Email(emailPaciente);

        if (citas.isEmpty()) {
            System.out.println("El paciente " + emailPaciente + " no tiene citas registradas.");
        }

        return citas;
    }

    @Override
    public List<Cita> listarCitasPorMedico(String emailMedico) {
        System.out.println("Buscando citas del médico con email: " + emailMedico);
        List<Cita> citas = citaRepository.findByMedico_Email(emailMedico);

        if (citas.isEmpty()) {
            System.out.println("El médico " + emailMedico + " no tiene citas asignadas.");
        }

        return citas;
    }
}
