
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
        Usuario nuevoUsuario = usuarioServiceImp.registrar(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    public ResponseEntity<Usuario> traerUsuarioPorEmail(@PathVariable String email) {
        return usuarioServiceImp.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @PatchMapping("/actualizar")
    public ResponseEntity<Usuario> actualizarUsuarioParcial(@RequestBody Map<String, Object> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "nombre":
                        usuarioExistente.setNombre((String) value);
                        break;
                    case "email":
                        usuarioExistente.setEmail((String) value);
                        break;
                    case "contrasena":
                        String encodedPassword = passwordEncoder.encode((String) value);
                        usuarioExistente.setContrasena(encodedPassword);
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
                        throw new IllegalArgumentException("Campo no válido: " + key);
                }
            });
            return ResponseEntity.ok(usuarioServiceImp.actualizar(usuarioExistente));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }

    @GetMapping("/datos")
    public ResponseEntity<Usuario> obtenerDatosUsuario(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Usuario usuario = usuarioServiceImp.buscarPorEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/eliminar/{idUsuario}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Long idUsuario) {
        // Obtener el email del usuario autenticado desde el contexto de seguridad
        String emailUsuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // Buscar al usuario autenticado por su email
            Optional<Usuario> usuarioAutenticadoOpt = usuarioServiceImp.buscarPorEmail(emailUsuarioAutenticado);

            // Lanzar excepción si no existe el usuario autenticado
            if (usuarioAutenticadoOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
            }

            Usuario usuarioAutenticado = usuarioAutenticadoOpt.get();

            // Verificar que el ID del usuario autenticado coincide con el ID a eliminar
            if (!usuarioAutenticado.getIdUsuario().equals(idUsuario)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este usuario");
            }

            // Proceder a eliminar el usuario
            usuarioServiceImp.eliminar(idUsuario);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario ha sido eliminado.");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e; // Repropagar excepciones específicas de respuesta
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado", e);
        }
    }
}
