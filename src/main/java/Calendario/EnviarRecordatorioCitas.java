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
public class EnviarRecordatorioCitas { // Nombre en PascalCase para seguir convenciones de Java

    @Autowired
    private ICitaService citaService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 0 * * *") // Se ejecuta todos los días a medianoche
    public void sendCitaReminders() {
        System.out.println(">>> Iniciando envío de recordatorios de citas...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plus(1, ChronoUnit.DAYS);
        
        List<Cita> citas = citaService.listarCitas();
        int contador = 0;

        if (citas.isEmpty()) {
            System.out.println("No hay citas registradas para revisar.");
            return;
        }

        try {
            for (Cita cita : citas) {
                if (cita.getFecha() == null) {
                    System.out.println("Advertencia: La cita con ID " + cita.getIdCita() + " no tiene fecha asignada.");
                    continue;
                }

                if (cita.getFecha().toLocalDate().isEqual(tomorrow.toLocalDate())) {
                    try {
                        if (cita.getPaciente() == null || cita.getPaciente().getEmail() == null) {
                            System.out.println("Error: La cita con ID " + cita.getIdCita() + " no tiene un paciente con email.");
                            continue;
                        }

                        String email = cita.getPaciente().getEmail();
                        String fechaCita = cita.getFecha().toString();
                        emailService.recordatorioCita(email, fechaCita, "Sevilla");
                        contador++;
                        System.out.println("Recordatorio enviado a: " + email);

                    } catch (MessagingException e) {
                        System.err.println("Error enviando recordatorio a " + cita.getPaciente().getEmail() + ": " + e.getMessage());
                        e.printStackTrace(); 
                    }
                }
            }

            System.out.println("Envío de recordatorios finalizado. Total enviados: " + contador);
        } catch (Exception e) {
            System.out.println("Error inesperado durante el envío de recordatorios: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
