package org.example.dao;

import org.example.model.Transaction;
import org.example.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // Issue a book
    public boolean issueBook(Transaction transaction) {

        String checkSql =
                "SELECT available_quantity FROM books WHERE book_id = ?";

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

            // Start database transaction
            connection.setAutoCommit(false);

            // Check book availability
            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkSql)) {

                checkStatement.setInt(1, transaction.getBookId());

                ResultSet resultSet = checkStatement.executeQuery();

                if (!resultSet.next()) {
                    connection.rollback();
                    return false;
                }

                int availableQuantity =
                        resultSet.getInt("available_quantity");

                if (availableQuantity <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Decrease available quantity
            try (PreparedStatement updateStatement =
                         connection.prepareStatement(updateBookSql)) {

                updateStatement.setInt(1, transaction.getBookId());

                int updatedRows = updateStatement.executeUpdate();

                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Create transaction record
            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertTransactionSql)) {

                insertStatement.setInt(1, transaction.getBookId());
                insertStatement.setInt(2, transaction.getMemberId());
                insertStatement.setDate(
                        3,
                        Date.valueOf(transaction.getIssueDate())
                );
                insertStatement.setDate(
                        4,
                        Date.valueOf(transaction.getDueDate())
                );

                insertStatement.executeUpdate();
            }

            connection.commit();

            return true;

        } catch (SQLException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            e.printStackTrace();
            return false;

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Return a book
    public boolean returnBook(int transactionId, int bookId) {

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
                WHERE book_id = ?
                """;

        Connection connection = null;

        try {

            connection = DBConnection.getConnection();

            connection.setAutoCommit(false);

            // Mark transaction as returned
            try (PreparedStatement statement =
                         connection.prepareStatement(updateTransactionSql)) {

                statement.setInt(1, transactionId);

                int updatedRows = statement.executeUpdate();

                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Increase book availability
            try (PreparedStatement statement =
                         connection.prepareStatement(updateBookSql)) {

                statement.setInt(1, bookId);

                statement.executeUpdate();
            }

            connection.commit();

            return true;

        } catch (SQLException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            e.printStackTrace();
            return false;

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Get all transactions
    public List<Transaction> getAllTransactions() {

        List<Transaction> transactions = new ArrayList<>();

        String sql =
                """
                SELECT *
                FROM transactions
                ORDER BY transaction_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Transaction transaction = new Transaction(
                        resultSet.getInt("transaction_id"),
                        resultSet.getInt("book_id"),
                        resultSet.getInt("member_id"),
                        resultSet.getDate("issue_date").toLocalDate(),
                        resultSet.getDate("due_date").toLocalDate(),
                        resultSet.getDate("return_date") != null
                                ? resultSet.getDate("return_date").toLocalDate()
                                : null,
                        resultSet.getString("status")
                );

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}