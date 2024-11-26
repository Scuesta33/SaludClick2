package com.example.SaludClick.service;

import java.util.List;
import java.util.Optional;

import com.example.SaludClick.model.Usuario;



public interface IUsuarioService {
	Usuario registrar(Usuario usuario);
	Optional<Usuario> buscarPorEmail(String email);
	List<Usuario> listar();
	Usuario actualizar(Usuario usuario);
	void eliminar(Long idUsuario);

}