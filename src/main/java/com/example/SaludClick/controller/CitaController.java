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
import com.example.SaludClick.service.EmailService;
import com.example.SaludClick.service.ICitaService;
import com.example.SaludClick.service.UsuarioServiceImp;

import jakarta.mail.MessagingException;
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
    @Autowired
    private EmailService emailService;
    


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

                // Send email notification
                try {
                    emailService.sendCitaCreationEmail(usuario.getEmail(), cita.getFecha().toString(), "Location");
                } catch (MessagingException e) {
                    logger.error("Error sending email", e);
                }

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
public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getRol() == Usuario.Rol.PACIENTE || usuario.getRol() == Usuario.Rol.MEDICO) {
                logger.info("Updating Cita with id: {}", id);
                Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
                if (citaOpt.isPresent()) {
                    Cita cita = citaOpt.get();
                    cita.setFecha(citaDTO.getFecha());
                    cita.setEstado(citaDTO.getEstado());
                    cita.setPaciente(usuario); // Set the authenticated user as the paciente

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

                    Cita citaActualizada = citaService.actualizarCita(cita);

                    // Send email notification
                    try {
                        emailService.sendCitaUpdateEmail(usuario.getEmail(), cita.getFecha().toString(), "Location");
                    } catch (MessagingException e) {
                        logger.error("Error sending email", e);
                    }

                    return new ResponseEntity<>(citaActualizada, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    } else {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}



@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getRol() == Usuario.Rol.PACIENTE || usuario.getRol() == Usuario.Rol.MEDICO) {
                logger.info("Deleting Cita with id: {}", id);
                citaService.eliminarCita(id);

                // Send email notification
                try {
                    emailService.sendCitaDeletionEmail(usuario.getEmail());
                } catch (MessagingException e) {
                    logger.error("Error sending email", e);
                }

                return ResponseEntity.noContent().build();
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    } else {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}

    
}
