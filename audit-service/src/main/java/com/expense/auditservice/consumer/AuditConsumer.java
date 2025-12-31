package com.expense.auditservice.consumer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.expense.auditservice.entity.ActivityLog;
import com.expense.auditservice.repository.ActivityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuditConsumer {

    @Autowired
    private ActivityRepository activityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "activity-log-topic", groupId = "audit-group")
    public void handleActivityEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);

            Long userId = Long.valueOf(payload.get("userId").toString());
            String actionType = payload.get("actionType").toString();
            String description = payload.get("description").toString();

            ActivityLog log = new ActivityLog(userId, actionType, description);
            activityRepository.save(log);

            System.out.println("Saved Audit Log for User " + userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
