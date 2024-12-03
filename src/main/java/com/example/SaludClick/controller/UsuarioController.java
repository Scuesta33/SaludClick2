package com.example.SaludClick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.UsuarioServiceImp;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
@Autowired
	private UsuarioServiceImp usuarioServiceImp;
@PostMapping("/registrar")
	public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario usuario) {
		Usuario nuevoUsuario = usuarioServiceImp.registrar(usuario);
		return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
	}
@GetMapping("/{email}")
public ResponseEntity<Usuario> traerUsuarioPorEmail(@PathVariable String email) {
  return usuarioServiceImp.buscarPorEmail(email)
            .map(ResponseEntity::ok)  // Transforma el Usuario en ResponseEntity con HTTP 200
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));  // Lanza una excepción si no se encuentra
}

@PutMapping("/actualizar")
public ResponseEntity<Usuario> actualizarUsuario(@Valid @RequestBody Usuario usuario) {
	 return ResponseEntity.ok(usuarioServiceImp.actualizar(usuario));
}

@DeleteMapping("/eliminar/{idUsuario}")
public ResponseEntity<Void>eliminarUsuario(@PathVariable Long idUsuario){
	try {
		usuarioServiceImp.eliminar(idUsuario);
        return ResponseEntity.noContent().build();
    } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado", e);
    }
	
}


}
