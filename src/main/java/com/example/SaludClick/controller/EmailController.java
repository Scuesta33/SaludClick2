package com.example.SaludClick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SaludClick.service.EmailService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

   
    @PostMapping("/sendAccountDeletion")
    public String sendAccountDeletionEmail(@RequestParam String toEmail) {
        try {
            emailService.sendAccountDeletionEmail(toEmail);
            return "Account deletion email successfully sent to " + toEmail;
        } catch (MessagingException e) {
            return "Error sending account deletion email: " + e.getMessage();
        }
    } 
    @PostMapping("/sendCredentialUpdate")
    public String sendCredentialUpdateEmail(@RequestParam String toEmail) {
        try {
            emailService.sendCredentialUpdateEmail(toEmail);
            return "Credential update email successfully sent to " + toEmail;
        } catch (MessagingException e) {
            return "Error sending credential update email: " + e.getMessage();
        }
    }
    @PostMapping("/sendRegistration")
    public String sendRegistrationEmail(@RequestParam String toEmail) {
        try {
            emailService.sendRegistrationEmail(toEmail);
            return "Registration email successfully sent to " + toEmail;
        } catch (MessagingException e) {
            return "Error sending registration email: " + e.getMessage();
        }
    }
}
