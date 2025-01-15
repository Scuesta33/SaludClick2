package com.example.SaludClick.DTO;

import java.time.LocalDateTime;

import com.example.SaludClick.model.Cita.EstadoCita;

public class CitaDTO {
	private Long id;
	private LocalDateTime fecha;
    private EstadoCita estado;
    private String medicoNombre; // Solo el email del médico
    private String pacienteNombre; // Solo el email del paciente
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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPacienteNombre() {
		return pacienteNombre;
	}
	public void setPacienteNombre(String pacienteNombre) {
		this.pacienteNombre = pacienteNombre;
	}
	

    // Getters y setters
    
}
