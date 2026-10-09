package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    private long accountId;
    private String accountNumber;
    private long userId;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient / join display field
    private String ownerName;

    public Account() {
        this.balance = BigDecimal.ZERO.setScale(2);
        this.accountType = AccountType.SAVINGS;
        this.status = AccountStatus.ACTIVE;
    }

    public Account(long accountId, String accountNumber, long userId, AccountType accountType,
                   BigDecimal balance, AccountStatus status) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.accountType = accountType != null ? accountType : AccountType.SAVINGS;
        this.balance = balance != null ? balance.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
        this.status = status != null ? status : AccountStatus.ACTIVE;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return accountNumber + " (" + accountType + " - Balance: ₹" + balance.toPlainString() + ")";
    }
}
