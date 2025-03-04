package com.example.SaludClick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.SaludClick.DTO.AuthLoginRequestDTO;
import com.example.SaludClick.DTO.AuthResponseDTO;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.securityConfig.JwtUtils;
import com.example.SaludClick.service.UsuarioServiceImp;
import com.example.SaludClick.service.UsuarioDetailsServiceImp;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioServiceImp usuarioServiceImp;
    @Autowired
    private UsuarioDetailsServiceImp usuarioDetailsServiceImp;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthLoginRequestDTO loginRequest) {
        try {
            // Autenticacion
            UsernamePasswordAuthenticationToken authInput = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getContrasena());
            Authentication authentication = authenticationManager.authenticate(authInput);
            // Obtener detalles del usuario 
            UserDetails userDetails = usuarioDetailsServiceImp.loadUserByUsername(loginRequest.getEmail());
            // Buscar el usuario en la base de datos
            Usuario usuario = usuarioServiceImp.buscarPorEmail(loginRequest.getEmail()).orElse(null);
            if (usuario == null) {
                System.out.println("Usuario no encontrado :(");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            // Generar token JWT para el usuario
            String tokenGenerado = jwtUtils.createToken(authentication, usuario.getIdUsuario());
            // Construir respuesta con el token y datos del usuario
            AuthResponseDTO response = new AuthResponseDTO(tokenGenerado, usuario.getEmail(), usuario.getNombre(),
                    usuario.getRol().name());
            System.out.println("Usuario autenticado: " + usuario.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error en la autenticación: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
