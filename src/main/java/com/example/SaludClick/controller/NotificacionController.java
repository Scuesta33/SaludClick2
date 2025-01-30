
package com.example.SaludClick.controller;

import com.example.SaludClick.model.Notificacion;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.NotificacionesService;
import com.example.SaludClick.service.UsuarioServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionesService notificacionesService;

    @Autowired
    private UsuarioServiceImp usuarioServiceImp;


@PostMapping("/enviar")
public ResponseEntity<?> enviarNotificacion(@RequestParam String asunto, @RequestParam String estado, @RequestParam String mensaje, @RequestParam String destinatarioNombre) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());
        List<Usuario> destinatarios = usuarioServiceImp.buscarPorNombre(destinatarioNombre);

        if (usuarioOpt.isPresent() && !destinatarios.isEmpty()) {
            Usuario usuario = usuarioOpt.get();
            Usuario destinatario = destinatarios.get(0); // Assuming the first match is the intended recipient
            Notificacion notificacion = notificacionesService.enviarNotificacion(usuario, asunto, estado, mensaje, destinatario);
            return new ResponseEntity<>(notificacion, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Usuario o destinatario no encontrado", HttpStatus.BAD_REQUEST);
        }
    } else {
        return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
    }
}

    @GetMapping("/usuario")
    public ResponseEntity<?> obtenerNotificaciones() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                List<Notificacion> notificaciones = notificacionesService.obtenerNotificacionesPorUsuario(usuario);
                return new ResponseEntity<>(notificaciones, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Usuario no encontrado", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/destinatario")
    public ResponseEntity<?> obtenerNotificacionesPorDestinatario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            Optional<Usuario> destinatarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

            if (destinatarioOpt.isPresent()) {
                Usuario destinatario = destinatarioOpt.get();
                List<Notificacion> notificaciones = notificacionesService.obtenerNotificacionesPorDestinatario(destinatario);
                return new ResponseEntity<>(notificaciones, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Destinatario no encontrado", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }
    }
}
