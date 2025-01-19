package com.example.SaludClick.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendCitaCreationEmail(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        citaLocation = "Sevilla";
        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Cita Creada");
            helper.setText("<h1>¡Tu cita ha sido creada!</h1>" +
                    "<p>Fecha de la cita: <strong>" + citaFecha + "</strong></p>" +
                    "<p>Ubicación: " + citaLocation + "</p>", true);

            mailSender.send(message);
            logger.info("Correo de creación de cita enviado a: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error al enviar el correo de creación de cita a " + toEmail, e);
            throw new MessagingException("Error al enviar el correo de creación de cita", e);
        }
    }

    public void sendCitaUpdateEmail(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        citaLocation = "Sevilla";
        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Cita Actualizada");
            helper.setText("<h1>¡Tu cita ha sido actualizada!</h1>" +
                    "<p>Nueva fecha de la cita: <strong>" + citaFecha + "</strong></p>" +
                    "<p>Ubicación: " + citaLocation + "</p>", true);

            mailSender.send(message);
            logger.info("Correo de actualización de cita enviado a: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error al enviar el correo de actualización de cita a " + toEmail, e);
            throw new MessagingException("Error al enviar el correo de actualización de cita", e);
        }
    }

    public void sendCitaDeletionEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Cita Cancelada");
            helper.setText("<h1>Tu cita ha sido cancelada</h1>" +
                    "<p>Tu cita ha sido cancelada exitosamente.</p>", true);

            mailSender.send(message);
            logger.info("Correo de cancelación de cita enviado a: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error al enviar el correo de cancelación de cita a " + toEmail, e);
            throw new MessagingException("Error al enviar el correo de cancelación de cita", e);
        }
    }


    public void sendAccountDeletionEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Confirmacion de eliminacion de cuenta");
            helper.setText("<h1>Tu cuenta ha sido eliminada</h1>" +
                    "<p>Tu cuenta asociada a con este email ha sido eliminada</p>", true);

            mailSender.send(message);
            logger.info("Account deletion email sent to: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error sending account deletion email to " + toEmail, e);
            throw new MessagingException("Error sending account deletion email", e);
        }
    }

    public void sendCredentialUpdateEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Confirmación de actualización de credenciales");
            helper.setText("<h1>Tus credenciales han sido actualizadas</h1>" +
                    "<p>Tus credenciales de cuenta han sido exitosamente actualizados.</p>", true);

            mailSender.send(message);
            logger.info("Credential update email sent to: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error sending credential update email to " + toEmail, e);
            throw new MessagingException("Error sending credential update email", e);
        }
    }

    public void sendRegistrationEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Confimación de registro");
            helper.setText("<h1>Bienvenido a SaludClick</h1>" +
                    "<p>Tu usuario ha sido registrado exitosamente.</p>", true);

            mailSender.send(message);
            logger.info("Registration email sent to: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error sending registration email to " + toEmail, e);
            throw new MessagingException("Error sending registration email", e);
        }
    }
    public void sendCitaReminderEmail(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
        	helper.setFrom("SaludClick <scuesta33@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Recordatorio de Cita");
            helper.setText("<h1>Recordatorio de tu cita</h1>" +
                    "<p>Fecha de la cita: <strong>" + citaFecha + "</strong></p>" +
                    "<p>Ubicación: " + citaLocation + "</p>", true);

            mailSender.send(message);
            logger.info("Correo de recordatorio de cita enviado a: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error al enviar el correo de recordatorio de cita a " + toEmail, e);
            throw new MessagingException("Error al enviar el correo de recordatorio de cita", e);
        }
    }
    

public void sendCitaAcceptanceEmail(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    citaLocation = "Sevilla";
    try {
        helper.setFrom("SaludClick <scuesta33@gmail.com>");
        helper.setTo(toEmail);
        helper.setSubject("Cita Aceptada");
        helper.setText("<h1>¡Tu cita ha sido aceptada!</h1>" +
                "<p>Fecha de la cita: <strong>" + citaFecha + "</strong></p>" +
                "<p>Ubicación: " + citaLocation + "</p>", true);

        mailSender.send(message);
        logger.info("Correo de aceptación de cita enviado a: " + toEmail);

    } catch (MessagingException e) {
        logger.error("Error al enviar el correo de aceptación de cita a " + toEmail, e);
        throw new MessagingException("Error al enviar el correo de aceptación de cita", e);
    }
}

public void sendCitaRejectionEmail(String toEmail) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);

    try {
        helper.setFrom("SaludClick <scuesta33@gmail.com>");
        helper.setTo(toEmail);
        helper.setSubject("Cita Rechazada");
        helper.setText("<h1>Tu cita ha sido rechazada</h1>" +
                "<p>Tu cita ha sido rechazada .</p>", true);

        mailSender.send(message);
        logger.info("Correo de rechazo de cita enviado a: " + toEmail);

    } catch (MessagingException e) {
        logger.error("Error al enviar el correo de rechazo de cita a " + toEmail, e);
        throw new MessagingException("Error al enviar el correo de rechazo de cita", e);
    }
}

}
