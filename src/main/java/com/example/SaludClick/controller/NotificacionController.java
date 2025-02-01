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
    public ResponseEntity<?> enviarNotificacion(@RequestParam String asunto, @RequestParam String estado, 
                                                @RequestParam String mensaje, @RequestParam String destinatarioNombre) {
        System.out.println("Intentando enviar una notificación a: " + destinatarioNombre);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());
        List<Usuario> destinatarios = usuarioServiceImp.buscarPorNombre(destinatarioNombre);

        if (!usuarioOpt.isPresent()) {
            System.out.println("Error: Usuario no encontrado en la BD.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado");
        }

        if (destinatarios.isEmpty()) {
            System.out.println("Error: No se encontró destinatario con ese nombre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Destinatario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        Usuario destinatario = destinatarios.get(0); // Se asume que el primer resultado es el correcto

        Notificacion notificacion = notificacionesService.enviarNotificacion(usuario, asunto, estado, mensaje, destinatario);
        
        System.out.println("Notificación enviada con éxito.");
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacion);
    }

    @GetMapping("/usuario")
    public ResponseEntity<?> obtenerNotificaciones() {
        System.out.println("Recuperando notificaciones del usuario actual.");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("Error: Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        List<Notificacion> notificaciones = notificacionesService.obtenerNotificacionesPorUsuario(usuario);

        if (notificaciones.isEmpty()) {
            System.out.println("No hay notificaciones disponibles para el usuario.");
        }

        return new ResponseEntity<>(notificaciones, HttpStatus.OK);
    }

    @GetMapping("/destinatario")
    public ResponseEntity<?> obtenerNotificacionesPorDestinatario() {
        System.out.println("Buscando notificaciones para el destinatario actual.");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> destinatarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!destinatarioOpt.isPresent()) {
            System.out.println("Error: Destinatario no encontrado.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Destinatario no encontrado");
        }

        Usuario destinatario = destinatarioOpt.get();
        List<Notificacion> notificaciones = notificacionesService.obtenerNotificacionesPorDestinatario(destinatario);

        if (notificaciones.isEmpty()) {
            System.out.println("No hay notificaciones para este destinatario.");
        }

        return ResponseEntity.ok(notificaciones);
    }
}
