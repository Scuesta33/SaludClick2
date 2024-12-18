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

    public void sendConfirmationEmail(String toEmail, String concertName, String concertDate, String concertLocation) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setFrom("scuesta33@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Confirmación de compra - Entrada para " + concertName);
            helper.setText("<h1>¡Gracias por tu compra!</h1>" +
                    "<p>Has comprado una entrada para el concierto: <strong>" + concertName + "</strong></p>" +
                    "<p><strong>Fecha del concierto:</strong> " + concertDate + "</p>" +
                    "<p><strong>Ubicación:</strong> " + concertLocation + "</p>" +
                    "<p>¡Nos vemos en el concierto!</p>", true);

            mailSender.send(message);
            logger.info("Correo enviado a: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error al enviar el correo a " + toEmail, e);
            throw new MessagingException("Error al enviar el correo de confirmación", e);
        }
    }

    public void sendAccountDeletionEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setFrom("scuesta33@gmail.com");
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
            helper.setFrom("scuesta33@gmail.com");
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
            helper.setFrom("scuesta33@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Confimación de registro");
            helper.setText("<h1>Bienvenido a TicketsApp</h1>" +
                    "<p>Tu usuario ha sido registrado exitosamente.</p>", true);

            mailSender.send(message);
            logger.info("Registration email sent to: " + toEmail);

        } catch (MessagingException e) {
            logger.error("Error sending registration email to " + toEmail, e);
            throw new MessagingException("Error sending registration email", e);
        }
    }
}
