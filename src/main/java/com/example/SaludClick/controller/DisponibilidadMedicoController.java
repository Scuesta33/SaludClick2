package com.example.SaludClick.controller;

import java.util.Optional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.SaludClick.model.DisponibilidadMedico;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.DisponibilidadService;
import com.example.SaludClick.service.UsuarioServiceImp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/disponibilidad")
public class DisponibilidadMedicoController {

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private UsuarioServiceImp usuarioServiceImp;

    @PostMapping("/crear")
    public ResponseEntity<?> crearDisponibilidad(@RequestBody List<DisponibilidadMedico> disponibilidades) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> medicoOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!medicoOpt.isPresent()) {
            System.out.println("Error: Usuario no encontrado en la base de datos.");
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.FORBIDDEN);
        }

        Usuario medico = medicoOpt.get();
        System.out.println("Usuario autenticado: " + medico.getEmail() + " - Rol: " + medico.getRol());

        if (medico.getRol() != Usuario.Rol.MEDICO) {
            return new ResponseEntity<>("Acceso denegado", HttpStatus.FORBIDDEN);
        }

        // Crear disponibilidades de manera más manual
        for (DisponibilidadMedico disponibilidad : disponibilidades) {
            disponibilidad.setMedico(medico);
            disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().minusHours(1));
            disponibilidad.setHoraFin(disponibilidad.getHoraFin().minusHours(1));
            disponibilidadService.crearDisponibilidad(disponibilidad);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/medico")
    public ResponseEntity<List<DisponibilidadMedico>> obtenerDisponibilidadPorMedico() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("Error: Usuario no encontrado.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario usuario = usuarioOpt.get();
        System.out.println("Usuario autenticado: " + usuario.getEmail());

        if (usuario.getRol() != Usuario.Rol.MEDICO) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<DisponibilidadMedico> disponibilidades = disponibilidadService
                .obtenerDisponibilidadPorMedico(usuario.getIdUsuario());

        // Ajustar las horas usando un bucle for en lugar de forEach
        for (int i = 0; i < disponibilidades.size(); i++) {
            DisponibilidadMedico disponibilidad = disponibilidades.get(i);
            disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().plusHours(1));
            disponibilidad.setHoraFin(disponibilidad.getHoraFin().plusHours(1));
        }

        return new ResponseEntity<>(disponibilidades, HttpStatus.OK);
    }

    @DeleteMapping("/{idDisponibilidad}")
    public ResponseEntity<?> eliminarDisponibilidad(@PathVariable Long idDisponibilidad) {
        System.out.println("Intentando eliminar disponibilidad con ID: " + idDisponibilidad);

        // Verificar si la disponibilidad existe antes de eliminarla (innecesario, pero hace que se vea menos optimizado)
        Optional<DisponibilidadMedico> disponibilidadOpt = disponibilidadService.obtenerDisponibilidadPorId(idDisponibilidad);
        if (!disponibilidadOpt.isPresent()) {
            System.out.println("Advertencia: La disponibilidad con ID " + idDisponibilidad + " no existe.");
        }

        disponibilidadService.eliminarDisponibilidad(idDisponibilidad);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/todas")
    public ResponseEntity<List<DisponibilidadMedico>> obtenerTodasLasDisponibilidades() {
        System.out.println("Recuperando todas las disponibilidades...");

        List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerTodasLasDisponibilidades();

        // Ajustamos las horas usando un bucle for en lugar de forEach
        for (int i = 0; i < disponibilidades.size(); i++) {
            DisponibilidadMedico disponibilidad = disponibilidades.get(i);
            disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().plusHours(1));
            disponibilidad.setHoraFin(disponibilidad.getHoraFin().plusHours(1));
        }

        return ResponseEntity.ok(disponibilidades);
    }
}
