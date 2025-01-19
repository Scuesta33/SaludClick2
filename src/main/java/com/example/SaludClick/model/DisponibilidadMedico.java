package com.example.SaludClick.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalTime;
@Entity
public class DisponibilidadMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDisponibilidad;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "id_medico", nullable = false) 
    private Usuario medico;

    @Column(nullable = false) 
    private String diaSemana;

    @Column(nullable = false) 
    private LocalTime horaInicio;

    @Column(nullable = false) 
    private LocalTime horaFin;

    
    public DisponibilidadMedico() {
        super();
    }

    
    public DisponibilidadMedico(Long idDisponibilidad, Usuario medico, String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        super();
        this.idDisponibilidad = idDisponibilidad;
        this.medico = medico;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    
    public Long getIdDisponibilidad() {
        return idDisponibilidad;
    }

    public void setIdDisponibilidad(Long idDisponibilidad) {
        this.idDisponibilidad = idDisponibilidad;
    }

    public Usuario getMedico() {
        return medico;
    }

    public void setMedico(Usuario medico) {
        this.medico = medico;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}
