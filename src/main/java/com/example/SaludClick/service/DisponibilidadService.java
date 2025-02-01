package com.example.SaludClick.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SaludClick.model.DisponibilidadMedico;
import com.example.SaludClick.repository.DisponibilidadRepository;

@Service
public class DisponibilidadService {
    
    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    public DisponibilidadMedico crearDisponibilidad(DisponibilidadMedico disponibilidadMedico) {
        System.out.println("Intentando guardar disponibilidad en la BD...");
        
        if (disponibilidadMedico == null) {
            System.out.println("Error: La disponibilidad recibida es nula.");
            return null; 
        }

        DisponibilidadMedico guardada = disponibilidadRepository.save(disponibilidadMedico);
        System.out.println("Disponibilidad guardada correctamente.");
        return guardada;
    }

    public List<DisponibilidadMedico> obtenerDisponibilidadPorMedico(Long idMedico) {
        System.out.println("Buscando disponibilidad para el médico con ID: " + idMedico);

        if (idMedico == null || idMedico <= 0) {
            System.out.println("Error: ID de médico inválido.");
            return List.of(); // En lugar de lanzar una excepción, devuelve una lista vacía
        }

        List<DisponibilidadMedico> disponibilidades = disponibilidadRepository.findByMedico_IdUsuario(idMedico);

        if (disponibilidades.isEmpty()) {
            System.out.println("No hay disponibilidades registradas para este médico.");
        }

        return disponibilidades;
    }

    public List<DisponibilidadMedico> obtenerTodasLasDisponibilidades() {
        System.out.println("Obteniendo todas las disponibilidades...");
        List<DisponibilidadMedico> disponibilidades = disponibilidadRepository.findAll();

        if (disponibilidades.isEmpty()) {
            System.out.println("Advertencia: No hay disponibilidades en la base de datos.");
        }

        return disponibilidades;
    }

    public Optional<DisponibilidadMedico> obtenerDisponibilidadPorId(Long idDisponibilidad) {
        System.out.println("Buscando disponibilidad con ID: " + idDisponibilidad);

        if (idDisponibilidad == null || idDisponibilidad <= 0) {
            System.out.println("Error: ID de disponibilidad inválido.");
            return Optional.empty();
        }

        Optional<DisponibilidadMedico> disponibilidadOpt = disponibilidadRepository.findById(idDisponibilidad);

        if (!disponibilidadOpt.isPresent()) {
            System.out.println("No se encontró ninguna disponibilidad con ID: " + idDisponibilidad);
        }

        return disponibilidadOpt;
    }

    public void eliminarDisponibilidad(Long idDisponibilidad) {
        System.out.println("Intentando eliminar disponibilidad con ID: " + idDisponibilidad);

        if (idDisponibilidad == null || idDisponibilidad <= 0) {
            System.out.println("Error: ID de disponibilidad inválido.");
            return;
        }

        Optional<DisponibilidadMedico> disponibilidadOpt = disponibilidadRepository.findById(idDisponibilidad);
        
        if (!disponibilidadOpt.isPresent()) {
            System.out.println("Advertencia: No se encontró disponibilidad con ese ID.");
            return;
        }

        try {
            disponibilidadRepository.deleteById(idDisponibilidad);
            System.out.println("Disponibilidad eliminada correctamente.");
        } catch (Exception e) {
            System.out.println("Error al eliminar la disponibilidad: " + e.getMessage());
        }
    }
}
