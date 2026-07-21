package com.rolemanagement.starter.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirm your email");
        message.setText("Use this token to verify your account: " + token);
        mailSender.send(message);
    }

    public void sendInvitationEmail(String to, String organisationName, Long invitationId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("You've been invited to join " + organisationName);
        message.setText("You've been invited to join " + organisationName
                + ". Use this invitation id to accept: " + invitationId);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset your password");
        message.setText("Use this token to reset your password: " + token);
        mailSender.send(message);
    }
}
