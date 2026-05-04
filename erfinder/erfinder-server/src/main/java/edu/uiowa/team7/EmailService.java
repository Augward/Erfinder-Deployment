package edu.uiowa.team7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Email Service Component
@Service
public class EmailService {

    // Service Execution Logger
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // Spring Mail Autowire
    @Autowired
    private JavaMailSender mailSender;

    // Send Email Execution
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("erfinder.team7@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Successfully sent email to: " + to);
        } catch (Exception e) {
            logger.error("Failed to send email to " + to, e);
        }
    }
}