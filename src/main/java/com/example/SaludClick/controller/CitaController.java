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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // Función para convertir el día de la semana
    public String convertirDiaALaSemanaEspañol(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return "";
        }
    }
    


    

    @PostMapping("/crear")
    public ResponseEntity<?> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                if (usuario.getRol() != Usuario.Rol.PACIENTE) {
                    return new ResponseEntity<>("El usuario no tiene permisos suficientes para crear una cita", HttpStatus.FORBIDDEN);
                }

                Cita cita = new Cita();
                
                // Obtener la fecha original
                LocalDateTime fechaOriginal = citaDTO.getFecha();
                
                // Sumar una hora a la fecha, pero solo si no se cruza al siguiente día
                LocalDateTime nuevaFecha = fechaOriginal.plusHours(1);
                if (nuevaFecha.toLocalDate().isEqual(fechaOriginal.toLocalDate())) {
                    cita.setFecha(nuevaFecha); // Solo ajusta la fecha si sigue dentro del mismo día
                } else {
                    return new ResponseEntity<>("La cita no puede ser ajustada a un día diferente", HttpStatus.BAD_REQUEST);
                }

                cita.setEstado(citaDTO.getEstado());
                cita.setPaciente(usuario);

                List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());
                if (medicos.size() != 1 || medicos.get(0).getRol() != Usuario.Rol.MEDICO) {
                    return new ResponseEntity<>("No se encuentra un medico con ese nombre", HttpStatus.BAD_REQUEST);
                }

                Usuario medico = medicos.get(0);
                List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(medico.getIdUsuario());

                String diaEnEspañol = convertirDiaALaSemanaEspañol(citaDTO.getFecha().getDayOfWeek());
                boolean isAvailable = disponibilidades.stream().anyMatch(d ->
                    d.getDiaSemana().equals(diaEnEspañol) &&
                    !citaDTO.getFecha().toLocalTime().isBefore(d.getHoraInicio()) &&
                    !citaDTO.getFecha().toLocalTime().isAfter(d.getHoraFin())
                );

                if (!isAvailable) {
                    return new ResponseEntity<>("El médico no está disponible en esa fecha y hora", HttpStatus.BAD_REQUEST);
                }

                cita.setMedico(medico);
                Cita nuevaCita = citaService.crearCita(cita);

                try {
                    emailService.sendCitaCreationEmail(usuario.getEmail(), cita.getFecha().toString(), "Location");
                } catch (MessagingException e) {
                    logger.error("Error sending email notification to user {} for cita {}: {}", usuario.getEmail(), cita.getIdCita(), e.getMessage());
                }

                return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Usuario no encontrado", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
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




    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        logger.info("Datos recibidos para actualizar la cita: id={}, fecha={}, estado={}, medicoNombre={}",
                    citaDTO.getId(), citaDTO.getFecha(), citaDTO.getEstado(), citaDTO.getMedicoNombre());

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
                                Map<String, String> response = new HashMap<>();
                                response.put("error", "Medico not found");
                                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Bad Request si no se encuentra el médico
                            } else if (medicos.size() > 1) {
                                logger.warn("Se encontraron múltiples médicos con el nombre: {}", citaDTO.getMedicoNombre());
                                Map<String, String> response = new HashMap<>();
                                response.put("error", "Multiple medicos found");
                                return new ResponseEntity<>(response, HttpStatus.CONFLICT); // Conflicto si hay múltiples médicos
                            } else {
                                Usuario medico = medicos.get(0); // Asignar el único médico encontrado
                                if (medico.getRol() == Usuario.Rol.MEDICO) {
                                    List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(medico.getIdUsuario());

                                    boolean isAvailable = disponibilidades.stream().anyMatch(d ->
                                        d.getDiaSemana().equals(convertirDiaALaSemanaEspañol(citaDTO.getFecha().getDayOfWeek())) &&
                                        !citaDTO.getFecha().toLocalTime().isBefore(d.getHoraInicio()) &&
                                        !citaDTO.getFecha().toLocalTime().isAfter(d.getHoraFin())
                                    );

                                    if (isAvailable) {
                                        cita.setMedico(medico);
                                    } else {
                                        Map<String, String> response = new HashMap<>();
                                        response.put("error", "Medico is not available at the selected time");
                                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                                    }
                                } else {
                                    logger.warn("El usuario con nombre {} no tiene el rol de MEDICO", citaDTO.getMedicoNombre());
                                    Map<String, String> response = new HashMap<>();
                                    response.put("error", "User is not a medico");
                                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Bad Request si no es médico
                                }
                            }
                        } else {
                            logger.warn("Información del médico está incompleta o faltante");
                            Map<String, String> response = new HashMap<>();
                            response.put("error", "Medico information is incomplete or missing");
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Bad Request si falta información del médico
                        }

                        Cita citaActualizada = citaService.actualizarCita(cita);

                        // Send email notification
                        try {
                            emailService.sendCitaUpdateEmail(usuario.getEmail(), cita.getFecha().toString(), "Location");
                        } catch (MessagingException e) {
                            logger.error("Error sending email", e);
                        }

                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Cita updated successfully");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    } else {
                        Map<String, String> response = new HashMap<>();
                        response.put("error", "Cita not found");
                        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    }
                } else {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Forbidden");
                    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                }
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Forbidden");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
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
    @GetMapping
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        logger.info("Listing all Citas");
        List<Cita> citas = citaService.listarCitas();
        List<CitaDTO> citaDTOs = citas.stream().map(cita -> {
            CitaDTO dto = new CitaDTO();
            dto.setId(cita.getIdCita());
            dto.setFecha(cita.getFecha());
            dto.setEstado(cita.getEstado());
            dto.setMedicoNombre(cita.getMedico().getNombre()); // Asegúrate de que el nombre del médico se está asignando correctamente
            return dto;
        }).collect(Collectors.toList());
        return new ResponseEntity<>(citaDTOs, HttpStatus.OK);
    }
}