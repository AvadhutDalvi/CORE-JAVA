package com.banking.dao;

import com.banking.model.AuditEventType;
import com.banking.model.AuditLog;
import com.banking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for security audit logs.
 */
public class AuditLogDAO {

    public boolean logEvent(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, username_attempted, event_type, source_ip, details, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (log.getUserId() != null) {
                stmt.setLong(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }

            stmt.setString(2, log.getUsernameAttempted());
            stmt.setString(3, log.getEventType().name());
            stmt.setString(4, log.getSourceIp());
            stmt.setString(5, log.getDetails());
            stmt.setString(6, log.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setLogId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database error saving audit log: " + e.getMessage());
        }
        return false;
    }

    public List<AuditLog> getLogsByUserId(long userId) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching audit logs by user ID: " + e.getMessage());
        }
        return list;
    }

    public List<AuditLog> getRecentLogs(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching recent audit logs: " + e.getMessage());
        }
        return list;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));

        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) {
            log.setUserId(uid);
        }

        log.setUsernameAttempted(rs.getString("username_attempted"));
        log.setEventType(AuditEventType.valueOf(rs.getString("event_type")));
        log.setSourceIp(rs.getString("source_ip"));
        log.setDetails(rs.getString("details"));
        log.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            log.setCreatedAt(ts.toLocalDateTime());
        }
        return log;
    }
}
