package com.expense.notificationservice.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.expense.notificationservice.controller.TokenController;
import com.expense.notificationservice.entity.DeviceToken;
import com.expense.notificationservice.repository.DeviceTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @KafkaListener(topics = "user-invite-topic", groupId = "notification-group")
    public void handleInviteEvent(String message) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, Map.class);
            String email = payload.get("email");
            String name = payload.get("name");

            sendEmail(email, name);

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String name) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("You've been added to a Split Group!");
        mail.setText("Hello " + name + ",\n\n" +
                "You have been added to a group expense in Split & Track.\n" +
                "Please download the app and register with this email to see your expenses.\n\n" +
                "- The Team");

        mailSender.send(mail);
        System.out.println("Email sent successfully to " + to);
    }

    @KafkaListener(topics = "expense-added-topic", groupId = "notification-group")
    public void handleExpenseEvent(String message) {

        try {

            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            Long targetUserId = Long.valueOf(payload.get("targetUserId").toString());
            String payerName = (String) payload.get("payerName");
            Double amount = Double.valueOf(payload.get("amount").toString());

            DeviceToken deviceToken = deviceTokenRepository.findById(targetUserId).orElse(null);

            if (deviceToken == null) {
                System.err.println(">>> FAILURE: No Token in DB for User " + targetUserId);
            } else {
                String fcmToken = deviceToken.getFcmToken();
                sendPushNotification(fcmToken, "New Expense",
                        payerName + " added an expense. You owe ₹" + amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "group-add-topic", groupId = "notification-group")
    public void handleGroupAddEvent(String message) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, Map.class);
            Long targetUserId = Long.valueOf(payload.get("targetUserId").toString());
            String name = payload.get("name");
            String groupName = payload.get("groupName");
            String addedBy = payload.get("addedBy");

            DeviceToken deviceToken = deviceTokenRepository.findById(targetUserId).orElse(null);

            if (deviceToken == null) {
                System.err.println(">>> FAILURE: No Token in DB for User " + targetUserId);
            } else {
                String fcmToken = deviceToken.getFcmToken();
                sendPushNotification(fcmToken, "Added to Group " + groupName,
                        "Hello " + name + ", you have been added to the group '" + groupName + "' by " + addedBy + ".");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPushNotification(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            System.out.println("Push sent to " + token);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

}
