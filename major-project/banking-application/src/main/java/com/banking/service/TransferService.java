package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.LedgerDAO;
import com.banking.dao.OtpDAO;
import com.banking.dao.TransactionDAO;
import com.banking.dao.UserDAO;
import com.banking.model.*;
import com.banking.security.OtpDeliveryService;
import com.banking.security.OtpUtil;
import com.banking.security.SimulatedOtpDeliveryService;
import com.banking.security.UserSession;
import com.banking.util.BankingException;
import com.banking.util.DBConnection;
import com.banking.util.ValidationException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-integrity financial fund transfer service.
 * Enforces atomic database transactions, pessimistic lock ordering (deadlock prevention),
 * double-entry accounting ledger entries, and cryptographically secure OTP verification.
 */
public class TransferService {

    public static final BigDecimal MAX_TRANSFER_LIMIT = new BigDecimal("500000.00");
    public static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1.00");

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final LedgerDAO ledgerDAO;
    private final OtpDAO otpDAO;
    private final UserDAO userDAO;
    private final AuditService auditService;
    private final OtpDeliveryService otpDeliveryService;

    // In-memory cache for active transfer drafts awaiting OTP verification
    private final Map<String, TransferDraft> activeDrafts = new ConcurrentHashMap<>();

    public TransferService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.ledgerDAO = new LedgerDAO();
        this.otpDAO = new OtpDAO();
        this.userDAO = new UserDAO();
        this.auditService = new AuditService();
        this.otpDeliveryService = SimulatedOtpDeliveryService.getInstance();
    }

    public TransferService(AccountDAO accountDAO, TransactionDAO transactionDAO,
                           LedgerDAO ledgerDAO, OtpDAO otpDAO, UserDAO userDAO,
                           AuditService auditService, OtpDeliveryService otpDeliveryService) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.ledgerDAO = ledgerDAO;
        this.otpDAO = otpDAO;
        this.userDAO = userDAO;
        this.auditService = auditService;
        this.otpDeliveryService = otpDeliveryService != null ? otpDeliveryService : SimulatedOtpDeliveryService.getInstance();
    }

    /**
     * Stage 1: Initiates a fund transfer request.
     * Validates business rules, creates a pending TransferDraft, generates and hashes OTP,
     * and dispatches the OTP via the delivery service.
     */
    public TransferDraft initiateTransfer(long userId, long sourceAccountId,
                                          String destinationAccountNumber,
                                          BigDecimal amount, String remarks) {

        // 1. Amount validation
        if (amount == null) {
            throw new ValidationException("Transfer amount is required.");
        }
        if (amount.scale() > 2) {
            throw new ValidationException("Transfer amount cannot have more than 2 decimal places.");
        }
        if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            throw new ValidationException("Minimum transfer amount is ₹" + MIN_TRANSFER_AMOUNT.toPlainString());
        }
        if (amount.compareTo(MAX_TRANSFER_LIMIT) > 0) {
            throw new ValidationException("Transfer amount exceeds maximum per-transaction limit of ₹" + MAX_TRANSFER_LIMIT.toPlainString());
        }

        // 2. Source Account validation & authorization
        Account sourceAccount = accountDAO.findById(sourceAccountId)
                .orElseThrow(() -> new ValidationException("Source account not found."));

        if (sourceAccount.getUserId() != userId) {
            auditService.log(userId, null, AuditEventType.TRANSFER_INITIATED,
                    "Unauthorized transfer attempt on account ID " + sourceAccountId, false);
            throw new ValidationException("Access denied: You are not authorized to transfer from this account.");
        }

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Source account is " + sourceAccount.getStatus() + " and cannot send funds.");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Insufficient balance. Available balance: ₹" + sourceAccount.getBalance().toPlainString());
        }

        // 3. Destination Account validation
        if (destinationAccountNumber == null || destinationAccountNumber.trim().isEmpty()) {
            throw new ValidationException("Destination account number is required.");
        }

        Account destAccount = accountDAO.findByAccountNumber(destinationAccountNumber.trim())
                .orElseThrow(() -> new ValidationException("Destination account '" + destinationAccountNumber + "' does not exist."));

        if (destAccount.getAccountId() == sourceAccount.getAccountId()) {
            throw new ValidationException("Cannot transfer funds to the same source account.");
        }

        if (destAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Destination account is " + destAccount.getStatus() + " and cannot receive funds.");
        }

        // 4. Create unique TransferDraft
        String draftId = UUID.randomUUID().toString();
        String safeRemarks = (remarks != null && !remarks.trim().isEmpty()) ? remarks.trim() : "Fund Transfer";
        TransferDraft draft = new TransferDraft(
                draftId,
                userId,
                sourceAccount.getAccountId(),
                sourceAccount.getAccountNumber(),
                destAccount.getAccountId(),
                destAccount.getAccountNumber(),
                destAccount.getOwnerName(),
                amount,
                safeRemarks
        );

        activeDrafts.put(draftId, draft);

        // 5. Invalidate previous OTPs for this user and purpose
        otpDAO.invalidateUserOtps(userId, "FUND_TRANSFER");

        // 6. Generate cryptographically secure OTP
        String plainOtp = OtpUtil.generateOtp();
        String hashedOtp = OtpUtil.hashOtp(plainOtp);

        OtpRecord otpRecord = new OtpRecord();
        otpRecord.setUserId(userId);
        otpRecord.setOtpHash(hashedOtp);
        otpRecord.setPurpose("FUND_TRANSFER");
        otpRecord.setReferenceId(draftId);
        otpRecord.setExpiresAt(OtpUtil.calculateExpiryTime(OtpUtil.DEFAULT_EXPIRY_MINUTES));
        otpRecord.setAttemptsCount(0);
        otpRecord.setMaxAttempts(OtpUtil.DEFAULT_MAX_ATTEMPTS);
        otpRecord.setConsumed(false);

        boolean otpSaved = otpDAO.saveOtp(otpRecord);
        if (!otpSaved) {
            activeDrafts.remove(draftId);
            throw new BankingException("Failed to initiate OTP security verification. Please try again.");
        }

        // 7. Deliver OTP
        Optional<User> userOpt = userDAO.findById(userId);
        String destinationContact = userOpt.map(u -> u.getEmail() + " / " + u.getPhone()).orElse("registered device");
        otpDeliveryService.deliverOtp(destinationContact, plainOtp, "Fund Transfer of ₹" + amount.toPlainString());

        auditService.log(userId, null, AuditEventType.TRANSFER_INITIATED,
                "Transfer initiated: ₹" + amount + " to " + destAccount.getAccountNumber() + " [Draft: " + draftId + "]", true);
        auditService.log(userId, null, AuditEventType.OTP_GENERATED,
                "Security OTP generated for transfer draft " + draftId, true);

        return draft;
    }

    /**
     * Stage 2: Verifies the OTP and executes the atomic database transaction.
     */
    public Transaction completeTransfer(String draftId, String enteredOtp) {
        if (draftId == null || !activeDrafts.containsKey(draftId)) {
            throw new ValidationException("Invalid or expired transfer session. Please initiate a new transfer.");
        }

        TransferDraft draft = activeDrafts.get(draftId);
        if (enteredOtp == null || enteredOtp.trim().isEmpty()) {
            throw new ValidationException("Please enter the 6-digit OTP.");
        }

        String cleanOtp = enteredOtp.trim();

        // 1. Fetch OTP record
        OtpRecord otpRecord = otpDAO.getLatestValidOtp(draft.getUserId(), "FUND_TRANSFER", draftId)
                .orElseThrow(() -> new ValidationException("OTP has expired or is invalid. Please request a new transfer."));

        if (!otpRecord.isValid()) {
            activeDrafts.remove(draftId);
            auditService.log(draft.getUserId(), null, AuditEventType.OTP_FAILED,
                    "Attempted use of invalid/expired OTP for draft " + draftId, false);
            throw new ValidationException("OTP is expired or maximum attempts were exceeded.");
        }

        // 2. Verify entered OTP
        boolean matches = OtpUtil.verifyOtp(cleanOtp, otpRecord.getOtpHash());
        if (!matches) {
            otpDAO.incrementAttempts(otpRecord.getOtpId());
            int attemptsLeft = otpRecord.getMaxAttempts() - (otpRecord.getAttemptsCount() + 1);

            auditService.log(draft.getUserId(), null, AuditEventType.OTP_FAILED,
                    "Invalid OTP entered for draft " + draftId + " (" + attemptsLeft + " attempts left)", false);

            if (attemptsLeft <= 0) {
                activeDrafts.remove(draftId);
                throw new ValidationException("Maximum OTP attempts exceeded. Transfer request has been cancelled.");
            }
            throw new ValidationException("Incorrect OTP. " + attemptsLeft + " attempts remaining.");
        }

        // 3. Mark OTP as consumed immediately
        otpDAO.markAsConsumed(otpRecord.getOtpId());
        auditService.log(draft.getUserId(), null, AuditEventType.OTP_VERIFIED,
                "OTP verified successfully for draft " + draftId, true);

        // 4. Execute Atomic Financial Transaction
        Transaction executedTxn = executeAtomicTransfer(draft);

        // 5. Clean up completed draft
        activeDrafts.remove(draftId);

        return executedTxn;
    }

    /**
     * Executes the debit, credit, transaction record, and double-entry ledger within a single ACID transaction.
     * Uses pessimistic lock ordering (lower account ID first) to eliminate deadlocks under high concurrency.
     */
    private Transaction executeAtomicTransfer(TransferDraft draft) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID transaction

            long srcId = draft.getSourceAccountId();
            long dstId = draft.getDestinationAccountId();
            BigDecimal amount = draft.getAmount();

            // Deadlock prevention: Lock in ascending order of account IDs
            long firstLockId = Math.min(srcId, dstId);
            long secondLockId = Math.max(srcId, dstId);

            Account firstLocked = accountDAO.lockAccountForUpdate(conn, firstLockId)
                    .orElseThrow(() -> new BankingException("Unable to lock account ID: " + firstLockId));
            Account secondLocked = accountDAO.lockAccountForUpdate(conn, secondLockId)
                    .orElseThrow(() -> new BankingException("Unable to lock account ID: " + secondLockId));

            Account lockedSource = (srcId == firstLockId) ? firstLocked : secondLocked;
            Account lockedDest = (dstId == firstLockId) ? firstLocked : secondLocked;

            // Strict sanity re-verification inside the lock
            if (lockedSource.getStatus() != AccountStatus.ACTIVE) {
                throw new ValidationException("Source account is not active. Transfer aborted.");
            }
            if (lockedDest.getStatus() != AccountStatus.ACTIVE) {
                throw new ValidationException("Destination account is not active. Transfer aborted.");
            }
            if (lockedSource.getBalance().compareTo(amount) < 0) {
                throw new ValidationException("Insufficient funds. Account balance changed during processing.");
            }

            // Calculate new balances
            BigDecimal newSourceBalance = lockedSource.getBalance().subtract(amount);
            BigDecimal newDestBalance = lockedDest.getBalance().add(amount);

            // Execute balance updates
            boolean debited = accountDAO.updateBalance(conn, srcId, newSourceBalance);
            if (!debited) {
                throw new BankingException("Failed to debit source account.");
            }

            boolean credited = accountDAO.updateBalance(conn, dstId, newDestBalance);
            if (!credited) {
                throw new BankingException("Failed to credit destination account.");
            }

            // Record Master Transaction
            String txnRef = transactionDAO.generateUniqueReference();
            Transaction txn = new Transaction();
            txn.setTransactionReference(txnRef);
            txn.setSourceAccountId(srcId);
            txn.setDestinationAccountId(dstId);
            txn.setAmount(amount);
            txn.setTransactionType(TransactionType.TRANSFER);
            txn.setStatus(TransactionStatus.SUCCESS);
            txn.setDescription(draft.getRemarks());

            boolean txnCreated = transactionDAO.createTransaction(conn, txn);
            if (!txnCreated) {
                throw new BankingException("Failed to record transaction ledger record.");
            }

            // Double-entry ledger records
            // 1. DEBIT Entry
            LedgerEntry debitEntry = new LedgerEntry();
            debitEntry.setTransactionId(txn.getTransactionId());
            debitEntry.setAccountId(srcId);
            debitEntry.setEntryType(EntryType.DEBIT);
            debitEntry.setAmount(amount);
            debitEntry.setBalanceAfter(newSourceBalance);
            ledgerDAO.createLedgerEntry(conn, debitEntry);

            // 2. CREDIT Entry
            LedgerEntry creditEntry = new LedgerEntry();
            creditEntry.setTransactionId(txn.getTransactionId());
            creditEntry.setAccountId(dstId);
            creditEntry.setEntryType(EntryType.CREDIT);
            creditEntry.setAmount(amount);
            creditEntry.setBalanceAfter(newDestBalance);
            ledgerDAO.createLedgerEntry(conn, creditEntry);

            // COMMIT TRANSACTION
            conn.commit();

            txn.setSourceAccountNumber(draft.getSourceAccountNumber());
            txn.setDestinationAccountNumber(draft.getDestinationAccountNumber());

            auditService.log(draft.getUserId(), null, AuditEventType.TRANSFER_SUCCESS,
                    "Transfer completed successfully: " + txnRef + " (₹" + amount + " from " +
                    draft.getSourceAccountNumber() + " to " + draft.getDestinationAccountNumber() + ")", true);

            return txn;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Fatal: Rollback failed: " + rollbackEx.getMessage());
                }
            }

            auditService.log(draft.getUserId(), null, AuditEventType.TRANSFER_FAILED,
                    "Transfer failed for draft " + draft.getDraftId() + ": " + e.getMessage(), false);

            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            } else if (e instanceof BankingException) {
                throw (BankingException) e;
            } else {
                throw new BankingException("Transaction failed and was rolled back: " + e.getMessage(), e);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    public TransferDraft getDraft(String draftId) {
        return activeDrafts.get(draftId);
    }
}
