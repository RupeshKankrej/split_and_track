package com.expense.auditservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.expense.auditservice.entity.ActivityLog;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserIdOrderByTimestampDesc(Long userId);
}