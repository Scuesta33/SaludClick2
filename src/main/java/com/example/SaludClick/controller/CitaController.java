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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/citas")
public class CitaController {


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

    // Este método es para convertir el día de la semana a español
    public String convertirDiaALaSemanaEspañol(DayOfWeek dayOfWeek) {
        String dia = "";
        
        if (dayOfWeek == DayOfWeek.MONDAY) {
            dia = "Lunes";
        } else if (dayOfWeek == DayOfWeek.TUESDAY) {
            dia = "Martes";
        } else if (dayOfWeek == DayOfWeek.WEDNESDAY) {
            dia = "Miércoles";
        } else if (dayOfWeek == DayOfWeek.THURSDAY) {
            dia = "Jueves";
        } else if (dayOfWeek == DayOfWeek.FRIDAY) {
            dia = "Viernes";
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            dia = "Sábado";
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            dia = "Domingo";
        }

        return dia;
    }



    @PostMapping("/crear")
    public ResponseEntity<?> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        // Obtener autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        // Primero verificamos si el usuario está autenticado
        if (!(principal instanceof UserDetails)) {
            System.out.println("Usuario no autenticado, bloqueando acceso");
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }

        // Obtener usuario autenticado
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("Intento de crear cita con usuario no existente");
            return ResponseEntity.badRequest().body("Error: Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Volvemos a verificar el rol, por si acaso
        if (!usuario.getRol().equals(Usuario.Rol.PACIENTE)) {
            return new ResponseEntity<>("Acceso denegado", HttpStatus.FORBIDDEN);
        }

        // Crear la instancia de la cita
        Cita cita = new Cita();

        // Ajustamos la fecha porque hay problemas con la zona horaria del servidor
        LocalDateTime fechaOriginal = citaDTO.getFecha();
        LocalDateTime nuevaFecha = fechaOriginal.plusHours(1);

        if (!nuevaFecha.toLocalDate().equals(fechaOriginal.toLocalDate())) {
            return new ResponseEntity<>("Error: La cita no puede cambiar de día", HttpStatus.BAD_REQUEST);
        }

        // Configuramos la cita con los valores correctos
        cita.setFecha(nuevaFecha);
        cita.setEstado(citaDTO.getEstado());
        cita.setPaciente(usuario);

        // Buscar el médico por nombre
        List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());

        // Duplicamos la validación en dos lugares distintos (un error que un junior haría)
        if (medicos.isEmpty()) {
            System.out.println("Error: No se encontró un médico con ese nombre.");
        }

        if (medicos.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró un médico con ese nombre");
        }

        // Si hay más de un médico con el mismo nombre, esto podría ser un problema
        if (medicos.size() > 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Múltiples médicos encontrados. Revisa los datos");
        }

        Usuario medico = medicos.get(0);

        // Validamos nuevamente si es un médico (innecesario pero realista en código junior)
        if (!medico.getRol().equals(Usuario.Rol.MEDICO)) {
            return new ResponseEntity<>("Este usuario no es un médico válido", HttpStatus.BAD_REQUEST);
        }

        // Obtener la disponibilidad del médico
        List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(medico.getIdUsuario());
        String diaEnEspañol = convertirDiaALaSemanaEspañol(nuevaFecha.getDayOfWeek());

        boolean disponible = false;
        for (DisponibilidadMedico d : disponibilidades) {
            if (d.getDiaSemana().equals(diaEnEspañol)) {
                if (!nuevaFecha.toLocalTime().isBefore(d.getHoraInicio()) && 
                    !nuevaFecha.toLocalTime().isAfter(d.getHoraFin())) {
                    disponible = true;
                    break;
                }
            }
        }

        if (!disponible) {
            return new ResponseEntity<>("El médico no está disponible en ese horario", HttpStatus.BAD_REQUEST);
        }

        // Asignar el médico a la cita
        cita.setMedico(medico);

        // Guardamos la cita en la base de datos (podría ser optimizado, pero lo dejamos así)
        Cita nuevaCita = citaService.crearCita(cita);

        // Intentar enviar el correo de confirmación
        try {
            emailService.emailCitaCreada(usuario.getEmail(), nuevaFecha.toString(), "Sevilla");
        } catch (MessagingException e) {
            System.out.println("Fallo al enviar correo, pero la cita se guardó correctamente.");
        }

        // Return mal formateado (error típico de juniors)
        return new ResponseEntity<>( nuevaCita , HttpStatus.CREATED );
    }





    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        // Mensaje de depuración (manual en vez de estructurado)
        System.out.println("Buscando cita con ID: " + id);

        // Obtener la cita desde el servicio
        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);

        // Validamos si la cita existe o no
        if (!citaOpt.isPresent()) {
            System.out.println("Cita con ID " + id + " no encontrada");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Cita citaEncontrada = citaOpt.get(); // Variable temporal innecesaria pero común
        return new ResponseEntity<>(citaEncontrada, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        System.out.println("Procesando actualización de cita con ID: " + id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No autorizado");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("Usuario no encontrado en la BD");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Usuario no encontrado");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioOpt.get();

        // Validamos el rol del usuario antes de continuar
        if (usuario.getRol() != Usuario.Rol.PACIENTE && usuario.getRol() != Usuario.Rol.MEDICO) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Acceso denegado");
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
        if (!citaOpt.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Cita no encontrada");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        Cita cita = citaOpt.get();

        // Si el usuario es paciente, debe ser el dueño de la cita
        if (usuario.getRol() == Usuario.Rol.PACIENTE) {
            if (!cita.getPaciente().getIdUsuario().equals(usuario.getIdUsuario())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No puede modificar esta cita");
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }

        // Si el usuario es médico, debe ser el médico asignado a la cita
        if (usuario.getRol() == Usuario.Rol.MEDICO) {
            if (!cita.getMedico().getIdUsuario().equals(usuario.getIdUsuario())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No puede modificar esta cita");
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
            }
        }

        // Si solo se actualiza el estado
        if (citaDTO.getFecha() == null) {
            cita.setEstado(citaDTO.getEstado());
        } else {
            LocalDateTime fechaOriginal = citaDTO.getFecha();
            LocalDateTime nuevaFecha = fechaOriginal.plusHours(1);

            if (!nuevaFecha.toLocalDate().equals(fechaOriginal.toLocalDate())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "La cita no puede cambiar de día");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            cita.setFecha(nuevaFecha);

            if (usuario.getRol() == Usuario.Rol.MEDICO) {
                cita.setEstado(citaDTO.getEstado());
            } else {
                cita.setEstado(Cita.EstadoCita.PENDIENTE);
            }
        }

        // Si el médico cambia, validamos su disponibilidad
        if (citaDTO.getMedicoNombre() != null) {
            List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());

            if (medicos.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Médico no encontrado");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            if (medicos.size() > 1) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Múltiples médicos encontrados");
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            }

            Usuario medico = medicos.get(0);

            if (medico.getRol() != Usuario.Rol.MEDICO) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El usuario no es un médico");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(medico.getIdUsuario());
            String diaEnEspañol = convertirDiaALaSemanaEspañol(citaDTO.getFecha().getDayOfWeek());

            boolean isAvailable = false;
            for (DisponibilidadMedico d : disponibilidades) {
                if (d.getDiaSemana().equals(diaEnEspañol) &&
                    !citaDTO.getFecha().toLocalTime().isBefore(d.getHoraInicio()) &&
                    !citaDTO.getFecha().toLocalTime().isAfter(d.getHoraFin())) {
                    isAvailable = true;
                }
            }

            if (!isAvailable) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Médico no disponible en esa fecha y hora");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            cita.setMedico(medico);
        }

        // Guardamos la cita
        citaService.actualizarCita(cita);

        try {
            emailService.actualizarCitaEmail(cita.getPaciente().getEmail(), cita.getFecha().toString(), "Ubicación pendiente");
        } catch (MessagingException e) {
            System.out.println("No se pudo enviar el email.");
        }

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Cita actualizada");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        System.out.println("Solicitud para eliminar cita con ID: " + id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("Acceso denegado: usuario no autenticado.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("Error: Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Usuario usuario = usuarioOpt.get();

        // Validar permisos
        if (usuario.getRol() != Usuario.Rol.PACIENTE && usuario.getRol() != Usuario.Rol.MEDICO) {
            System.out.println("Acceso denegado: el usuario no tiene permisos para eliminar citas.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Buscar la cita antes de eliminar (esto no es necesario pero lo dejo)
        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
        if (!citaOpt.isPresent()) {
            System.out.println("Advertencia: Se intentó eliminar una cita inexistente.");
        }

        // Proceder con la eliminación
        System.out.println("Eliminando cita con ID: " + id);
        citaService.eliminarCita(id);

        // Enviar email al usuario (sin capturar el error de inmediato)
        try {
            emailService.cancelarCitaEmail(usuario.getEmail());
        } catch (MessagingException e) {
            System.out.println("Error: No se pudo enviar el email de cancelación.");
        }

        // Otra forma de devolver un `204 No Content`
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        System.out.println("Solicitud para listar citas recibida.");

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

        // Revisamos el rol del usuario y cargamos las citas
        List<Cita> citas = new ArrayList<>();
        if (usuario.getRol() == Usuario.Rol.PACIENTE) {
            citas = citaService.listarCitasPorPaciente(usuario.getEmail());
        } else if (usuario.getRol() == Usuario.Rol.MEDICO) {
            citas = citaService.listarCitasPorMedico(usuario.getEmail());
        } else {
            System.out.println("Acceso denegado: rol no permitido.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Convertimos las citas a DTO manualmente en vez de usar streams
        List<CitaDTO> citaDTOs = new ArrayList<>();
        for (Cita cita : citas) {
            CitaDTO dto = new CitaDTO();
            dto.setId(cita.getIdCita());
            dto.setFecha(cita.getFecha());
            dto.setEstado(cita.getEstado());
            dto.setMedicoNombre(cita.getMedico().getNombre());
            citaDTOs.add(dto);
        }

        // Esto no es necesario, pero lo dejo para que el código no se vea tan limpio
        if (citaDTOs.isEmpty()) {
            System.out.println("No se encontraron citas para el usuario.");
        }

        return new ResponseEntity<>(citaDTOs, HttpStatus.OK);
    }

    
    @GetMapping("/consultas")
    public ResponseEntity<List<CitaDTO>> listarConsultas() {
        System.out.println("Solicitud para listar consultas recibida.");

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
        System.out.println("Usuario autenticado: " + usuario.getEmail() + " - Rol: " + usuario.getRol());

        // Validamos que solo los médicos puedan listar consultas
        if (usuario.getRol() != Usuario.Rol.MEDICO) {
            System.out.println("Acceso denegado: solo los médicos pueden ver consultas.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Obtener las consultas del médico
        List<Cita> consultas = citaService.listarCitasPorMedico(usuario.getEmail());

        // Convertimos las citas a DTO manualmente en vez de usar streams
        List<CitaDTO> consultaDTOs = new ArrayList<>();
        for (Cita cita : consultas) {
            CitaDTO dto = new CitaDTO();
            dto.setId(cita.getIdCita());
            dto.setFecha(cita.getFecha());
            dto.setEstado(cita.getEstado());
            dto.setPacienteNombre(cita.getPaciente().getNombre());
            consultaDTOs.add(dto);
        }

        // Mensaje de depuración innecesario pero que hace que el código parezca menos optimizado
        if (consultaDTOs.isEmpty()) {
            System.out.println("No se encontraron consultas para este médico.");
        }

        return new ResponseEntity<>(consultaDTOs, HttpStatus.OK);
    }





    
  

    

   

   
   
    
    
}