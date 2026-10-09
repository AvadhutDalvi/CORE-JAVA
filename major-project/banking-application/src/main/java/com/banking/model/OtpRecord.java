package com.banking.model;

import java.time.LocalDateTime;

public class OtpRecord {

    private long otpId;
    private long userId;
    private String otpHash;
    private String purpose;
    private String referenceId;
    private LocalDateTime expiresAt;
    private int attemptsCount;
    private int maxAttempts;
    private boolean consumed;
    private LocalDateTime createdAt;

    public OtpRecord() {
        this.maxAttempts = 3;
        this.consumed = false;
    }

    public OtpRecord(long otpId, long userId, String otpHash, String purpose, String referenceId,
                     LocalDateTime expiresAt, int attemptsCount, int maxAttempts, boolean consumed,
                     LocalDateTime createdAt) {
        this.otpId = otpId;
        this.userId = userId;
        this.otpHash = otpHash;
        this.purpose = purpose;
        this.referenceId = referenceId;
        this.expiresAt = expiresAt;
        this.attemptsCount = attemptsCount;
        this.maxAttempts = maxAttempts;
        this.consumed = consumed;
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasExceededAttempts() {
        return attemptsCount >= maxAttempts;
    }

    public boolean isValid() {
        return !consumed && !isExpired() && !hasExceededAttempts();
    }

    public long getOtpId() {
        return otpId;
    }

    public void setOtpId(long otpId) {
        this.otpId = otpId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(int attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
