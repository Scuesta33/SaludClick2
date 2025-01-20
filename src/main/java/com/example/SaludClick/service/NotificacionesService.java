
package com.example.SaludClick.service;

import com.example.SaludClick.model.Notificacion;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificacionesService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    public Notificacion enviarNotificacion(Usuario usuario, String tipoNotificacion, String estado, String mensaje, Usuario destinatario) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setFechaEnvio(new Date());
        notificacion.setEstado(estado);
        notificacion.setMensaje(mensaje);
        notificacion.setDestinatario(destinatario);
        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> obtenerNotificacionesPorUsuario(Usuario usuario) {
        return notificacionRepository.findByUsuario(usuario);
    }

    public List<Notificacion> obtenerNotificacionesPorDestinatario(Usuario destinatario) {
        return notificacionRepository.findByDestinatario(destinatario);
    }
}
