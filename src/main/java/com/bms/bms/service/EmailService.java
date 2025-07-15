package com.bms.bms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {
    @Value("${spring.mail.username}") private String sender;
    @Autowired private JavaMailSender javaMailSender;
    public String sendCodeMail(String email, String code)
    {

        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(email);
            mailMessage.setText("Enter this code to proceed with resetting your password on the BMS Website: \"" + code + "\"");
            mailMessage.setSubject("BMS Password Reset Code");

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            return "Error while Sending Mail";
        }
    }
}
