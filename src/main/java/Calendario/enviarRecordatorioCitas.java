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
public class enviarRecordatorioCitas { 

    @Autowired
    private ICitaService citaService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 0 * * *") // Se ejecuta todos los días a medianoche
    public void sendCitaReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plus(1, ChronoUnit.DAYS);

        // Obtener solo las citas de mañana en lugar de recorrer todas
        List<Cita> citas = citaService.listarCitas(); // 
        int contador = 0;

        System.out.println("Iniciando envío de recordatorios");

        for (Cita cita : citas) {
            if (cita.getFecha().toLocalDate().isEqual(tomorrow.toLocalDate())) {
                try {
                    String email = cita.getPaciente().getEmail();
                    String fechaCita = cita.getFecha().toString();

                    emailService.recordatorioCita(email, fechaCita, "Sevilla");
                    contador++;
                    System.out.println("Recordatorio enviado a: " + email);

                } catch (MessagingException e) {
                    System.err.println("Error enviando recordatorio a " + cita.getPaciente().getEmail() + ": " + e.getMessage());
                    e.printStackTrace(); // Imprimir el error exacto
                }
            }
        }

        System.out.println("Envío de recordatorios finalizado. Total enviados: " + contador);
    }
}