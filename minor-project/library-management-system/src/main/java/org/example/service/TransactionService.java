package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.model.Transaction;

import java.time.LocalDate;
import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
    }

    public boolean issueBook(int bookId, int memberId, int loanDays) {

        if (bookId <= 0 || memberId <= 0 || loanDays <= 0) {
            return false;
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(loanDays);

        Transaction transaction = new Transaction(
                bookId,
                memberId,
                issueDate,
                dueDate
        );

        return transactionDAO.issueBook(transaction);
    }

    public boolean returnBook(int transactionId, int bookId) {

        if (transactionId <= 0 || bookId <= 0) {
            return false;
        }

        return transactionDAO.returnBook(transactionId, bookId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }
}