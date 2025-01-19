package com.example.SaludClick.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SaludClick.model.DisponibilidadMedico;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.DisponibilidadService;
import com.example.SaludClick.service.UsuarioServiceImp;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/disponibilidad")
public class DisponibilidadMedicoController {

	@Autowired
	private DisponibilidadService disponibilidadService;

	@Autowired
	private UsuarioServiceImp usuarioServiceImp;

	@PostMapping("/crear")
	public ResponseEntity<?> crearDisponibilidad(@RequestBody List<DisponibilidadMedico> disponibilidades) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = ((UserDetails) authentication.getPrincipal()).getUsername();
		System.out.println("Usuario autenticado: " + email);
		Optional<Usuario> medicoOpt = usuarioServiceImp.buscarPorEmail(email);

		if (medicoOpt.isPresent()) {
			Usuario medico = medicoOpt.get();
			System.out.println("Rol del usuario: " + medico.getRol());
			if (medico.getRol() == Usuario.Rol.MEDICO) {
				for (DisponibilidadMedico disponibilidad : disponibilidades) {
					disponibilidad.setMedico(medico);
					disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().minusHours(1));
					disponibilidad.setHoraFin(disponibilidad.getHoraFin().minusHours(1));
					disponibilidadService.crearDisponibilidad(disponibilidad);
				}
				return new ResponseEntity<>(HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping("/medico")
	public ResponseEntity<List<DisponibilidadMedico>> obtenerDisponibilidadPorMedico() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();

		if (principal instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) principal;
			Optional<Usuario> usuarioOpt = usuarioServiceImp.buscarPorEmail(userDetails.getUsername());

			if (usuarioOpt.isPresent()) {
				Usuario usuario = usuarioOpt.get();
				if (usuario.getRol() == Usuario.Rol.MEDICO) {
					List<DisponibilidadMedico> disponibilidades = disponibilidadService
							.obtenerDisponibilidadPorMedico(usuario.getIdUsuario());

					// Ajustar las horas antes de devolver las disponibilidades porque me daba
					// problemas con las horas
					disponibilidades.forEach(disponibilidad -> {
						disponibilidad.setHoraInicio(disponibilidad.getHoraInicio().plusHours(1));
						disponibilidad.setHoraFin(disponibilidad.getHoraFin().plusHours(1));
					});

					return new ResponseEntity<>(disponibilidades, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				}
			} else {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminarDisponibilidad(@PathVariable Long id) {
		disponibilidadService.eliminarDisponibilidad(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/todas")
	public ResponseEntity<List<DisponibilidadMedico>> obtenerTodasLasDisponibilidades() {
		List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerTodasLasDisponibilidades();
		return ResponseEntity.ok(disponibilidades);
	}
}