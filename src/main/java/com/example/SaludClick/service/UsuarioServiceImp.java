
package com.example.SaludClick.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
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
