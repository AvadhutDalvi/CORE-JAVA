package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.model.AuditEventType;
import com.banking.security.UserSession;
import com.banking.util.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service managing customer and operational bank accounts.
 */
public class AccountService {

    private final AccountDAO accountDAO;
    private final AuditService auditService;

    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.auditService = new AuditService();
    }

    public AccountService(AccountDAO accountDAO, AuditService auditService) {
        this.accountDAO = accountDAO;
        this.auditService = auditService;
    }

    public List<Account> getAccountsForUser(long userId) {
        return accountDAO.findByUserId(userId);
    }

    public Optional<Account> getAccountById(long accountId) {
        return accountDAO.findById(accountId);
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return accountDAO.findByAccountNumber(accountNumber.trim());
    }

    public BigDecimal getTotalBalanceForUser(long userId) {
        List<Account> accounts = accountDAO.findByUserId(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (Account acc : accounts) {
            if (acc.getStatus() == AccountStatus.ACTIVE) {
                total = total.add(acc.getBalance());
            }
        }
        return total.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Account createAdditionalAccount(long userId, AccountType type) {
        Account newAcc = new Account();
        newAcc.setUserId(userId);
        newAcc.setAccountNumber(accountDAO.generateUniqueAccountNumber());
        newAcc.setAccountType(type != null ? type : AccountType.SAVINGS);
        newAcc.setBalance(new BigDecimal("0.00"));
        newAcc.setStatus(AccountStatus.ACTIVE);

        boolean created = accountDAO.createAccount(newAcc);
        if (!created) {
            throw new ValidationException("Failed to open additional account. Please try again.");
        }

        auditService.log(userId, null, AuditEventType.ACCOUNT_CREATED,
                "New account opened: " + newAcc.getAccountNumber() + " (" + newAcc.getAccountType() + ")", true);
        return newAcc;
    }

    public boolean updateAccountStatus(long accountId, AccountStatus status) {
        if (UserSession.isAuthenticated() && !UserSession.getCurrentSession().isAdmin()) {
            throw new ValidationException("Access denied: Administrative privileges required.");
        }
        boolean updated = accountDAO.updateStatus(accountId, status);
        if (updated) {
            auditService.log(
                    UserSession.isAuthenticated() ? UserSession.getCurrentSession().getUserId() : null,
                    null,
                    AuditEventType.STATUS_CHANGE,
                    "Account ID " + accountId + " status updated to " + status,
                    true
            );
        }
        return updated;
    }

    public List<Account> getAllAccounts() {
        if (UserSession.isAuthenticated() && !UserSession.getCurrentSession().isAdmin()) {
            throw new ValidationException("Access denied: Administrative privileges required.");
        }
        return accountDAO.getAllAccounts();
    }
}
