package org.example.dao;

import org.example.model.Member;
import org.example.util.DBConnection;

import java.sql.*;
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
            e.printStackTrace();
            return false;
        }
    }

    // Get all members
    public List<Member> getAllMembers() {

        List<Member> members = new ArrayList<>();

        String sql = "SELECT * FROM members";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Member member = new Member(
                        resultSet.getInt("member_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("address"),
                        resultSet.getDate("registration_date").toLocalDate()
                );

                members.add(member);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Member member = new Member(
                        resultSet.getInt("member_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("address"),
                        resultSet.getDate("registration_date").toLocalDate()
                );

                members.add(member);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }
}