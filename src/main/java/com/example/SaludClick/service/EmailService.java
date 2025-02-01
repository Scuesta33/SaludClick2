package com.example.SaludClick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	// Método generico para enviar correos
	private void enviarEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
		System.out.println("Intentando enviar correo a: " + toEmail);

		if (toEmail == null || toEmail.isEmpty()) {
			System.out.println("Error: El email del destinatario es inválido.");
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom("SaludClick <scuesta33@gmail.com>");
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			System.out.println("Correo enviado correctamente a: " + toEmail);
		} catch (MailAuthenticationException e) {
			System.out.println("Error de autenticación: " + e.getMessage());
			throw new MessagingException("Error de autenticación en el servidor de correo", e);
		} catch (MailException e) {
			System.out.println("Error enviando el correo: " + e.getMessage());
			throw new MessagingException("Error en el envío del correo", e);
		}
	}

	// Métodos de envío de correos

	public void emailCitaCreada(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
		System.out.println("Enviando correo de cita creada...");
		String htmlContent = "<h1>¡Tu cita ha sido creada!</h1>" + "<p><strong>Fecha:</strong> " + citaFecha + "</p>"
				+ "<p><strong>Ubicación:</strong> " + citaLocation + "</p>";
		enviarEmail(toEmail, "Cita Creada", htmlContent);
	}

	public void actualizarCitaEmail(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
		System.out.println("Enviando correo de actualización de cita...");
		String htmlContent = "<h1>¡Tu cita ha sido actualizada!</h1>" + "<p><strong>Nueva fecha:</strong> " + citaFecha
				+ "</p>" + "<p><strong>Ubicación:</strong> " + citaLocation + "</p>";
		enviarEmail(toEmail, "Cita Actualizada", htmlContent);
	}

	public void cancelarCitaEmail(String toEmail) throws MessagingException {
		System.out.println("Enviando correo de cancelación de cita...");
		String htmlContent = "<h1>Tu cita ha sido cancelada</h1>" + "<p>Se ha cancelado correctamente.</p>";
		enviarEmail(toEmail, "Cita Cancelada", htmlContent);
	}

	public void eliminarCitaEmail(String toEmail) throws MessagingException {
		System.out.println("Enviando correo de eliminación de cuenta...");
		String htmlContent = "<h1>Tu cuenta ha sido eliminada</h1>"
				+ "<p>Tu cuenta asociada a este email ha sido eliminada.</p>";
		enviarEmail(toEmail, "Confirmación de eliminación de cuenta", htmlContent);
	}

	public void actualizarDatosEmail(String toEmail) throws MessagingException {
		System.out.println("Enviando correo de actualización de credenciales...");
		String htmlContent = "<h1>Tus credenciales han sido actualizadas</h1>"
				+ "<p>Se han actualizado correctamente.</p>";
		enviarEmail(toEmail, "Confirmación de actualización de credenciales", htmlContent);
	}

	public void registroEmail(String toEmail) throws MessagingException {
		System.out.println("Enviando correo de confirmación de registro...");
		String htmlContent = "<h1>Bienvenido a SaludClick</h1>" + "<p>Tu usuario ha sido registrado exitosamente.</p>";
		enviarEmail(toEmail, "Confirmación de registro", htmlContent);
	}

	public void recordatorioCita(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
		System.out.println("Enviando correo de recordatorio de cita...");
		String htmlContent = "<h1>Recordatorio de tu cita</h1>" + "<p><strong>Fecha:</strong> " + citaFecha + "</p>"
				+ "<p><strong>Ubicación:</strong> " + citaLocation + "</p>";
		enviarEmail(toEmail, "Recordatorio de Cita", htmlContent);
	}

	public void citaAceptadaEmail(String toEmail, String citaFecha, String citaLocation) throws MessagingException {
		System.out.println("Enviando correo de aceptación de cita...");
		String htmlContent = "<h1>¡Tu cita ha sido aceptada!</h1>" + "<p><strong>Fecha:</strong> " + citaFecha + "</p>"
				+ "<p><strong>Ubicación:</strong> " + citaLocation + "</p>";
		enviarEmail(toEmail, "Cita Aceptada", htmlContent);
	}

	public void citaRechazadaEmail(String toEmail) throws MessagingException {
		System.out.println("Enviando correo de rechazo de cita...");
		String htmlContent = "<h1>Tu cita ha sido rechazada</h1>" + "<p>Tu cita ha sido rechazada.</p>";
		enviarEmail(toEmail, "Cita Rechazada", htmlContent);
	}

	public void notificacionEmail(String toEmail, String tipoNotificacion, String mensaje) throws MessagingException {
		System.out.println("Enviando correo de notificación...");
		String htmlContent = "<h1>Has recibido una nueva notificación</h1>" + "<p><strong>Tipo:</strong> "
				+ tipoNotificacion + "</p>" + "<p>" + mensaje + "</p>";
		enviarEmail(toEmail, "Nueva Notificación: " + tipoNotificacion, htmlContent);
	}
}
