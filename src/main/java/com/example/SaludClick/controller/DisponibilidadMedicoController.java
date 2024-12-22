package com.example.SaludClick.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
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
    Optional<Usuario> medicoOpt = usuarioServiceImp.buscarPorEmail(email);

    if (medicoOpt.isPresent() && medicoOpt.get().getRol() == Usuario.Rol.MEDICO) {
        Usuario medico = medicoOpt.get();
        for (DisponibilidadMedico disponibilidad : disponibilidades) {
            disponibilidad.setMedico(medico);
            disponibilidadService.crearDisponibilidad(disponibilidad);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    } else {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}


    @GetMapping("/medico/{idMedico}")
    public ResponseEntity<List<DisponibilidadMedico>> obtenerDisponibilidadPorMedico(@PathVariable Long idMedico) {
        List<DisponibilidadMedico> disponibilidades = disponibilidadService.obtenerDisponibilidadPorMedico(idMedico);
        return new ResponseEntity<>(disponibilidades, HttpStatus.OK);
    }
}
