package com.lifesamadhan.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String providerName, String serviceName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Service OTP - LifeSamadhan");
            message.setText("Dear Customer,\n\n" +
                    "Your service request for '" + serviceName + "' has been accepted by " + providerName + ".\n" +
                    "Please share this OTP with the provider to START the service:\n\n" +
                    "OTP: " + otp + "\n\n" +
                    "Do not share this OTP with anyone else until the provider arrives.\n\n" +
                    "Regards,\n" +
                    "Team LifeSamadhan");

            mailSender.send(message);
            log.info("OTP Email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            
        }
    }
}
