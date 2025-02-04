package com.example.SaludClick.service;

import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import com.example.SaludClick.model.Usuario;

@Service
public class UsuarioDetailsServiceImp implements UserDetailsService {

    private final UsuarioServiceImp usuarioServiceImp;

    @Lazy
    public UsuarioDetailsServiceImp(UsuarioServiceImp usuarioServiceImp) {
        this.usuarioServiceImp = usuarioServiceImp;
    }

    // metodo generado por la interfaz UserDetailsService para cargar por email
    @Override
    public UserDetails loadUserByUsername(String email) {
        System.out.println("Intentando cargar usuario con email: " + email);
        if (email == null || email.isEmpty()) {
            System.out.println("Error: El email proporcionado es nulo o vacío.");
            throw new RuntimeException("Email inválido.");
        }
        Optional<Usuario> optionalUsuario = usuarioServiceImp.buscarPorEmail(email);
        if (optionalUsuario.isEmpty()) {
            System.out.println("Advertencia: No se encontró usuario con el email: " + email);
            throw new RuntimeException("Usuario no encontrado.");
        }
        Usuario usuario = optionalUsuario.get();
        System.out.println("Usuario encontrado: " + usuario.getEmail() + ", Rol: " + usuario.getRol().name());
        UserDetails userDetails = User.builder()
                                      .username(usuario.getEmail())
                                      .password(usuario.getContrasena())
                                      .roles(usuario.getRol().name())
                                      .build();
        System.out.println("Usuario cargado correctamente.");
        return userDetails;
    }
}
