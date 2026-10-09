package com.banking.dao;

import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import com.banking.util.DBConnection;

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for financial transaction ledger records.
 */
public class TransactionDAO {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public boolean createTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_reference, source_account_id, destination_account_id, " +
                     "amount, transaction_type, status, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, transaction.getTransactionReference());
            if (transaction.getSourceAccountId() != null) {
                stmt.setLong(2, transaction.getSourceAccountId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            if (transaction.getDestinationAccountId() != null) {
                stmt.setLong(3, transaction.getDestinationAccountId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getTransactionType().name());
            stmt.setString(6, transaction.getStatus().name());
            stmt.setString(7, transaction.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaction.setTransactionId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Optional<Transaction> findById(long transactionId) {
        String sql = baseTransactionSelectQuery() + " WHERE t.transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding transaction by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Transaction> findByReference(String reference) {
        String sql = baseTransactionSelectQuery() + " WHERE t.transaction_reference = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reference.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding transaction by reference: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Transaction> getTransactionsByAccountId(long accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = baseTransactionSelectQuery() +
                     " WHERE t.source_account_id = ? OR t.destination_account_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);
            stmt.setLong(2, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching account transactions: " + e.getMessage());
        }
        return list;
    }

    public List<Transaction> getTransactionsByUserId(long userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = baseTransactionSelectQuery() +
                     " WHERE sa.user_id = ? OR da.user_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching user transactions: " + e.getMessage());
        }
        return list;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = baseTransactionSelectQuery() + " ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all transactions: " + e.getMessage());
        }
        return list;
    }

    public String generateUniqueReference() {
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        int randomNum = 10000 + RANDOM.nextInt(90000);
        return "TXN-" + datePrefix + "-" + randomNum;
    }

    private String baseTransactionSelectQuery() {
        return "SELECT t.*, sa.account_number as src_acc_num, da.account_number as dst_acc_num " +
               "FROM transactions t " +
               "LEFT JOIN accounts sa ON t.source_account_id = sa.account_id " +
               "LEFT JOIN accounts da ON t.destination_account_id = da.account_id ";
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction txn = new Transaction();
        txn.setTransactionId(rs.getLong("transaction_id"));
        txn.setTransactionReference(rs.getString("transaction_reference"));

        long srcId = rs.getLong("source_account_id");
        if (!rs.wasNull()) {
            txn.setSourceAccountId(srcId);
        }

        long dstId = rs.getLong("destination_account_id");
        if (!rs.wasNull()) {
            txn.setDestinationAccountId(dstId);
        }

        txn.setAmount(rs.getBigDecimal("amount"));
        txn.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        txn.setStatus(TransactionStatus.valueOf(rs.getString("status")));
        txn.setDescription(rs.getString("description"));

        txn.setSourceAccountNumber(rs.getString("src_acc_num"));
        txn.setDestinationAccountNumber(rs.getString("dst_acc_num"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            txn.setCreatedAt(ts.toLocalDateTime());
        }
        return txn;
    }
}
