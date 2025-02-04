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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());
        List<Usuario> destinatarios = usuarioServiceImp.buscarPorNombre(destinatarioNombre);
        if (!usuarioOpt.isPresent()) {
            System.out.println("no encuentro el usuario :(");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (destinatarios.isEmpty()) {
            System.out.println("no hay destinatario con ese nombre :(");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Usuario usuario = usuarioOpt.get();
        Usuario destinatario = destinatarios.get(0); // Se asume que el primer resultado es el correcto
        Notificacion notificacion = notificacionesService.enviarNotificacion(usuario, asunto, estado, mensaje, destinatario);
        System.out.println("notificación enviada :))");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    
    @GetMapping("/usuario")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());
        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado :(");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Usuario usuario = usuarioOpt.get();
        List<Notificacion> notificaciones = notificacionesService.obtenerNotificacionesPorUsuario(usuario);
        if (notificaciones.isEmpty()) {
            System.out.println("no hay notificaciones para este usuario :(");
        }
        return new ResponseEntity<>(notificaciones, HttpStatus.OK);
    }

    
    @GetMapping("/destinatario")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesPorDestinatario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> destinatarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());
        if (!destinatarioOpt.isPresent()) {
            System.out.println("destinatario no encontrado :(");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Usuario destinatario = destinatarioOpt.get();
        List<Notificacion> notificaciones = notificacionesService.obtenerNotificacionesPorDestinatario(destinatario);
        if (notificaciones.isEmpty()) {
            System.out.println("No hay notificaciones :(");
        }
        return new ResponseEntity<>(notificaciones, HttpStatus.OK);
    }
}
