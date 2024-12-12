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

   import com.example.SaludClick.model.Cita;
   import com.example.SaludClick.model.Usuario;
   import com.example.SaludClick.service.ICitaService;
   import com.example.SaludClick.service.UsuarioServiceImp;

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

       @PostMapping
       public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
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
                       cita.setPaciente(usuario);
                       Cita nuevaCita = citaService.crearCita(cita);
                       logger.info("Cita created successfully for user: {}", usuario.getEmail());
                       return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
                   } else {
                       logger.warn("User does not have PACIENTE role: {}", usuario.getRol());
                       return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                   }
               } else {
                   logger.warn("User not found: {}", userDetails.getUsername());
                   return new ResponseEntity<>(HttpStatus.FORBIDDEN);
               }
           } else {
               logger.warn("Principal is not an instance of UserDetails");
               return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
