package com.example.SaludClick.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SaludClick.model.DisponibilidadMedico;
import com.example.SaludClick.repository.DisponibilidadRepository;

@Service
public class DisponibilidadService {
	 @Autowired
	    private DisponibilidadRepository disponibilidadRepository;

	    public DisponibilidadMedico crearDisponibilidad(DisponibilidadMedico disponibilidadMedico) {
	        return disponibilidadRepository.save(disponibilidadMedico);
	    }

	    public List<DisponibilidadMedico> obtenerDisponibilidadPorMedico(Long idUsuario) {
	        return disponibilidadRepository.findByMedico_IdUsuario(idUsuario);
	    }
}
