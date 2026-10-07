package org.example.dao;

import org.example.model.Book;
import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // Add a new book to the database
    public boolean addBook(Book book) {

        String sql = """
                INSERT INTO books
                (title, author, category, isbn, quantity, available_quantity, published_year)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getCategory());
            statement.setString(4, book.getIsbn());
            statement.setInt(5, book.getQuantity());
            statement.setInt(6, book.getAvailableQuantity());
            statement.setInt(7, book.getPublishedYear());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error in addBook: " + e.getMessage());
            return false;
        }
    }

    // Get all books from the database
    public List<Book> getAllBooks() {

        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Book book = mapResultSetToBook(resultSet);
                books.add(book);
            }

        } catch (SQLException e) {
            System.err.println("Database error in getAllBooks: " + e.getMessage());
        }

        return books;
    }

    // Get a single book by ID
    public Book getBookById(int bookId) {

        String sql = "SELECT * FROM books WHERE book_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToBook(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in getBookById: " + e.getMessage());
        }

        return null;
    }

    // Update book details
    public boolean updateBook(Book book) {

        String sql = """
            UPDATE books
            SET title = ?,
                author = ?,
                category = ?,
                isbn = ?,
                quantity = ?,
                available_quantity = ?,
                published_year = ?
            WHERE book_id = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getCategory());
            statement.setString(4, book.getIsbn());
            statement.setInt(5, book.getQuantity());
            statement.setInt(6, book.getAvailableQuantity());
            statement.setInt(7, book.getPublishedYear());
            statement.setInt(8, book.getBookId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error in updateBook: " + e.getMessage());
            return false;
        }
    }

    // Delete a book by ID
    public boolean deleteBook(int bookId) {

        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Database error in deleteBook: " + e.getMessage());
            return false;
        }
    }

    // Search books by title, author, or ISBN
    public List<Book> searchBooks(String keyword) {

        List<Book> books = new ArrayList<>();

        String sql = """
            SELECT * FROM books
            WHERE title LIKE ?
               OR author LIKE ?
               OR isbn LIKE ?
               OR category LIKE ?
            ORDER BY book_id ASC
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
                    books.add(mapResultSetToBook(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in searchBooks: " + e.getMessage());
        }

        return books;
    }

    // Check if an ISBN is already assigned to another book
    public boolean isIsbnExists(String isbn, int excludeBookId) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }

        String sql = excludeBookId > 0
                ? "SELECT COUNT(*) FROM books WHERE isbn = ? AND book_id <> ?"
                : "SELECT COUNT(*) FROM books WHERE isbn = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, isbn.trim());
            if (excludeBookId > 0) {
                statement.setInt(2, excludeBookId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in isIsbnExists: " + e.getMessage());
        }

        return false;
    }

    // Check active (BORROWED) transactions for a book
    public boolean hasActiveBorrowings(int bookId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND status = 'BORROWED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

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

    // Check if any transaction history exists for a book
    public boolean hasAnyTransactions(int bookId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE book_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

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

    private Book mapResultSetToBook(ResultSet resultSet) throws SQLException {
        return new Book(
                resultSet.getInt("book_id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getString("category"),
                resultSet.getString("isbn"),
                resultSet.getInt("quantity"),
                resultSet.getInt("available_quantity"),
                resultSet.getInt("published_year")
        );
    }
}