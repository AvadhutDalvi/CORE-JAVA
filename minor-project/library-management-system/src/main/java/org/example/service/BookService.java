package org.example.service;

import org.example.dao.BookDAO;
import org.example.model.Book;
import org.example.util.ValidationException;

import java.time.Year;
import java.util.List;

public class BookService {

    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public boolean addBook(Book book) {
        if (book == null) {
            throw new ValidationException("Book details cannot be empty.");
        }

        validateBookFields(book);

        // Normalize ISBN
        String isbn = book.getIsbn() != null ? book.getIsbn().trim() : "";
        book.setIsbn(isbn.isEmpty() ? null : isbn);

        // Check for duplicate ISBN if provided
        if (book.getIsbn() != null && bookDAO.isIsbnExists(book.getIsbn(), 0)) {
            throw new ValidationException("A book with ISBN '" + book.getIsbn() + "' already exists.");
        }

        // Set available quantity to total quantity on initial creation if not explicitly set
        if (book.getAvailableQuantity() <= 0 || book.getAvailableQuantity() > book.getQuantity()) {
            book.setAvailableQuantity(book.getQuantity());
        }

        boolean success = bookDAO.addBook(book);
        if (!success) {
            throw new ValidationException("Failed to save book to the database. Please try again.");
        }
        return true;
    }

    public boolean updateBook(Book book) {
        if (book == null) {
            throw new ValidationException("Book details cannot be empty.");
        }

        if (book.getBookId() <= 0) {
            throw new ValidationException("Invalid Book ID for update.");
        }

        validateBookFields(book);

        Book existing = bookDAO.getBookById(book.getBookId());
        if (existing == null) {
            throw new ValidationException("Book not found (ID: " + book.getBookId() + ").");
        }

        // Normalize ISBN
        String isbn = book.getIsbn() != null ? book.getIsbn().trim() : "";
        book.setIsbn(isbn.isEmpty() ? null : isbn);

        // Check duplicate ISBN (excluding current book)
        if (book.getIsbn() != null && bookDAO.isIsbnExists(book.getIsbn(), book.getBookId())) {
            throw new ValidationException("ISBN '" + book.getIsbn() + "' is already assigned to another book.");
        }

        // Protect available quantity and borrowed copies
        int borrowedCopies = existing.getQuantity() - existing.getAvailableQuantity();
        if (book.getQuantity() < borrowedCopies) {
            throw new ValidationException("Total quantity (" + book.getQuantity() +
                    ") cannot be less than currently borrowed copies (" + borrowedCopies + ").");
        }

        // Adjust available quantity according to change in total quantity
        int quantityDelta = book.getQuantity() - existing.getQuantity();
        int newAvailable = existing.getAvailableQuantity() + quantityDelta;

        if (newAvailable < 0 || newAvailable > book.getQuantity()) {
            throw new ValidationException("Invalid calculated available quantity: " + newAvailable);
        }

        book.setAvailableQuantity(newAvailable);

        boolean success = bookDAO.updateBook(book);
        if (!success) {
            throw new ValidationException("Failed to update book in database. Please try again.");
        }
        return true;
    }

    public boolean deleteBook(int bookId) {
        if (bookId <= 0) {
            throw new ValidationException("Invalid Book ID.");
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new ValidationException("Book not found (ID: " + bookId + ").");
        }

        if (bookDAO.hasActiveBorrowings(bookId)) {
            throw new ValidationException("Cannot delete book: this book currently has active borrowed copies.");
        }

        if (bookDAO.hasAnyTransactions(bookId)) {
            throw new ValidationException("Cannot delete book: transaction records exist for this book in history.");
        }

        boolean success = bookDAO.deleteBook(bookId);
        if (!success) {
            throw new ValidationException("Failed to delete book. It may be referenced by other records.");
        }
        return true;
    }

    public Book getBookById(int bookId) {
        if (bookId <= 0) {
            return null;
        }
        return bookDAO.getBookById(bookId);
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllBooks();
        }
        return bookDAO.searchBooks(keyword.trim());
    }

    private void validateBookFields(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new ValidationException("Book title is required.");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new ValidationException("Book author is required.");
        }

        if (book.getQuantity() <= 0) {
            throw new ValidationException("Book quantity must be greater than 0.");
        }

        int currentYear = Year.now().getValue();
        if (book.getPublishedYear() < 1000 || book.getPublishedYear() > currentYear + 1) {
            throw new ValidationException("Published year must be between 1000 and " + (currentYear + 1) + ".");
        }

        // Clean trimmed strings
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        if (book.getCategory() != null) {
            book.setCategory(book.getCategory().trim());
        }
    }
}
