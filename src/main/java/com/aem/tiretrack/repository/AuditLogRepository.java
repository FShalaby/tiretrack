package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop25ByOrderByCreatedAtDesc();
}
