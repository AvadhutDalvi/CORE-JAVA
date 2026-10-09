package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable double-entry balanced ledger record.
 */
public class LedgerEntry {

    private long entryId;
    private long transactionId;
    private long accountId;
    private EntryType entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;

    public LedgerEntry() {}

    public LedgerEntry(long entryId, long transactionId, long accountId, EntryType entryType,
                       BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        this.entryId = entryId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
        this.balanceAfter = balanceAfter != null ? balanceAfter.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
        this.createdAt = createdAt;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter != null ? balanceAfter.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
