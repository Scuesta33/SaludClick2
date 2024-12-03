package com.example.SaludClick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SaludClick.DTO.AuthLoginRequestDTO;
import com.example.SaludClick.DTO.AuthResponseDTO;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.securityConfig.JwtUtils;
import com.example.SaludClick.service.UsuarioDetailsServiceImp;
import com.example.SaludClick.service.UsuarioServiceImp;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioServiceImp usuarioServiceImp;

    @Autowired
    private UsuarioDetailsServiceImp usuarioDetailsServiceImp;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthLoginRequestDTO loginRequest) {
        // Verifica las credenciales y genera el token
        String email = loginRequest.getEmail();
        String contrasena = loginRequest.getContrasena();

        Usuario usuario = usuarioServiceImp.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Aquí se podría incluir lógica para verificar la contraseña con el PasswordEncoder

        // Generar token
        String token = jwtUtils.generateToken(email);

        // Construir la respuesta con los detalles del usuario y el token
        AuthResponseDTO response = new AuthResponseDTO(
            token,
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getRol().name()
        );

        return ResponseEntity.ok(response);
    }
}
