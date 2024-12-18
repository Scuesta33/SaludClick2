
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

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

@Service
public class UsuarioServiceImp implements IUsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setActivo(true);
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        // Send registration email
        try {
            emailService.sendRegistrationEmail(usuario.getEmail());
        } catch (MessagingException e) {
            // Handle the exception (e.g., log it)
        }

        return nuevoUsuario;
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
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        // Send credential update email
        try {
            emailService.sendCredentialUpdateEmail(usuario.getEmail());
        } catch (MessagingException e) {
            // Handle the exception (e.g., log it)
        }

        return usuarioActualizado;
    }

@Override
public void eliminar(Long idUsuario) {
    String emailUsuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
    Optional<Usuario> usuarioAutenticadoOpt = usuarioRepository.findByEmail(emailUsuarioAutenticado);

    if (usuarioAutenticadoOpt.isEmpty()) {
        throw new IllegalArgumentException("Usuario no autenticado");
    }

    Usuario usuarioAutenticado = usuarioAutenticadoOpt.get();

    if (!usuarioAutenticado.getIdUsuario().equals(idUsuario)) {
        throw new IllegalArgumentException("No tienes permiso para eliminar este usuario");
    }

    if (!usuarioRepository.existsById(idUsuario)) {
        throw new IllegalArgumentException("El usuario no existe");
    }

    usuarioRepository.deleteById(idUsuario);

    // Send account deletion email
    try {
        emailService.sendAccountDeletionEmail(usuarioAutenticado.getEmail());
    } catch (MessagingException e) {
        // Handle the exception (e.g., log it)
    }
}

    

    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }
}
