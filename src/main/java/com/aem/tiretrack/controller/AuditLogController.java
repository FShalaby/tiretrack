package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.AuditLogResponse;
import com.aem.tiretrack.service.AuditLogService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> latest() {
        return auditLogService.latest().stream().map(AuditLogResponse::new).toList();
    }
}
