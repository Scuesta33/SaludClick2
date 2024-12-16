package com.example.SaludClick.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SaludClick.model.Usuario;

public interface UsuarioRepository  extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    // Método para buscar por nombre
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
}