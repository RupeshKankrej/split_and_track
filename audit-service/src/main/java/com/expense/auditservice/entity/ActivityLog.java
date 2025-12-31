package com.expense.auditservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "t_activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String actionType;
    private String description;
    private LocalDateTime timestamp;

    public ActivityLog() {
    }

    public ActivityLog(Long userId, String actionType, String description) {
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
