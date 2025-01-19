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

@Component
public class CitaReminderScheduler {
	@Autowired
    private ICitaService citaService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 0 * * *") //corre todos los dias a medianoche
    public void sendCitaReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plus(1, ChronoUnit.DAYS);

        List<Cita> citas = citaService.listarCitas();
        for (Cita cita : citas) {
            if (cita.getFecha().toLocalDate().isEqual(tomorrow.toLocalDate())) {
                try {
                    emailService.sendCitaReminderEmail(cita.getPaciente().getEmail(), cita.getFecha().toString(), "Sevilla");
                } catch (MessagingException e) {
                    
                }
            }
        }
    }
}
