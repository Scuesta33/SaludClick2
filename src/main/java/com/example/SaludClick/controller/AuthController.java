package com.example.SaludClick.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthLoginRequestDTO loginRequest) {
        // Verifica las credenciales y autentica al usuario
        try {
            // Autenticación usando el AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getContrasena())
            );

            // Si la autenticación es exitosa, obtenemos los detalles del usuario
            UserDetails userDetails = usuarioDetailsServiceImp.loadUserByUsername(loginRequest.getEmail());

            // Obtén el usuario de la base de datos
            Usuario usuario = usuarioServiceImp.buscarPorEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Genera el token usando el método adecuado de JwtUtils
            String token = jwtUtils.createToken(authentication, usuario.getIdUsuario());

            // Construye la respuesta con el token y los detalles del usuario
            AuthResponseDTO response = new AuthResponseDTO(
                token,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Devuelve un error 401 si la autenticación falla
            return ResponseEntity.status(401).body(new AuthResponseDTO("Credenciales incorrectas", null, null, null));
        }
    }
}