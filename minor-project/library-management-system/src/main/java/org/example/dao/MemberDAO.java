package org.example.dao;

import org.example.model.Member;
import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    // Add member
    public boolean addMember(Member member) {

        String sql = """
                INSERT INTO members
                (name, email, phone, address)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, member.getName());
            statement.setString(2, member.getEmail());
            statement.setString(3, member.getPhone());
            statement.setString(4, member.getAddress());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error in addMember: " + e.getMessage());
            return false;
        }
    }

    // Get all members
    public List<Member> getAllMembers() {

        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY member_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                members.add(mapResultSetToMember(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Database error in getAllMembers: " + e.getMessage());
        }

        return members;
    }

    // Get single member by ID
    public Member getMemberById(int memberId) {

        String sql = "SELECT * FROM members WHERE member_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToMember(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in getMemberById: " + e.getMessage());
        }

        return null;
    }

    // Update member
    public boolean updateMember(Member member) {

        String sql = """
                UPDATE members
                SET name = ?,
                    email = ?,
                    phone = ?,
                    address = ?
                WHERE member_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, member.getName());
            statement.setString(2, member.getEmail());
            statement.setString(3, member.getPhone());
            statement.setString(4, member.getAddress());
            statement.setInt(5, member.getMemberId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error in updateMember: " + e.getMessage());
            return false;
        }
    }

    // Delete member
    public boolean deleteMember(int memberId) {

        String sql = "DELETE FROM members WHERE member_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error in deleteMember: " + e.getMessage());
            return false;
        }
    }

    // Search members
    public List<Member> searchMembers(String keyword) {

        List<Member> members = new ArrayList<>();

        String sql = """
                SELECT * FROM members
                WHERE name LIKE ?
                   OR email LIKE ?
                   OR phone LIKE ?
                   OR address LIKE ?
                ORDER BY member_id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(mapResultSetToMember(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in searchMembers: " + e.getMessage());
        }

        return members;
    }

    // Check if an email is already used by another member
    public boolean isEmailExists(String email, int excludeMemberId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = excludeMemberId > 0
                ? "SELECT COUNT(*) FROM members WHERE email = ? AND member_id <> ?"
                : "SELECT COUNT(*) FROM members WHERE email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim().toLowerCase());
            if (excludeMemberId > 0) {
                statement.setInt(2, excludeMemberId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in isEmailExists: " + e.getMessage());
        }

        return false;
    }

    // Check active borrowings (status = 'BORROWED')
    public boolean hasActiveBorrowings(int memberId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE member_id = ? AND status = 'BORROWED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in hasActiveBorrowings: " + e.getMessage());
        }

        return false;
    }

    // Check if member has any historical transactions
    public boolean hasAnyTransactions(int memberId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE member_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in hasAnyTransactions: " + e.getMessage());
        }

        return false;
    }

    private Member mapResultSetToMember(ResultSet resultSet) throws SQLException {
        Date regDate = resultSet.getDate("registration_date");
        LocalDate localRegDate = regDate != null ? regDate.toLocalDate() : LocalDate.now();

        return new Member(
                resultSet.getInt("member_id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getString("address"),
                localRegDate
        );
    }
}