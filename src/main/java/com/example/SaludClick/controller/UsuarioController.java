package com.example.SaludClick.controller;

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
        Usuario nuevoUsuario = usuarioServiceImp.registrar(usuario);
        System.out.println("usuario nuevo creado :)");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    
    @GetMapping("/{email}")
    public ResponseEntity<Usuario> traerUsuarioPorEmail(@PathVariable String email) {
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);
        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado :(");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(usuarioOpt.get(), HttpStatus.OK);
    }

    
    @PatchMapping("/actualizar")
    public ResponseEntity<Usuario> actualizarUsuarioParcial(@RequestBody Map<String, Object> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            System.out.println("no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = ((UserDetails) principal).getUsername();
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);
        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
                    System.out.println("campo desconocido: " + key);
            }
        });
        usuarioServiceImp.actualizar(usuarioExistente);
        System.out.println("usuario actualizado :)");
        return new ResponseEntity<>(HttpStatus.OK);
    }
    

    @GetMapping("/datos")
    public ResponseEntity<Usuario> obtenerDatosUsuario(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = userDetails.getUsername();
        Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(email);
        if (!usuarioOpt.isPresent()) {
            System.out.println("usuario no encontrado :(");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(usuarioOpt.get(), HttpStatus.OK);
    }

    
    @DeleteMapping("/eliminar/{idUsuario}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long idUsuario) {
        // Obtener el email del usuario autenticado
        String emailUsuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Usuario> usuarioAutenticadoOpt = usuarioServiceImp.buscarPorEmail(emailUsuarioAutenticado);
        if (!usuarioAutenticadoOpt.isPresent()) {
            System.out.println("usuario no autenticado :(");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Usuario usuarioAutenticado = usuarioAutenticadoOpt.get();
        // Verificar si el usuario tiene permiso para eliminarse
        if (!usuarioAutenticado.getIdUsuario().equals(idUsuario)) {
            System.out.println("no tienes permiso para eliminar este usuario :(");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            usuarioServiceImp.eliminar(idUsuario);
            System.out.println("usuario eliminado correctamente :))");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("error al eliminar usuario: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
