 package com.example.SaludClick.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.SaludClick.DTO.CitaDTO;
import com.example.SaludClick.model.Cita;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.ICitaService;
import com.example.SaludClick.service.UsuarioServiceImp;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/citas")
public class CitaController {

    private static final Logger logger = LoggerFactory.getLogger(CitaController.class);

    private final ICitaService citaService;
    private final UsuarioServiceImp usuarioServiceImp;

    @Autowired
    public CitaController(ICitaService citaService, UsuarioServiceImp usuarioServiceImp) {
        this.citaService = citaService;
        this.usuarioServiceImp = usuarioServiceImp;
    }

    @PostMapping("/crear")
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        logger.info("Attempting to create a new Cita");

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            logger.info("Authenticated user: {}", userDetails.getUsername());

            Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                logger.info("User found: {}", usuario.getEmail());

                if (usuario.getRol() == Usuario.Rol.PACIENTE) {
                    Cita cita = new Cita();
                    cita.setFecha(citaDTO.getFecha());
                    cita.setEstado(citaDTO.getEstado());
                    cita.setPaciente(usuario);

                    // Verificar y asignar el médico usando el nombre
                    if (citaDTO.getMedicoNombre() != null) {
                        List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());
                        if (medicos.isEmpty()) {
                            logger.warn("No se encontró ningún médico con el nombre: {}", citaDTO.getMedicoNombre());
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Bad Request si no se encuentra el médico
                        } else if (medicos.size() > 1) {
                            logger.warn("Se encontraron múltiples médicos con el nombre: {}", citaDTO.getMedicoNombre());
                            return new ResponseEntity<>(HttpStatus.CONFLICT); // Conflicto si hay múltiples médicos
                        } else {
                            Usuario medico = medicos.get(0); // Asignar el único médico encontrado
                            if (medico.getRol() == Usuario.Rol.MEDICO) {
                                cita.setMedico(medico);
                            } else {
                                logger.warn("El usuario con nombre {} no tiene el rol de MEDICO", citaDTO.getMedicoNombre());
                                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Bad Request si no es médico
                            }
                        }
                    } else {
                        logger.warn("Información del médico está incompleta o faltante");
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Bad Request si falta información del médico
                    }

                    // Crear la cita
                    Cita nuevaCita = citaService.crearCita(cita);
                    logger.info("Cita creada exitosamente para el usuario: {}", usuario.getEmail());
                    return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
                } else {
                    logger.warn("El usuario no tiene el rol de PACIENTE: {}", usuario.getRol());
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Forbidden si no es paciente
                }
            } else {
                logger.warn("Usuario no encontrado: {}", userDetails.getUsername());
                return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Forbidden si el usuario no es válido
            }
        } else {
            logger.warn("El principal no es una instancia de UserDetails");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Unauthorized si no está autenticado correctamente
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        logger.info("Fetching Cita with id: {}", id);
        Optional<Cita> cita = citaService.obtenerCitaPorId(id);
        return cita.map(ResponseEntity::ok).orElseGet(() -> {
            logger.warn("Cita not found with id: {}", id);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        logger.info("Listing all Citas");
        List<Cita> citas = citaService.listarCitas();
        return new ResponseEntity<>(citas, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody Cita cita) {
        logger.info("Updating Cita with id: {}", id);
        cita.setIdCita(id);
        Cita citaActualizada = citaService.actualizarCita(cita);
        return new ResponseEntity<>(citaActualizada, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        logger.info("Deleting Cita with id: {}", id);
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }
}
