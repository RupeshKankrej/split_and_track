package com.expense.notificationservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "device_tokens")
public class DeviceToken {

    @Id
    private Long userId;
    private String fcmToken;

    public DeviceToken() {
    }

    public DeviceToken(Long userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
