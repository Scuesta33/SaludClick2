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
        System.out.println("Intentando registrar usuario con email: " + usuario.getEmail());

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            System.out.println("Error: El email ya está registrado.");
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setActivo(true);
        
        System.out.println("Guardando usuario en la base de datos...");
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        try {
            System.out.println("Enviando correo de confirmación de registro...");
            emailService.registroEmail(usuario.getEmail());
        } catch (MessagingException e) {
            System.out.println("Error enviando correo de registro: " + e.getMessage());
        }

        return nuevoUsuario;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        System.out.println("Buscando usuario con email: " + email);
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<Usuario> listar() {
        System.out.println("Listando todos los usuarios...");
        List<Usuario> usuarios = usuarioRepository.findAll();

        if (usuarios.isEmpty()) {
            System.out.println("Advertencia: No hay usuarios en la base de datos.");
        }

        return usuarios;
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        System.out.println("Intentando actualizar usuario con ID: " + usuario.getIdUsuario());

        if (!usuarioRepository.existsById(usuario.getIdUsuario())) {
            System.out.println("Error: El usuario no existe.");
            throw new IllegalArgumentException("El usuario no existe.");
        }

        System.out.println("Guardando cambios en la base de datos...");
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        try {
            System.out.println("Enviando correo de actualización de datos...");
            emailService.actualizarDatosEmail(usuario.getEmail());
        } catch (MessagingException e) {
            System.out.println("Error enviando correo de actualización: " + e.getMessage());
        }

        return usuarioActualizado;
    }

    @Override
    public void eliminar(Long idUsuario) {
        System.out.println("Intentando eliminar usuario con ID: " + idUsuario);

        String emailUsuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Usuario> usuarioAutenticadoOpt = usuarioRepository.findByEmail(emailUsuarioAutenticado);

        if (!usuarioAutenticadoOpt.isPresent()) {
            System.out.println("Error: Usuario no autenticado.");
            throw new IllegalArgumentException("Usuario no autenticado.");
        }

        Usuario usuarioAutenticado = usuarioAutenticadoOpt.get();

        if (!usuarioAutenticado.getIdUsuario().equals(idUsuario)) {
            System.out.println("Error: No tienes permiso para eliminar este usuario.");
            throw new IllegalArgumentException("No tienes permiso para eliminar este usuario.");
        }

        if (!usuarioRepository.existsById(idUsuario)) {
            System.out.println("Error: El usuario no existe.");
            throw new IllegalArgumentException("El usuario no existe.");
        }

        usuarioRepository.deleteById(idUsuario);
        System.out.println("Usuario eliminado correctamente.");

        try {
            System.out.println("Enviando correo de eliminación de cuenta...");
            emailService.eliminarCitaEmail(usuarioAutenticado.getEmail());
        } catch (MessagingException e) {
            System.out.println("Error enviando correo de eliminación: " + e.getMessage());
        }
    }

    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        System.out.println("Buscando usuarios con el nombre: " + nombre);
        List<Usuario> usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre);

        if (usuarios.isEmpty()) {
            System.out.println("No se encontraron usuarios con el nombre: " + nombre);
        }

        return usuarios;
    }
}
