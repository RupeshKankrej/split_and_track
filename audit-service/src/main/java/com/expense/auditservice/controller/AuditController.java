package com.expense.auditservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expense.auditservice.entity.ActivityLog;
import com.expense.auditservice.repository.ActivityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private ActivityRepository activityRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<ActivityLog>> getUserActivity(@PathVariable Long userId) {
        List<ActivityLog> log = activityRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(log);
    }

}
