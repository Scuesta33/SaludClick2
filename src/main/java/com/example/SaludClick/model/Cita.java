package com.example.SaludClick.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
//entidad cita
@Entity
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idCita;

    @Column(nullable = false)
    private LocalDateTime fecha; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    @JsonIgnore 
    private Usuario paciente;

    @ManyToOne
    @JoinColumn(name = "id_medico", nullable = false)
    @JsonIgnore 
    private Usuario medico;
//diferentes estados para las citas con un enum
    public enum EstadoCita {
        PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA
    }
//constructores
    public Cita() {
        super();
    }

    public Cita(Long idCita, LocalDateTime fecha, EstadoCita estado, Usuario paciente, Usuario medico) {
        this.idCita = idCita;
        this.fecha = fecha;
        this.estado = estado;
        this.paciente = paciente;
        this.medico = medico;
    }
    
    public Cita(LocalDateTime fecha, EstadoCita estado, Usuario paciente, Usuario medico) {
        this.fecha = fecha;
        this.estado = estado;
        this.paciente = paciente;
        this.medico = medico;
    }


    public Long getIdCita() {
        return idCita;
    }

    public void setIdCita(Long idCita) {
        this.idCita = idCita;
    }

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

    public Usuario getPaciente() {
        return paciente;
    }

    public void setPaciente(Usuario paciente) {
        this.paciente = paciente;
    }

    public Usuario getMedico() {
        return medico;
    }

    public void setMedico(Usuario medico) {
        this.medico = medico;
    }
}