
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

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<Usuario> optionalUsuario = usuarioServiceImp.buscarPorEmail(username);

        if (optionalUsuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con el email: " + username);
        }

        Usuario usuario = optionalUsuario.get();

        return User.builder()
                   .username(usuario.getEmail())
                   .password(usuario.getContrasena())
                   .roles(usuario.getRol().name())
                   .build();
    }
}
