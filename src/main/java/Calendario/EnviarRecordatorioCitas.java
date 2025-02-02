package Calendario;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.SaludClick.model.Cita;
import com.example.SaludClick.service.EmailService;
import com.example.SaludClick.service.ICitaService;
import jakarta.mail.MessagingException;

//clase para enviar recordatorio de citas
@Component
public class EnviarRecordatorioCitas {
	@Autowired // se inyectan dependendencias
	private ICitaService citaService;
	@Autowired
	private EmailService emailService;

	@Scheduled(cron = "0 0 0 * * *") // se ejecuta todos los días a medianoche
	public void enviarCitasRecordatorio() {
		LocalDateTime now = LocalDateTime.now();// se obtiene la fecha y hora actual
		LocalDateTime tomorrow = now.plus(1, ChronoUnit.DAYS); // calcula el dia siguiente
		List<Cita> citas = citaService.listarCitas();// listar citas
		int contador = 0;
		if (citas.isEmpty()) {// si no hay citas no se envia
			return;
		}

		try {
			for (Cita cita : citas) {// va iterando por cada cita
				if (cita.getFecha() == null) {// si la cita no tiene fecha no se envia
					continue;
				}
				// aqui se verifica si la fecha de la cita es igual a la fecha de mañana
				if (cita.getFecha().toLocalDate().isEqual(tomorrow.toLocalDate())) {
					try {// si no hay email o paciente no se envia
						if (cita.getPaciente() == null || cita.getPaciente().getEmail() == null) {
							continue;
						}
						// se envia el email con la fecha y sevilla como predeterminado
						String email = cita.getPaciente().getEmail();
						String fechaCita = cita.getFecha().toString();
						emailService.recordatorioCita(email, fechaCita, "Sevilla");
						contador++;

					} catch (MessagingException e) {
						e.printStackTrace(); // imprimir error
					}
				}
			}
		 } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
