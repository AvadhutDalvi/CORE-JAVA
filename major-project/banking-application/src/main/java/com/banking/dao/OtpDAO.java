package com.banking.dao;

import com.banking.model.OtpRecord;
import com.banking.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data Access Object for OTP records.
 */
public class OtpDAO {

    public boolean saveOtp(OtpRecord otp) {
        String sql = "INSERT INTO otp_verifications (user_id, otp_hash, purpose, reference_id, " +
                     "expires_at, attempts_count, max_attempts, is_consumed) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, otp.getUserId());
            stmt.setString(2, otp.getOtpHash());
            stmt.setString(3, otp.getPurpose());
            stmt.setString(4, otp.getReferenceId());
            stmt.setTimestamp(5, Timestamp.valueOf(otp.getExpiresAt()));
            stmt.setInt(6, otp.getAttemptsCount());
            stmt.setInt(7, otp.getMaxAttempts());
            stmt.setBoolean(8, otp.isConsumed());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        otp.setOtpId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database error saving OTP: " + e.getMessage());
        }
        return false;
    }

    public Optional<OtpRecord> getLatestValidOtp(long userId, String purpose, String referenceId) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM otp_verifications WHERE user_id = ? AND purpose = ? " +
                "AND is_consumed = FALSE AND expires_at > ? AND attempts_count < max_attempts "
        );
        if (referenceId != null) {
            sql.append("AND reference_id = ? ");
        }
        sql.append("ORDER BY created_at DESC LIMIT 1");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setLong(1, userId);
            stmt.setString(2, purpose);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            if (referenceId != null) {
                stmt.setString(4, referenceId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOtp(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving latest valid OTP: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean incrementAttempts(long otpId) {
        String sql = "UPDATE otp_verifications SET attempts_count = attempts_count + 1 WHERE otp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, otpId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error incrementing OTP attempts: " + e.getMessage());
        }
        return false;
    }

    public boolean markAsConsumed(long otpId) {
        String sql = "UPDATE otp_verifications SET is_consumed = TRUE WHERE otp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, otpId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error marking OTP consumed: " + e.getMessage());
        }
        return false;
    }

    public boolean invalidateUserOtps(long userId, String purpose) {
        String sql = "UPDATE otp_verifications SET is_consumed = TRUE WHERE user_id = ? AND purpose = ? AND is_consumed = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, purpose);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Database error invalidating prior OTPs: " + e.getMessage());
        }
        return false;
    }

    private OtpRecord mapResultSetToOtp(ResultSet rs) throws SQLException {
        OtpRecord otp = new OtpRecord();
        otp.setOtpId(rs.getLong("otp_id"));
        otp.setUserId(rs.getLong("user_id"));
        otp.setOtpHash(rs.getString("otp_hash"));
        otp.setPurpose(rs.getString("purpose"));
        otp.setReferenceId(rs.getString("reference_id"));

        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) {
            otp.setExpiresAt(exp.toLocalDateTime());
        }

        otp.setAttemptsCount(rs.getInt("attempts_count"));
        otp.setMaxAttempts(rs.getInt("max_attempts"));
        otp.setConsumed(rs.getBoolean("is_consumed"));

        Timestamp crt = rs.getTimestamp("created_at");
        if (crt != null) {
            otp.setCreatedAt(crt.toLocalDateTime());
        }
        return otp;
    }
}
