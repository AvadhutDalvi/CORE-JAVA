package org.example.dao;

import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    public int getTotalBooks() {
        return getCount("SELECT COUNT(*) FROM books");
    }

    public int getTotalMembers() {
        return getCount("SELECT COUNT(*) FROM members");
    }

    public int getAvailableBooks() {
        return getCount("SELECT COALESCE(SUM(available_quantity), 0) FROM books");
    }

    public int getIssuedBooks() {
        return getCount("SELECT COUNT(*) FROM transactions WHERE status = 'BORROWED'");
    }

    public int getOverdueBooks() {
        String sql = """
                SELECT COUNT(*)
                FROM transactions
                WHERE status = 'BORROWED'
                  AND due_date < CURDATE()
                """;
        return getCount(sql);
    }

    private int getCount(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Database error in DashboardDAO: " + e.getMessage());
        }

        return 0;
    }
}