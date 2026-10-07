package org.example.dao;

import org.example.model.Transaction;
import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // Issue a book atomically
    public boolean issueBook(Transaction transaction) {

        String checkBookSql =
                "SELECT available_quantity FROM books WHERE book_id = ? FOR UPDATE";

        String checkMemberSql =
                "SELECT member_id FROM members WHERE member_id = ?";

        String checkActiveLoanSql =
                "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND member_id = ? AND status = 'BORROWED'";

        String updateBookSql =
                """
                UPDATE books
                SET available_quantity = available_quantity - 1
                WHERE book_id = ? AND available_quantity > 0
                """;

        String insertTransactionSql =
                """
                INSERT INTO transactions
                (book_id, member_id, issue_date, due_date, status)
                VALUES (?, ?, ?, ?, 'BORROWED')
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            // 1. Verify book existence and availability
            try (PreparedStatement checkBookStmt = connection.prepareStatement(checkBookSql)) {
                checkBookStmt.setInt(1, transaction.getBookId());
                try (ResultSet rs = checkBookStmt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                    int availableQuantity = rs.getInt("available_quantity");
                    if (availableQuantity <= 0) {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // 2. Verify member existence
            try (PreparedStatement checkMemberStmt = connection.prepareStatement(checkMemberSql)) {
                checkMemberStmt.setInt(1, transaction.getMemberId());
                try (ResultSet rs = checkMemberStmt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // 3. Verify no active borrowed transaction of same book for same member
            try (PreparedStatement checkLoanStmt = connection.prepareStatement(checkActiveLoanSql)) {
                checkLoanStmt.setInt(1, transaction.getBookId());
                checkLoanStmt.setInt(2, transaction.getMemberId());
                try (ResultSet rs = checkLoanStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // 4. Atomically decrease available quantity
            try (PreparedStatement updateStmt = connection.prepareStatement(updateBookSql)) {
                updateStmt.setInt(1, transaction.getBookId());
                int updatedRows = updateStmt.executeUpdate();
                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // 5. Create transaction record
            try (PreparedStatement insertStmt = connection.prepareStatement(insertTransactionSql)) {
                insertStmt.setInt(1, transaction.getBookId());
                insertStmt.setInt(2, transaction.getMemberId());
                insertStmt.setDate(3, Date.valueOf(transaction.getIssueDate()));
                insertStmt.setDate(4, Date.valueOf(transaction.getDueDate()));

                insertStmt.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback error: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Database error in issueBook: " + e.getMessage());
            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // Return a book atomically
    public boolean returnBook(int transactionId, int bookId) {

        String checkTxSql =
                "SELECT transaction_id, book_id, status FROM transactions WHERE transaction_id = ? FOR UPDATE";

        String updateTransactionSql =
                """
                UPDATE transactions
                SET return_date = CURDATE(),
                    status = 'RETURNED'
                WHERE transaction_id = ?
                  AND status = 'BORROWED'
                """;

        String updateBookSql =
                """
                UPDATE books
                SET available_quantity = available_quantity + 1
                WHERE book_id = ? AND available_quantity < quantity
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            int targetBookId = bookId;

            // 1. Verify transaction status and get book_id
            try (PreparedStatement checkTxStmt = connection.prepareStatement(checkTxSql)) {
                checkTxStmt.setInt(1, transactionId);
                try (ResultSet rs = checkTxStmt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                    String status = rs.getString("status");
                    if (!"BORROWED".equalsIgnoreCase(status)) {
                        connection.rollback();
                        return false;
                    }
                    if (targetBookId <= 0) {
                        targetBookId = rs.getInt("book_id");
                    }
                }
            }

            // 2. Mark transaction as RETURNED
            try (PreparedStatement updateTxStmt = connection.prepareStatement(updateTransactionSql)) {
                updateTxStmt.setInt(1, transactionId);
                int updatedRows = updateTxStmt.executeUpdate();
                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // 3. Atomically increase book availability without exceeding total quantity
            try (PreparedStatement updateBookStmt = connection.prepareStatement(updateBookSql)) {
                updateBookStmt.setInt(1, targetBookId);
                int updatedRows = updateBookStmt.executeUpdate();
                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback error: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Database error in returnBook: " + e.getMessage());
            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // Get a transaction by ID
    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, transactionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToTransaction(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in getTransactionById: " + e.getMessage());
        }

        return null;
    }

    // Check if a member currently has a book borrowed
    public boolean hasActiveBorrowing(int bookId, int memberId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND member_id = ? AND status = 'BORROWED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);
            statement.setInt(2, memberId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in hasActiveBorrowing: " + e.getMessage());
        }

        return false;
    }

    // Get all transactions
    public List<Transaction> getAllTransactions() {

        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                transactions.add(mapResultSetToTransaction(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Database error in getAllTransactions: " + e.getMessage());
        }

        return transactions;
    }

    private Transaction mapResultSetToTransaction(ResultSet resultSet) throws SQLException {
        Date returnDateSql = resultSet.getDate("return_date");

        return new Transaction(
                resultSet.getInt("transaction_id"),
                resultSet.getInt("book_id"),
                resultSet.getInt("member_id"),
                resultSet.getDate("issue_date").toLocalDate(),
                resultSet.getDate("due_date").toLocalDate(),
                returnDateSql != null ? returnDateSql.toLocalDate() : null,
                resultSet.getString("status")
        );
    }
}