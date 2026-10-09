package com.banking.dao;

import com.banking.model.User;
import com.banking.model.UserRole;
import com.banking.model.UserStatus;
import com.banking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entities.
 * Handles user persistence, query lookups, and account status management.
 */
public class UserDAO {

    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, full_name, email, phone, password_hash, role, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getPasswordHash());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database error creating user: " + e.getMessage());
        }
        return false;
    }

    public Optional<User> findById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding user by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> findByEmailOrUsername(String identifier) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?) OR LOWER(username) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier.trim());
            stmt.setString(2, identifier.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding user by identifier: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean isEmailExists(String email, long excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND user_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            stmt.setLong(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error checking email existence: " + e.getMessage());
        }
        return false;
    }

    public boolean isUsernameExists(String username, long excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?) AND user_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setLong(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error checking username existence: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(long userId, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error updating user status: " + e.getMessage());
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all users: " + e.getMessage());
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            user.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            user.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        return user;
    }
}
