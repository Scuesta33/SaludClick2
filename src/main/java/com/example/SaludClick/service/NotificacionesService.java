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
        if (usuario == null || destinatario == null) {
            System.out.println("usuario o destinatario es nulo :(");
            return null; 
        }
        if (asunto == null || asunto.isEmpty()) {
            System.out.println("el asunto de la notificación está vacío :(");
        }
        if (mensaje == null || mensaje.isEmpty()) {
            System.out.println("el mensaje de la notificación está vacío :(");
        }
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setAsunto(asunto);
        notificacion.setFechaEnvio(new Date());
        notificacion.setEstado(estado);
        notificacion.setMensaje(mensaje);
        notificacion.setDestinatario(destinatario);
        Notificacion savedNotificacion = notificacionRepository.save(notificacion);
        try {
            emailService.notificacionEmail(destinatario.getEmail(), asunto, mensaje);
            System.out.println("correo de notificación enviado con éxito :)");
        } catch (MessagingException e) {
            System.out.println("error al enviar el correo de notificación: " + e.getMessage());
        }
        return savedNotificacion;
    }

    
    public List<Notificacion> obtenerNotificacionesPorUsuario(Usuario usuario) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuario(usuario);
        if (notificaciones.isEmpty()) {
            System.out.println("el usuario no tiene notificaciones enviadas :(");
        }
        return notificaciones;
    }

    
    public List<Notificacion> obtenerNotificacionesPorDestinatario(Usuario destinatario) {
        List<Notificacion> notificaciones = notificacionRepository.findByDestinatario(destinatario);
        if (notificaciones.isEmpty()) {
            System.out.println("el destinatario no tiene notificaciones.");
        }
        return notificaciones;
    }
}
