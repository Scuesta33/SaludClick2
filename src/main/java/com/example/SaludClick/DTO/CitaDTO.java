package com.example.SaludClick.DTO;

import java.time.LocalDateTime;

import com.example.SaludClick.model.Cita.EstadoCita;

public class CitaDTO {
    private LocalDateTime fecha;
    private EstadoCita estado;
    private String medicoNombre; // Solo el email del médico
	public LocalDateTime getFecha() {
		return fecha;
	}
	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}
	public EstadoCita getEstado() {
		return estado;
	}
	public void setEstado(EstadoCita estado) {
		this.estado = estado;
	}
	public String getMedicoNombre() {
		return medicoNombre;
	}
	public void setMedicoNombre(String medicoNombre) {
		this.medicoNombre = medicoNombre;
	}
	

    // Getters y setters
    
}
