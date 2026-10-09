package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Encapsulates a validated pending transfer request awaiting OTP authentication.
 */
public class TransferDraft {

    private final String draftId;
    private final long userId;
    private final long sourceAccountId;
    private final String sourceAccountNumber;
    private final long destinationAccountId;
    private final String destinationAccountNumber;
    private final String destinationOwnerName;
    private final BigDecimal amount;
    private final String remarks;
    private final LocalDateTime initiatedAt;

    public TransferDraft(String draftId, long userId, long sourceAccountId, String sourceAccountNumber,
                         long destinationAccountId, String destinationAccountNumber,
                         String destinationOwnerName, BigDecimal amount, String remarks) {
        this.draftId = draftId;
        this.userId = userId;
        this.sourceAccountId = sourceAccountId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountId = destinationAccountId;
        this.destinationAccountNumber = destinationAccountNumber;
        this.destinationOwnerName = destinationOwnerName;
        this.amount = amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
        this.remarks = remarks;
        this.initiatedAt = LocalDateTime.now();
    }

    public String getDraftId() {
        return draftId;
    }

    public long getUserId() {
        return userId;
    }

    public long getSourceAccountId() {
        return sourceAccountId;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public long getDestinationAccountId() {
        return destinationAccountId;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public String getDestinationOwnerName() {
        return destinationOwnerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getInitiatedAt() {
        return initiatedAt;
    }
}
