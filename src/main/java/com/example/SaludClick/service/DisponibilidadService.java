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
        if (disponibilidadMedico == null) {
            System.out.println("la disponibilidad recibida es nula :(");
            return null; 
        }

        DisponibilidadMedico guardada = disponibilidadRepository.save(disponibilidadMedico);
        System.out.println("disponibilidad guardada :)");
        return guardada;
    }

    public List<DisponibilidadMedico> obtenerDisponibilidadPorMedico(Long idMedico) {
        if (idMedico == null || idMedico <= 0) {
            System.out.println("ID del médico inválido.");
            return List.of(); 
        }

        List<DisponibilidadMedico> disponibilidades = disponibilidadRepository.findByMedico_IdUsuario(idMedico);

        if (disponibilidades.isEmpty()) {
            System.out.println("No hay disponibilidades registradas para este médico :(");
        }

        return disponibilidades;
    }

    public List<DisponibilidadMedico> obtenerTodasLasDisponibilidades() {
        List<DisponibilidadMedico> disponibilidades = disponibilidadRepository.findAll();

        if (disponibilidades.isEmpty()) {
            System.out.println("no hay disponibilidades :(");
        }

        return disponibilidades;
    }

    public Optional<DisponibilidadMedico> obtenerDisponibilidadPorId(Long idDisponibilidad) {
        if (idDisponibilidad == null || idDisponibilidad <= 0) {
            System.out.println("ID de disponibilidad inválido");
            return Optional.empty();
        }

        Optional<DisponibilidadMedico> disponibilidadOpt = disponibilidadRepository.findById(idDisponibilidad);

        if (!disponibilidadOpt.isPresent()) {
            System.out.println("no se encontró ninguna disponibilidad con ID: " + idDisponibilidad);
        }

        return disponibilidadOpt;
    }

    public void eliminarDisponibilidad(Long idDisponibilidad) {
        if (idDisponibilidad == null || idDisponibilidad <= 0) {
            System.out.println("ID de disponibilidad inválido.");
            return;
        }

        Optional<DisponibilidadMedico> disponibilidadOpt = disponibilidadRepository.findById(idDisponibilidad);
        
        if (!disponibilidadOpt.isPresent()) {
            System.out.println("no se encontró disponibilidad con ese ID.");
            return;
        }

        try {
            disponibilidadRepository.deleteById(idDisponibilidad);
            System.out.println("disponibilidad eliminada correctamente :)");
        } catch (Exception e) {
            System.out.println("error al eliminar la disponibilidad: " + e.getMessage());
        }
    }
}
