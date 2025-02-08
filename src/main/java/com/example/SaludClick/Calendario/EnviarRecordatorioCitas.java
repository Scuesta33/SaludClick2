package com.example.SaludClick.Calendario;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.SaludClick.model.Cita;
import com.example.SaludClick.model.Usuario;
import com.example.SaludClick.service.CitaService;
import com.example.SaludClick.service.EmailService;
import jakarta.mail.MessagingException;

@Component
public class EnviarRecordatorioCitas {
	@Autowired // se inyectan dependendencias
	private CitaService citaService;
	@Autowired
	private EmailService emailService;
// a esta hora salta el recordatorio, si el dia siguiente hay citas
	@Scheduled(cron = "0 10 11 * * *", zone = "Europe/Madrid")
	public void enviarCitasRecordatorio() {
		LocalDate mañana = LocalDate.now().plusDays(1);
		List<Cita> citas = citaService.listarCitas();
		if (citas.isEmpty()) {
			System.out.println("no hay citas programadas para mañana.");
			return;
		}
		for (Cita cita : citas) {
			try {
				if (cita.getFecha() != null && cita.getFecha().toLocalDate().isEqual(mañana)) {
					Usuario paciente = cita.getPaciente();
					if (paciente != null && paciente.getRol() == Usuario.Rol.PACIENTE && paciente.getEmail() != null
							&& !paciente.getEmail().isEmpty()) {
						System.out.println("enviando correo a: " + paciente.getEmail());
						emailService.recordatorioCita(paciente.getEmail(), cita.getFecha().toString(), "Sevilla");
						
					} else {
					}
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}