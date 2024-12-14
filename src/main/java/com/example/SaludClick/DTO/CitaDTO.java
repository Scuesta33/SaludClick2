package com.example.SaludClick.DTO;

import java.time.LocalDateTime;

import com.example.SaludClick.model.Cita.EstadoCita;

public class CitaDTO {
    private LocalDateTime fecha;
    private EstadoCita estado;
    private String medicoEmail; // Solo el email del médico
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
	public String getMedicoEmail() {
		return medicoEmail;
	}
	public void setMedicoEmail(String medicoEmail) {
		this.medicoEmail = medicoEmail;
	}

    // Getters y setters
    
}
