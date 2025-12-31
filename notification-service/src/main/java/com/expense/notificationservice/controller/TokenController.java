package com.expense.notificationservice.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expense.notificationservice.entity.DeviceToken;
import com.expense.notificationservice.repository.DeviceTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/notifications")
public class TokenController {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @PostMapping("/register-device")
    public ResponseEntity<String> registerDevice(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String token = (String) payload.get("token");
        DeviceToken deviceToken = new DeviceToken(userId, token);
        deviceTokenRepository.save(deviceToken);
        System.out.println("PERSISTED Token for User " + userId);
        return ResponseEntity.ok("Token saved to Database");
    }

}
