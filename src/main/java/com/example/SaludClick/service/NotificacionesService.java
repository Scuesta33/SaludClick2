
package com.example.SaludClick.service;

import com.example.SaludClick.model.Notificacion;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@Service
public class NotificacionesService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(NotificacionesService.class);

    public Notificacion enviarNotificacion(Usuario usuario, String asunto, String estado, String mensaje, Usuario destinatario) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setAsunto(asunto);
        notificacion.setFechaEnvio(new Date());
        notificacion.setEstado(estado);
        notificacion.setMensaje(mensaje);
        notificacion.setDestinatario(destinatario);

        Notificacion savedNotificacion = notificacionRepository.save(notificacion);

        try {
            emailService.sendNotificacionEmail(destinatario.getEmail(), asunto, mensaje);
            logger.info("Correo de notificación enviado a: " + destinatario.getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar el correo de notificación a " + destinatario.getEmail(), e);
        }

        return savedNotificacion;
    }

    public List<Notificacion> obtenerNotificacionesPorUsuario(Usuario usuario) {
        return notificacionRepository.findByUsuario(usuario);
    }

    public List<Notificacion> obtenerNotificacionesPorDestinatario(Usuario destinatario) {
        return notificacionRepository.findByDestinatario(destinatario);
    }
}
