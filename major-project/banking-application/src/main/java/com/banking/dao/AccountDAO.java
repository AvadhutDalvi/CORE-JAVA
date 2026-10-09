package com.banking.dao;

import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.util.DBConnection;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Account entities.
 * Includes methods for safe transactional updates and pessimistic record locking (FOR UPDATE).
 */
public class AccountDAO {

    private static final SecureRandom RANDOM = new SecureRandom();

    public boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (account_number, user_id, account_type, balance, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setLong(2, account.getUserId());
            stmt.setString(3, account.getAccountType().name());
            stmt.setBigDecimal(4, account.getBalance());
            stmt.setString(5, account.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        account.setAccountId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database error creating account: " + e.getMessage());
        }
        return false;
    }

    public Optional<Account> findById(long accountId) {
        String sql = "SELECT a.*, u.full_name as owner_name FROM accounts a " +
                     "JOIN users u ON a.user_id = u.user_id WHERE a.account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding account by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT a.*, u.full_name as owner_name FROM accounts a " +
                     "JOIN users u ON a.user_id = u.user_id WHERE a.account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding account by number: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Account> findByUserId(long userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name as owner_name FROM accounts a " +
                     "JOIN users u ON a.user_id = u.user_id WHERE a.user_id = ? ORDER BY a.account_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding accounts by user ID: " + e.getMessage());
        }
        return accounts;
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name as owner_name FROM accounts a " +
                     "JOIN users u ON a.user_id = u.user_id ORDER BY a.account_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all accounts: " + e.getMessage());
        }
        return accounts;
    }

    /**
     * Locks an account record within an active database transaction using SELECT FOR UPDATE.
     * Prevents race conditions, double-spending, and inconsistent reads during concurrent transfers.
     */
    public Optional<Account> lockAccountForUpdate(Connection conn, long accountId) throws SQLException {
        String sql = "SELECT a.*, u.full_name as owner_name FROM accounts a " +
                     "JOIN users u ON a.user_id = u.user_id WHERE a.account_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates account balance within an active database transaction.
     */
    public boolean updateBalance(Connection conn, long accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setLong(2, accountId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(long accountId, AccountStatus status) {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, accountId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error updating account status: " + e.getMessage());
        }
        return false;
    }

    public boolean isAccountNumberExists(String accountNumber) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error checking account number: " + e.getMessage());
        }
        return false;
    }

    /**
     * Generates a 11-digit unique account number starting with standard branch routing prefix.
     */
    public String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long randomDigits = 10000000L + (long) (RANDOM.nextDouble() * 90000000L);
            accountNumber = "100" + randomDigits; // 11 digits total: 100xxxxxxxx
        } while (isAccountNumberExists(accountNumber));
        return accountNumber;
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getLong("account_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setUserId(rs.getLong("user_id"));
        account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setStatus(AccountStatus.valueOf(rs.getString("status")));

        try {
            account.setOwnerName(rs.getString("owner_name"));
        } catch (SQLException ignored) {}

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            account.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            account.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        return account;
    }
}
