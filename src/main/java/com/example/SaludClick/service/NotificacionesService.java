package com.example.SaludClick.service;

import com.example.SaludClick.model.Notificacion;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;

import java.util.Date;
import java.util.List;

@Service
public class NotificacionesService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private EmailService emailService;

    public Notificacion enviarNotificacion(Usuario usuario, String asunto, String estado, String mensaje, Usuario destinatario) {
        System.out.println("Preparando notificación para el usuario: " + usuario.getEmail());

        if (usuario == null || destinatario == null) {
            System.out.println("Error: Usuario o destinatario es nulo.");
            return null; 
        }

        if (asunto == null || asunto.isEmpty()) {
            System.out.println("Advertencia: El asunto de la notificación está vacío.");
        }

        if (mensaje == null || mensaje.isEmpty()) {
            System.out.println("Advertencia: El mensaje de la notificación está vacío.");
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setAsunto(asunto);
        notificacion.setFechaEnvio(new Date());
        notificacion.setEstado(estado);
        notificacion.setMensaje(mensaje);
        notificacion.setDestinatario(destinatario);

        System.out.println("Guardando notificación en la base de datos...");
        Notificacion savedNotificacion = notificacionRepository.save(notificacion);

        try {
            System.out.println("Intentando enviar correo de notificación a: " + destinatario.getEmail());
            emailService.notificacionEmail(destinatario.getEmail(), asunto, mensaje);
            System.out.println("Correo de notificación enviado con éxito.");
        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo de notificación: " + e.getMessage());
        }

        return savedNotificacion;
    }

    public List<Notificacion> obtenerNotificacionesPorUsuario(Usuario usuario) {
        System.out.println("Buscando notificaciones enviadas por el usuario: " + usuario.getEmail());
        List<Notificacion> notificaciones = notificacionRepository.findByUsuario(usuario);

        if (notificaciones.isEmpty()) {
            System.out.println("El usuario no tiene notificaciones enviadas.");
        }

        return notificaciones;
    }

    public List<Notificacion> obtenerNotificacionesPorDestinatario(Usuario destinatario) {
        System.out.println("Buscando notificaciones para el destinatario: " + destinatario.getEmail());
        List<Notificacion> notificaciones = notificacionRepository.findByDestinatario(destinatario);

        if (notificaciones.isEmpty()) {
            System.out.println("El destinatario no tiene notificaciones.");
        }

        return notificaciones;
    }
}
