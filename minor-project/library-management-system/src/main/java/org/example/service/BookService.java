package org.example.service;

import org.example.dao.BookDAO;
import org.example.model.Book;

import java.util.List;

public class BookService {

    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public boolean addBook(Book book) {

        if (book == null) {
            return false;
        }

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            return false;
        }

        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            return false;
        }

        if (book.getQuantity() <= 0) {
            return false;
        }

        if (book.getPublishedYear() < 1000 ||
                book.getPublishedYear() > java.time.Year.now().getValue()) {
            return false;
        }

        if (book.getAvailableQuantity() < 0 ||
                book.getAvailableQuantity() > book.getQuantity()) {
            return false;
        }

        return bookDAO.addBook(book);
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public boolean updateBook(Book book) {

        if (book == null) {
            return false;
        }

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            return false;
        }

        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            return false;
        }

        if (book.getQuantity() <= 0) {
            return false;
        }

        if (book.getPublishedYear() < 1000 ||
                book.getPublishedYear() > java.time.Year.now().getValue()) {
            return false;
        }

        if (book.getAvailableQuantity() < 0 ||
                book.getAvailableQuantity() > book.getQuantity()) {
            return false;
        }

        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(int bookId) {
        return bookDAO.deleteBook(bookId);
    }

    public List<Book> searchBooks(String keyword) {
        return bookDAO.searchBooks(keyword);
    }
}
