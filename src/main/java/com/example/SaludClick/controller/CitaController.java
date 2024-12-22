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
import com.example.SaludClick.model.DisponibilidadMedico;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.DisponibilidadService;
import com.example.SaludClick.service.EmailService;
import com.example.SaludClick.service.ICitaService;
import com.example.SaludClick.service.UsuarioServiceImp;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import java.time.ZoneId;
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
    
	@Autowired
	private DisponibilidadService disponibilidadService;



@PostMapping("/crear")
public ResponseEntity<String> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (usuarioOpt.isPresent() && usuarioOpt.get().getRol() == Usuario.Rol.PACIENTE) {
            Usuario usuario = usuarioOpt.get();
            Cita cita = new Cita();
            cita.setFecha(citaDTO.getFecha().atZone(ZoneId.of("UTC")).toLocalDateTime());
            cita.setEstado(citaDTO.getEstado());
            cita.setPaciente(usuario);

            List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());
            if (medicos.size() == 1 && medicos.get(0).getRol() == Usuario.Rol.MEDICO) {
                Usuario medico = medicos.get(0);
                List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(medico.getIdUsuario());

                boolean isAvailable = disponibilidades.stream().anyMatch(d ->
                    d.getDiaSemana().equals(citaDTO.getFecha().getDayOfWeek().toString()) &&
                    !citaDTO.getFecha().toLocalTime().isBefore(d.getHoraInicio()) &&
                    !citaDTO.getFecha().toLocalTime().isAfter(d.getHoraFin())
                );

                if (isAvailable) {
                    cita.setMedico(medico);
                    Cita nuevaCita = citaService.crearCita(cita);

                    // Send email notification
                    try {
                        emailService.sendCitaCreationEmail(usuario.getEmail(), cita.getFecha().toString(), "Location");
                    } catch (MessagingException e) {
                        logger.error("Error sending email", e);
                    }

                    return new ResponseEntity<>("Cita created successfully", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("Medico is not available at the selected time", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Medico not found or multiple found", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Not a paciente", HttpStatus.FORBIDDEN);
        }
    } else {
        return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);
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
public ResponseEntity<String> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
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
                    cita.setFecha(citaDTO.getFecha().atZone(ZoneId.of("UTC")).toLocalDateTime());
                    cita.setEstado(citaDTO.getEstado());
                    cita.setPaciente(usuario); // Set the authenticated user as the paciente

                    // Verificar y asignar el médico usando el nombre
                    if (citaDTO.getMedicoNombre() != null) {
                        List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());
                        if (medicos.isEmpty()) {
                            logger.warn("No se encontró ningún médico con el nombre: {}", citaDTO.getMedicoNombre());
                            return new ResponseEntity<>("Medico not found", HttpStatus.BAD_REQUEST); // Bad Request si no se encuentra el médico
                        } else if (medicos.size() > 1) {
                            logger.warn("Se encontraron múltiples médicos con el nombre: {}", citaDTO.getMedicoNombre());
                            return new ResponseEntity<>("Multiple medicos found", HttpStatus.CONFLICT); // Conflicto si hay múltiples médicos
                        } else {
                            Usuario medico = medicos.get(0); // Asignar el único médico encontrado
                            if (medico.getRol() == Usuario.Rol.MEDICO) {
                                List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(medico.getIdUsuario());

                                boolean isAvailable = disponibilidades.stream().anyMatch(d ->
                                    d.getDiaSemana().equals(citaDTO.getFecha().getDayOfWeek().toString()) &&
                                    !citaDTO.getFecha().toLocalTime().isBefore(d.getHoraInicio()) &&
                                    !citaDTO.getFecha().toLocalTime().isAfter(d.getHoraFin())
                                );

                                if (isAvailable) {
                                    cita.setMedico(medico);
                                } else {
                                    return new ResponseEntity<>("Medico is not available at the selected time", HttpStatus.BAD_REQUEST);
                                }
                            } else {
                                logger.warn("El usuario con nombre {} no tiene el rol de MEDICO", citaDTO.getMedicoNombre());
                                return new ResponseEntity<>("User is not a medico", HttpStatus.BAD_REQUEST); // Bad Request si no es médico
                            }
                        }
                    } else {
                        logger.warn("Información del médico está incompleta o faltante");
                        return new ResponseEntity<>("Medico information is incomplete or missing", HttpStatus.BAD_REQUEST); // Bad Request si falta información del médico
                    }

                    Cita citaActualizada = citaService.actualizarCita(cita);

                    // Send email notification
                    try {
                        emailService.sendCitaUpdateEmail(usuario.getEmail(), cita.getFecha().toString(), "Location");
                    } catch (MessagingException e) {
                        logger.error("Error sending email", e);
                    }

                    return new ResponseEntity<>("Cita updated successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Cita not found", HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
    } else {
        return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
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
