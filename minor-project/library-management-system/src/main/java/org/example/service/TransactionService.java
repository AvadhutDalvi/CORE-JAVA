package org.example.service;

import org.example.dao.BookDAO;
import org.example.dao.MemberDAO;
import org.example.dao.TransactionDAO;
import org.example.model.Book;
import org.example.model.Member;
import org.example.model.Transaction;
import org.example.util.ValidationException;

import java.time.LocalDate;
import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
    }

    public TransactionService(TransactionDAO transactionDAO, BookDAO bookDAO, MemberDAO memberDAO) {
        this.transactionDAO = transactionDAO;
        this.bookDAO = bookDAO;
        this.memberDAO = memberDAO;
    }

    public boolean issueBook(int bookId, int memberId, int loanDays) {

        if (bookId <= 0) {
            throw new ValidationException("Book ID must be a positive number.");
        }

        if (memberId <= 0) {
            throw new ValidationException("Member ID must be a positive number.");
        }

        if (loanDays <= 0) {
            throw new ValidationException("Loan days must be greater than 0.");
        }

        if (loanDays > 365) {
            throw new ValidationException("Loan period cannot exceed 365 days.");
        }

        // Verify Book existence
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new ValidationException("Book not found with ID: " + bookId);
        }

        // Verify Member existence
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            throw new ValidationException("Member not found with ID: " + memberId);
        }

        // Check availability
        if (book.getAvailableQuantity() <= 0) {
            throw new ValidationException("This book is currently unavailable (0 copies available).");
        }

        // Prevent duplicate active borrowing of the same book by the same member
        if (transactionDAO.hasActiveBorrowing(bookId, memberId)) {
            throw new ValidationException("This member already has this book issued.");
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(loanDays);

        Transaction transaction = new Transaction(
                bookId,
                memberId,
                issueDate,
                dueDate
        );

        boolean success = transactionDAO.issueBook(transaction);
        if (!success) {
            throw new ValidationException("Failed to issue book. Please check availability and try again.");
        }

        return true;
    }

    public boolean returnBook(int transactionId, int bookId) {

        if (transactionId <= 0) {
            throw new ValidationException("Transaction ID must be a positive number.");
        }

        Transaction transaction = transactionDAO.getTransactionById(transactionId);
        if (transaction == null) {
            throw new ValidationException("Transaction record not found (ID: " + transactionId + ").");
        }

        if ("RETURNED".equalsIgnoreCase(transaction.getStatus())) {
            throw new ValidationException("This book has already been returned.");
        }

        int targetBookId = bookId > 0 ? bookId : transaction.getBookId();

        Book book = bookDAO.getBookById(targetBookId);
        if (book != null && book.getAvailableQuantity() >= book.getQuantity()) {
            throw new ValidationException("Cannot return book: available copies cannot exceed total stock (" + book.getQuantity() + ").");
        }

        boolean success = transactionDAO.returnBook(transactionId, targetBookId);
        if (!success) {
            throw new ValidationException("Failed to process book return. Please try again.");
        }

        return true;
    }

    public boolean returnBook(int transactionId) {
        return returnBook(transactionId, 0);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }
}