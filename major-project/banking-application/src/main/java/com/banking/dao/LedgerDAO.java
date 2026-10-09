package com.banking.dao;

import com.banking.model.EntryType;
import com.banking.model.LedgerEntry;
import com.banking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for immutable double-entry ledger records.
 */
public class LedgerDAO {

    public boolean createLedgerEntry(Connection conn, LedgerEntry entry) throws SQLException {
        String sql = "INSERT INTO ledger_entries (transaction_id, account_id, entry_type, amount, balance_after) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, entry.getTransactionId());
            stmt.setLong(2, entry.getAccountId());
            stmt.setString(3, entry.getEntryType().name());
            stmt.setBigDecimal(4, entry.getAmount());
            stmt.setBigDecimal(5, entry.getBalanceAfter());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        entry.setEntryId(rs.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<LedgerEntry> getEntriesByTransactionId(long transactionId) {
        List<LedgerEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM ledger_entries WHERE transaction_id = ? ORDER BY entry_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToLedger(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching ledger entries for txn: " + e.getMessage());
        }
        return entries;
    }

    public List<LedgerEntry> getEntriesByAccountId(long accountId) {
        List<LedgerEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM ledger_entries WHERE account_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToLedger(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching ledger entries for account: " + e.getMessage());
        }
        return entries;
    }

    private LedgerEntry mapResultSetToLedger(ResultSet rs) throws SQLException {
        LedgerEntry entry = new LedgerEntry();
        entry.setEntryId(rs.getLong("entry_id"));
        entry.setTransactionId(rs.getLong("transaction_id"));
        entry.setAccountId(rs.getLong("account_id"));
        entry.setEntryType(EntryType.valueOf(rs.getString("entry_type")));
        entry.setAmount(rs.getBigDecimal("amount"));
        entry.setBalanceAfter(rs.getBigDecimal("balance_after"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            entry.setCreatedAt(ts.toLocalDateTime());
        }
        return entry;
    }
}
