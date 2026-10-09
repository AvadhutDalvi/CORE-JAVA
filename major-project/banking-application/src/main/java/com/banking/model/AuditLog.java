package com.banking.model;

import java.time.LocalDateTime;

public class AuditLog {

    private long logId;
    private Long userId;
    private String usernameAttempted;
    private AuditEventType eventType;
    private String sourceIp;
    private String details;
    private String status; // SUCCESS or FAILURE
    private LocalDateTime createdAt;

    public AuditLog() {
        this.sourceIp = "127.0.0.1";
        this.status = "SUCCESS";
    }

    public AuditLog(long logId, Long userId, String usernameAttempted, AuditEventType eventType,
                    String sourceIp, String details, String status, LocalDateTime createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.usernameAttempted = usernameAttempted;
        this.eventType = eventType;
        this.sourceIp = sourceIp != null ? sourceIp : "127.0.0.1";
        this.details = details;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsernameAttempted() {
        return usernameAttempted;
    }

    public void setUsernameAttempted(String usernameAttempted) {
        this.usernameAttempted = usernameAttempted;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
