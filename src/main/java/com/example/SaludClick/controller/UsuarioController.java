package com.example.SaludClick.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.UsuarioServiceImp;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServiceImp usuarioServiceImp;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario usuario) {
        System.out.println("Intentando registrar un nuevo usuario...");
        Usuario nuevoUsuario = usuarioServiceImp.registrar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> traerUsuarioPorEmail(@PathVariable String email) {
        System.out.println("Buscando usuario con email: " + email);
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);

        if (usuarioOpt.isPresent()) {
            return new ResponseEntity<>(usuarioOpt.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    @PatchMapping("/actualizar")
    public ResponseEntity<?> actualizarUsuarioParcial(@RequestBody Map<String, Object> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }

        String email = ((UserDetails) principal).getUsername();
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);

        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuarioExistente = usuarioOpt.get();
        System.out.println("Usuario encontrado: " + usuarioExistente.getEmail() + ". Aplicando actualizaciones...");

        updates.forEach((key, value) -> {
            switch (key) {
                case "nombre":
                    usuarioExistente.setNombre((String) value);
                    break;
                case "email":
                    usuarioExistente.setEmail((String) value);
                    break;
                case "contrasena":
                    usuarioExistente.setContrasena(passwordEncoder.encode((String) value));
                    break;
                case "telefono":
                    usuarioExistente.setTelefono((String) value);
                    break;
                case "direccion":
                    usuarioExistente.setDireccion((String) value);
                    break;
                case "activo":
                    usuarioExistente.setActivo((Boolean) value);
                    break;
                case "rol":
                    usuarioExistente.setRol(Usuario.Rol.valueOf((String) value));
                    break;
                default:
                    System.out.println("Campo desconocido: " + key);
            }
        });

        Usuario actualizado = usuarioServiceImp.actualizar(usuarioExistente);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/datos")
    public ResponseEntity<?> obtenerDatosUsuario(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>("No autenticado", HttpStatus.UNAUTHORIZED);
        }

        String email = userDetails.getUsername();
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);

        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        return ResponseEntity.ok(usuarioOpt.get());
    }

    @DeleteMapping("/eliminar/{idUsuario}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Long idUsuario) {
        System.out.println("Intentando eliminar usuario con ID: " + idUsuario);

        // Obtener el email del usuario autenticado
        String emailUsuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Usuario> usuarioAutenticadoOpt = usuarioServiceImp.buscarPorEmail(emailUsuarioAutenticado);

        if (!usuarioAutenticadoOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado"));
        }

        Usuario usuarioAutenticado = usuarioAutenticadoOpt.get();

        // Verificar si el usuario tiene permiso para eliminarse
        if (!usuarioAutenticado.getIdUsuario().equals(idUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para eliminar este usuario"));
        }

        try {
            usuarioServiceImp.eliminar(idUsuario);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario ha sido eliminado.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }
    }
}
