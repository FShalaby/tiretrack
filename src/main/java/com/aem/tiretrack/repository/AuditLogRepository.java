package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aem.tiretrack.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop25ByOrderByCreatedAtDesc();

    @Modifying
    @Query("update AuditLog log set log.performedBy = 'legacy-system' where log.performedBy is null or log.performedBy = 'system'")
    int relabelLegacySystemActors();
}
