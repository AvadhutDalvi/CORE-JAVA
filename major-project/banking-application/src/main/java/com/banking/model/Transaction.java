package com.banking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private long transactionId;
    private String transactionReference;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;

    // Display fields
    private String sourceAccountNumber;
    private String destinationAccountNumber;

    public Transaction() {
        this.status = TransactionStatus.SUCCESS;
        this.amount = BigDecimal.ZERO.setScale(2);
    }

    public Transaction(long transactionId, String transactionReference, Long sourceAccountId,
                       Long destinationAccountId, BigDecimal amount, TransactionType transactionType,
                       TransactionStatus status, String description, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
        this.transactionType = transactionType;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "ref='" + transactionReference + '\'' +
                ", type=" + transactionType +
                ", amount=₹" + amount +
                ", status=" + status +
                '}';
    }
}
