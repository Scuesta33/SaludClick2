package com.example.SaludClick.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.repository.UsuarioRepository;


@Service
public class UsuarioServiceImp implements IUsuarioService {
     @Autowired
     private UsuarioRepository usuarioRepository;
	@Override
	public Usuario registrar(Usuario usuario) { //registrar usuario
		//ver que el email no exista
		if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
		    throw new IllegalArgumentException("El email ya está registrado.");
		}
		usuario.setActivo(true);
		return usuarioRepository.save(usuario);

	}

	@Override
	public Optional<Usuario> buscarPorEmail(String email) { //buscar por email
		return usuarioRepository.findByEmail(email)
				;}

	@Override
	public List<Usuario> listar() {     //listar los usuarios
		return usuarioRepository.findAll();
	}

	@Override
	public Usuario actualizar(Usuario usuario) {   //actualizar usuario
		return usuarioRepository.save(usuario);
	}

	@Override
	public void eliminar(Long idUsuario) {      //eliminar usuario
		usuarioRepository.deleteById(idUsuario);
	}

	

	
	

}