package com.example.SaludClick.controller;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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

    //este método es para convertir el día de la semana a español
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

        // verifico si está autenticado
        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Obtener usuario autenticado
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        // Revisar si el usuario existe
        if (!usuarioOpt.isPresent()) {
            System.out.println("el usuario no existe o es un fantasma");
            return ResponseEntity.badRequest().build();
        }

        Usuario usuario = usuarioOpt.get();
        
        // Comprobar que el usuario sea paciente
        if (!usuario.getRol().equals(Usuario.Rol.PACIENTE)) {
            System.out.println("tienes que ser paciente");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Crear la instancia de la cita
        Cita cita = new Cita();

        // Ajustamos la hora porque me daba problemas
        LocalDateTime fechaOriginal = citaDTO.getFecha();
        LocalDateTime nuevaFecha = fechaOriginal.plusHours(1);

        if (!nuevaFecha.toLocalDate().equals(fechaOriginal.toLocalDate())) {
            System.out.println("no se puede cambiar la cita de día :(");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        cita.setFecha(nuevaFecha);
        cita.setEstado(citaDTO.getEstado());
        cita.setPaciente(usuario);

        // Buscar el médico por nombre
        List<Usuario> medicos = usuarioServiceImp.buscarPorNombre(citaDTO.getMedicoNombre());

        
        if (medicos.isEmpty()) {
            System.out.println("no se encontró a ese médico prueba con otro :)");
        }

        if (medicos.size() == 0) {
            System.out.println("no hay médicos disponibles");
            return ResponseEntity.badRequest().build();
        }

        if (medicos.size() > 1) {
            System.out.println("demasiados médicos con ese nombre");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Usuario medico = medicos.get(0);

        if (!medico.getRol().equals(Usuario.Rol.MEDICO)) {
            System.out.println("este usuario no es un médico válido");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
            System.out.println("el medico no está disponible en ese horario, prueba otro");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Asignar el médico a la cita
        cita.setMedico(medico);

        // Guardamos la cita
        Cita nuevaCita = citaService.crearCita(cita);

        // Intentar enviar el correo de confirmación
        try {
            emailService.emailCitaCreada(usuario.getEmail(), nuevaFecha.toString(), "Sevilla");
        } catch (MessagingException e) {
            System.out.println("fallo al enviar correo");
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        // Obtener la cita 
        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);

        // Validamos si la cita existe o no
        if (!citaOpt.isPresent()) {
            System.out.println("Cita con ID " + id + " no encontrada");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Cita citaEncontrada = citaOpt.get(); 
        return new ResponseEntity<>(citaEncontrada, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioOpt.get();

        // Validamos el rol del usuario antes de continuar
        if (usuario.getRol() != Usuario.Rol.PACIENTE && usuario.getRol() != Usuario.Rol.MEDICO) {
            System.out.println("tienes que ser paciente o medico ;)");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
        if (!citaOpt.isPresent()) {
            System.out.println("Cita no encontrada");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Cita cita = citaOpt.get();

        // Si el usuario es paciente, debe ser el dueño de la cita
        if (usuario.getRol() == Usuario.Rol.PACIENTE) {
            if (!cita.getPaciente().getIdUsuario().equals(usuario.getIdUsuario())) {
                System.out.println("No puede modificar esta cita");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        // Si el usuario es médico, debe ser el médico asignado a la cita
        if (usuario.getRol() == Usuario.Rol.MEDICO) {
            if (!cita.getMedico().getIdUsuario().equals(usuario.getIdUsuario())) {
                System.out.println("No puede modificar esta cita");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        // Si solo se actualiza el estado
        if (citaDTO.getFecha() == null) {
            cita.setEstado(citaDTO.getEstado());
        } else {
            LocalDateTime fechaOriginal = citaDTO.getFecha();
            LocalDateTime nuevaFecha = fechaOriginal.plusHours(1);

            if (!nuevaFecha.toLocalDate().equals(fechaOriginal.toLocalDate())) {
                System.out.println("La cita no puede cambiar de día");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
                System.out.println("Médico no encontrado");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (medicos.size() > 1) {
                System.out.println("Múltiples médicos encontrados");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            Usuario medico = medicos.get(0);

            if (medico.getRol() != Usuario.Rol.MEDICO) {
                System.out.println("El usuario no es un médico");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
                System.out.println("Médico no disponible en esa fecha y hora");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            cita.setMedico(medico);
        }

        // Guardamos la cita
        citaService.actualizarCita(cita);

        try {
            emailService.actualizarCitaEmail(cita.getPaciente().getEmail(), cita.getFecha().toString(), "Sevilla");
        } catch (MessagingException e) {
            System.out.println("No se pudo enviar el email");
        }

        System.out.println("Cita actualizada correctamente");
        return new ResponseEntity<>(HttpStatus.OK);
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario usuario = usuarioOpt.get();

        // Validar permisos
        if (usuario.getRol() != Usuario.Rol.PACIENTE && usuario.getRol() != Usuario.Rol.MEDICO) {
            System.out.println("el usuario no tiene permisos para eliminar cita :(");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Buscar la cita antes de eliminar (esto no es necesario pero lo dejo)
        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
        if (!citaOpt.isPresent()) {
            System.out.println("no puedes eliminar una cita que no existe XD");
        }

        // Proceder con la eliminación
        System.out.println("Eliminando cita con ID: " + id);
        citaService.eliminarCita(id);

        try {
            emailService.cancelarCitaEmail(usuario.getEmail());
        } catch (MessagingException e) {
            System.out.println("no he podido enviar el email de cancelación :(");
        }

        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("no estas autenticado :(");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("no encuentro el usuario :(");
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
            System.out.println("no tienes permisos para ver citas :(");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

   
        List<CitaDTO> citaDTOs = new ArrayList<>();
        for (Cita cita : citas) {
            CitaDTO dto = new CitaDTO();
            dto.setId(cita.getIdCita());
            dto.setFecha(cita.getFecha());
            dto.setEstado(cita.getEstado());
            dto.setMedicoNombre(cita.getMedico().getNombre());
            citaDTOs.add(dto);
        }

       
        if (citaDTOs.isEmpty()) {
            System.out.println("No se encontraron citas para este usuario");
        }

        return new ResponseEntity<>(citaDTOs, HttpStatus.OK);
    }

    
    @GetMapping("/consultas")
    public ResponseEntity<List<CitaDTO>> listarConsultas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            System.out.println("usuario no autenticado :(");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado :(");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario usuario = usuarioOpt.get();

        // Validamos que solo los médicos puedan listar consultas
        if (usuario.getRol() != Usuario.Rol.MEDICO) {
            System.out.println("solo los médicos pueden ver consultas :)");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Obtener las consultas del médico
        List<Cita> consultas = citaService.listarCitasPorMedico(usuario.getEmail());

        List<CitaDTO> consultaDTOs = new ArrayList<>();
        for (Cita cita : consultas) {
            CitaDTO dto = new CitaDTO();
            dto.setId(cita.getIdCita());
            dto.setFecha(cita.getFecha());
            dto.setEstado(cita.getEstado());
            dto.setPacienteNombre(cita.getPaciente().getNombre());
            consultaDTOs.add(dto);
        }

        if (consultaDTOs.isEmpty()) {
            System.out.println("no se encontraron consultas");
        }

        return new ResponseEntity<>(consultaDTOs, HttpStatus.OK);
    }





    
  

    

   

   
   
    
    
}