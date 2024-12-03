package com.example.SaludClick.service;

import java.util.Optional;
import java.util.Collections;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.UsuarioServiceImp;

@Service
public class UsuarioDetailsServiceImp implements UserDetailsService {

    private final UsuarioServiceImp usuarioServiceImp;

    // Constructor para la inyección de dependencias
    @Lazy
    public UsuarioDetailsServiceImp(UsuarioServiceImp usuarioServiceImp) {
        this.usuarioServiceImp = usuarioServiceImp;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Buscar al usuario por email en la base de datos
        Optional<Usuario> optionalUsuario = usuarioServiceImp.buscarPorEmail(username);
        
        // Si el usuario no existe, lanzar una excepción
        if (optionalUsuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con el email: " + username);
        }
        
        Usuario usuario = optionalUsuario.get();

        // Crear un objeto UserDetails con la información del usuario
        return User.builder()
                   .username(usuario.getEmail())
                   .password(usuario.getContrasena()) // Asegúrate de que la contraseña esté codificada
                   .roles(usuario.getRol().name()) // Convertimos el rol a un String
                   .build();
    }
}