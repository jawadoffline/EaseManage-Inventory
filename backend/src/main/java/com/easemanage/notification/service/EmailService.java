package com.easemanage.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@easemanage.com}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean emailEnabled;

    @Async
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email disabled. Would send to={}, subject={}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to={}, subject={}: {}", to, subject, e.getMessage());
        }
    }

    public void sendLowStockAlert(String to, String productName, int quantity) {
        sendEmail(to, "Low Stock Alert: " + productName,
            "The stock for '" + productName + "' has dropped to " + quantity + " units.\n\n" +
            "Please review and place a purchase order if needed.\n\n" +
            "— EaseManage Inventory System");
    }

    public void sendOrderStatusUpdate(String to, String orderNumber, String newStatus) {
        sendEmail(to, "Order Status Update: " + orderNumber,
            "Order " + orderNumber + " has been updated to status: " + newStatus + ".\n\n" +
            "— EaseManage Inventory System");
    }
}
