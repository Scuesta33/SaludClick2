package com.example.SaludClick.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @ManyToOne(optional = false) // Ensures this field is mandatory
    @JoinColumn(name = "id_usuario", nullable = false) // Foreign key to Usuario
    private Usuario usuario;

    @ManyToOne // Allows null values if a notification is not directly related to a Cita
    @JoinColumn(name = "id_cita") // Foreign key to Cita
    private Cita cita;

    @Column(nullable = false) // Ensures the notification type is mandatory
    private String tipoNotificacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio;

    @Column(nullable = false) 
    private String estado;

    // Default constructor
    public Notificacion() {
        super();
    }

    // Constructor with fields
    public Notificacion(Long idNotificacion, Usuario usuario, Cita cita, String tipoNotificacion, Date fechaEnvio,
                        String estado) {
        super();
        this.idNotificacion = idNotificacion;
        this.usuario = usuario;
        this.cita = cita;
        this.tipoNotificacion = tipoNotificacion;
        this.fechaEnvio = fechaEnvio;
        this.estado = estado;
    }

    // Getters and Setters
    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(String tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}