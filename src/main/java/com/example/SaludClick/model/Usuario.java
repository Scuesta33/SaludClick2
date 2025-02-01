package com.example.SaludClick.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
// entidad usuario
@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    private String contrasena;  
    private String telefono;   
    private String direccion;  
    private Boolean activo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    public enum Rol {
        PACIENTE, MEDICO
    }
// los json ignore se usan para evitar que se serialicen en los json
    @OneToMany(mappedBy = "paciente")
    @JsonIgnore
    private List<Cita> citasComoPaciente;

    @OneToMany(mappedBy = "medico")
    @JsonIgnore
    private List<Cita> citasComoMedico;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Notificacion> notificaciones;

	public Usuario() {
		super();
	}

	public Usuario(Long idUsuario, String nombre, String email, String contrasena, String telefono, String direccion,
			Boolean activo, Rol rol, List<Cita> citasComoPaciente, List<Cita> citasComoMedico,
			List<Notificacion> notificaciones) {
		super();
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.email = email;
		this.contrasena = contrasena;
		this.telefono = telefono;
		this.direccion = direccion;
		this.activo = activo;
		this.rol = rol;
		this.citasComoPaciente = citasComoPaciente;
		this.citasComoMedico = citasComoMedico;
		this.notificaciones = notificaciones;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public List<Cita> getCitasComoPaciente() {
		return citasComoPaciente;
	}

	public void setCitasComoPaciente(List<Cita> citasComoPaciente) {
		this.citasComoPaciente = citasComoPaciente;
	}

	public List<Cita> getCitasComoMedico() {
		return citasComoMedico;
	}

	public void setCitasComoMedico(List<Cita> citasComoMedico) {
		this.citasComoMedico = citasComoMedico;
	}

	public List<Notificacion> getNotificaciones() {
		return notificaciones;
	}

	public void setNotificaciones(List<Notificacion> notificaciones) {
		this.notificaciones = notificaciones;
	}
    
    
}