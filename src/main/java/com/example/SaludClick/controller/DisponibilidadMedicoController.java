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
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> medicoOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!medicoOpt.isPresent()) {
            System.out.println("Usuario no encontrado, prueba de nuevo :)");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario medico = medicoOpt.get();
        if (medico.getRol() != Usuario.Rol.MEDICO) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Crear disponibilidades una por una con ajustes de hora
        for (DisponibilidadMedico disponibilidad : disponibilidades) {
            disponibilidad.setMedico(medico);
            disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().minusHours(1));
            disponibilidad.setHoraFin(disponibilidad.getHoraFin().minusHours(1));
            disponibilidadService.crearDisponibilidad(disponibilidad);
        }

        System.out.println("Disponibilidad creada ;))");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/medico")
    public ResponseEntity<List<DisponibilidadMedico>> obtenerDisponibilidadPorMedico() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado prueba de nuevo :)");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getRol() != Usuario.Rol.MEDICO) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<DisponibilidadMedico> disponibilidades = disponibilidadService
                .obtenerDisponibilidadPorMedico(usuario.getIdUsuario());

        // Ajustar las horas porque se guardan con una hora menos
        for (int i = 0; i < disponibilidades.size(); i++) {
            DisponibilidadMedico disponibilidad = disponibilidades.get(i);
            disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().plusHours(1));
            disponibilidad.setHoraFin(disponibilidad.getHoraFin().plusHours(1));
        }

        System.out.println("Disponibilidades obtenidas correctamente.");
        return new ResponseEntity<>(disponibilidades, HttpStatus.OK);
    }

    @DeleteMapping("/{idDisponibilidad}")
    public ResponseEntity<?> eliminarDisponibilidad(@PathVariable Long idDisponibilidad) {
        Optional<DisponibilidadMedico> disponibilidadOpt = disponibilidadService.obtenerDisponibilidadPorId(idDisponibilidad);
        if (!disponibilidadOpt.isPresent()) {
            System.out.println("la disponibilidad con ese id no eexiste :(");
        }

        // Eliminar la disponibilidad
        disponibilidadService.eliminarDisponibilidad(idDisponibilidad);
        System.out.println("Disponibilidad eliminada :)");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<DisponibilidadMedico>> obtenerTodasLasDisponibilidades() {
        List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerTodasLasDisponibilidades();
// ajsutar las horas porqeu se guardan con una hora menos
        for (int i = 0; i < disponibilidades.size(); i++) {
            DisponibilidadMedico disponibilidad = disponibilidades.get(i);
            disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().plusHours(1));
            disponibilidad.setHoraFin(disponibilidad.getHoraFin().plusHours(1));
        }

        System.out.println("aqui tienes todas las disponibilidades :)");
        return new ResponseEntity<>(disponibilidades, HttpStatus.OK);
    }
}
