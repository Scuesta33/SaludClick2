
package com.example.SaludClick.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioServiceImp implements IUsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrar(Usuario usuario) {
        // Ver que el email no exista
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        // Encriptar contraseña
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getIdUsuario())) {
            throw new IllegalArgumentException("El usuario no existe");
        }
        return usuarioRepository.save(usuario);
    }

@Override
public void eliminar(Long idUsuario) {
    // Get the authenticated user's email from the security context
    String emailUsuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

    // Fetch the authenticated user's details from the database
    Optional<Usuario> usuarioAutenticadoOpt = usuarioRepository.findByEmail(emailUsuarioAutenticado);

    // Throw an exception if the authenticated user is not found
    if (usuarioAutenticadoOpt.isEmpty()) {
        throw new IllegalArgumentException("Usuario no autenticado");
    }

    Usuario usuarioAutenticado = usuarioAutenticadoOpt.get();

    // Check if the authenticated user's ID matches the ID of the user to be deleted
    if (!usuarioAutenticado.getIdUsuario().equals(idUsuario)) {
        throw new IllegalArgumentException("No tienes permiso para eliminar este usuario");
    }

    // Proceed with the deletion if the IDs match
    if (!usuarioRepository.existsById(idUsuario)) {
        throw new IllegalArgumentException("El usuario no existe");
    }
    usuarioRepository.deleteById(idUsuario);
}

    

    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }
}
