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

    @ManyToOne(optional = false) // Ensures this field is mandatory
    @JoinColumn(name = "id_medico", nullable = false) // Foreign key to Usuario
    private Usuario medico;

    @Column(nullable = false) // Ensures the day of the week is mandatory
    private String diaSemana;

    @Column(nullable = false) // Ensures the start time is mandatory
    private LocalTime horaInicio;

    @Column(nullable = false) // Ensures the end time is mandatory
    private LocalTime horaFin;

    // Default constructor
    public DisponibilidadMedico() {
        super();
    }

    // Constructor with fields
    public DisponibilidadMedico(Long idDisponibilidad, Usuario medico, String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        super();
        this.idDisponibilidad = idDisponibilidad;
        this.medico = medico;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    // Getters and Setters
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
