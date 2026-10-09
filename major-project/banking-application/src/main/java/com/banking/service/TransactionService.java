package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.LedgerDAO;
import com.banking.dao.TransactionDAO;
import com.banking.model.Account;
import com.banking.model.LedgerEntry;
import com.banking.model.Transaction;
import com.banking.security.UserSession;
import com.banking.util.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing financial statement enquiries and transaction histories.
 */
public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final LedgerDAO ledgerDAO;
    private final AccountDAO accountDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.ledgerDAO = new LedgerDAO();
        this.accountDAO = new AccountDAO();
    }

    public TransactionService(TransactionDAO transactionDAO, LedgerDAO ledgerDAO, AccountDAO accountDAO) {
        this.transactionDAO = transactionDAO;
        this.ledgerDAO = ledgerDAO;
        this.accountDAO = accountDAO;
    }

    public List<Transaction> getTransactionsForUser(long userId) {
        return transactionDAO.getTransactionsByUserId(userId);
    }

    public List<Transaction> getRecentTransactions(long userId, int limit) {
        return transactionDAO.getTransactionsByUserId(userId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsForAccount(long accountId, long currentUserId) {
        // Authorization check: Verify account belongs to current user unless admin
        Optional<Account> accOpt = accountDAO.findById(accountId);
        if (accOpt.isEmpty()) {
            throw new ValidationException("Account not found.");
        }
        if (accOpt.get().getUserId() != currentUserId && 
            !(UserSession.isAuthenticated() && UserSession.getCurrentSession().isAdmin())) {
            throw new ValidationException("Access denied: You cannot view transaction history for another customer's account.");
        }
        return transactionDAO.getTransactionsByAccountId(accountId);
    }

    public Optional<Transaction> getTransactionByReference(String reference) {
        return transactionDAO.findByReference(reference);
    }

    public List<LedgerEntry> getLedgerEntries(long transactionId) {
        return ledgerDAO.getEntriesByTransactionId(transactionId);
    }

    public List<Transaction> getAllTransactions() {
        if (UserSession.isAuthenticated() && !UserSession.getCurrentSession().isAdmin()) {
            throw new ValidationException("Access denied: Administrative privileges required.");
        }
        return transactionDAO.getAllTransactions();
    }
}
