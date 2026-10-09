package com.banking.service;

import com.banking.dao.AuditLogDAO;
import com.banking.model.AuditEventType;
import com.banking.model.AuditLog;

import java.util.List;

/**
 * Service for logging and querying security and operational audit trails.
 */
public class AuditService {

    private final AuditLogDAO auditLogDAO;

    public AuditService() {
        this.auditLogDAO = new AuditLogDAO();
    }

    public AuditService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    public void log(Long userId, String usernameAttempted, AuditEventType eventType, String details, boolean success) {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setUsernameAttempted(usernameAttempted);
            log.setEventType(eventType);
            log.setDetails(details);
            log.setStatus(success ? "SUCCESS" : "FAILURE");
            log.setSourceIp("127.0.0.1");
            auditLogDAO.logEvent(log);
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> getLogsForUser(long userId) {
        return auditLogDAO.getLogsByUserId(userId);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.getRecentLogs(limit);
    }
}
