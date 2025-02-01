
package com.example.SaludClick.model;

import java.util.Date;
import jakarta.persistence.*;
//entidad notificacion, quiero dejar claro que estan todos los contructores porque no me funcionaba lombok
@Entity
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_cita")
    private Cita cita;

    @Column(nullable = false)
	private String asunto;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String mensaje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_destinatario", nullable = false)
    private Usuario destinatario;

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

	

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
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

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Usuario getDestinatario() {
		return destinatario;
	}

	public void setDestinatario(Usuario destinatario) {
		this.destinatario = destinatario;
	}

    
}
