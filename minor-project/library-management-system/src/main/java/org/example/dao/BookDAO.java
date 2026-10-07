package org.example.dao;



import org.example.model.Book;
import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all books from the database
    public List<Book> getAllBooks() {

        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM books";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Book book = new Book(
                        resultSet.getInt("book_id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getString("category"),
                        resultSet.getString("isbn"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("available_quantity"),
                        resultSet.getInt("published_year")
                );

                books.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

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

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteBook(int bookId) {

        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Book> searchBooks(String keyword) {

        List<Book> books = new ArrayList<>();

        String sql = """
            SELECT * FROM books
            WHERE title LIKE ?
               OR author LIKE ?
               OR isbn LIKE ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Book book = new Book(
                        resultSet.getInt("book_id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getString("category"),
                        resultSet.getString("isbn"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("available_quantity"),
                        resultSet.getInt("published_year")
                );

                books.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }
}