package com.expense.identityservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendInviteEmail(String to, String inviterName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("You've been added to a Split Group!");
        message.setText("Hello! " + inviterName + " added you to a group expense. " +
                "Register on our app with this email to see the details.");
        mailSender.send(message);
    }
}
