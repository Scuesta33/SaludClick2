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
public class HistorialCita {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idHistorial;

  @ManyToOne 
  @JoinColumn(name = "id_cita", nullable = false) 
  private Cita cita;

  @ManyToOne 
  @JoinColumn(name = "id_usuario", nullable = false) 
  private Usuario usuario;

  @Temporal(TemporalType.TIMESTAMP)
  private Date fechaHora;

  private String estado;

  @Column(length = 500) 
  private String notas;

  // Default constructor
  public HistorialCita() {
    super();
  }

  
  public HistorialCita(Long idHistorial, Cita cita, Usuario usuario, Date fechaHora, String estado, String notas) {
    super();
    this.idHistorial = idHistorial;
    this.cita = cita;
    this.usuario = usuario;
    this.fechaHora = fechaHora;
    this.estado = estado;
    this.notas = notas;
  }

  
  public Long getIdHistorial() {
    return idHistorial;
  }

  public void setIdHistorial(Long idHistorial) {
    this.idHistorial = idHistorial;
  }

  public Cita getCita() {
    return cita;
  }

  public void setCita(Cita cita) {
    this.cita = cita;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Date getFechaHora() {
    return fechaHora;
  }

  public void setFechaHora(Date fechaHora) {
    this.fechaHora = fechaHora;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getNotas() {
    return notas;
  }

  public void setNotas(String notas) {
    this.notas = notas;
  }
}