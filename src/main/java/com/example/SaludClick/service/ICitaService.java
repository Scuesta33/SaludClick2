package com.example.SaludClick.service;



	import java.util.List;
	import java.util.Optional;

import com.example.SaludClick.model.Cita;

	

	public interface ICitaService {
		Cita crearCita(Cita cita);
	    Optional<Cita> obtenerCitaPorId(Long id);
	    List<Cita> listarCitas();
	    Cita actualizarCita(Cita cita);
	    void eliminarCita(Long id);
	}

