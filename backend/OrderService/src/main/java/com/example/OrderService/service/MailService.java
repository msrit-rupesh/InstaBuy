package com.example.OrderService.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoicePath(String toEmail, String filePath) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Invoice Generated");
        message.setText("""
                Hello,

                Your invoice has been generated successfully.

                You can access it at the following location:

                """ + filePath + """

                Regards,
                Order Service
                """);

        mailSender.send(message);
    }

    public void sendErrorMessage(String toEmail,String data) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Invoice Generated");
        message.setText("""
                Failed to generate invoice
                
                """+data);

        mailSender.send(message);
    }
}
