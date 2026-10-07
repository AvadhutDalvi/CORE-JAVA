package org.example.gui;

import org.example.model.Transaction;
import org.example.service.TransactionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransactionPanel extends JPanel {

    private final TransactionService transactionService;

    private JTextField bookIdField;
    private JTextField memberIdField;
    private JTextField loanDaysField;

    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public TransactionPanel() {

        transactionService = new TransactionService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createForm();
        createTable();

        loadTransactions();
    }

    private void createForm() {

        JPanel formPanel = new JPanel(
                new GridLayout(2, 6, 10, 10)
        );

        bookIdField = new JTextField();
        memberIdField = new JTextField();
        loanDaysField = new JTextField("14");

        formPanel.add(new JLabel("Book ID:"));
        formPanel.add(bookIdField);

        formPanel.add(new JLabel("Member ID:"));
        formPanel.add(memberIdField);

        formPanel.add(new JLabel("Loan Days:"));
        formPanel.add(loanDaysField);

        JButton issueButton = new JButton("Issue Book");
        JButton returnButton = new JButton("Return Selected");
        JButton refreshButton = new JButton("Refresh");

        formPanel.add(issueButton);
        formPanel.add(returnButton);
        formPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        topPanel.add(formPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        issueButton.addActionListener(e -> issueBook());
        returnButton.addActionListener(e -> returnBook());
        refreshButton.addActionListener(e -> loadTransactions());
    }

    private void createTable() {

        String[] columns = {
                "Transaction ID",
                "Book ID",
                "Member ID",
                "Issue Date",
                "Due Date",
                "Return Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);

        JScrollPane scrollPane =
                new JScrollPane(transactionTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void issueBook() {

        try {

            int bookId =
                    Integer.parseInt(bookIdField.getText().trim());

            int memberId =
                    Integer.parseInt(memberIdField.getText().trim());

            int loanDays =
                    Integer.parseInt(loanDaysField.getText().trim());

            boolean success =
                    transactionService.issueBook(
                            bookId,
                            memberId,
                            loanDays
                    );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book issued successfully!"
                );

                clearFields();
                loadTransactions();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to issue book. Check Book ID, Member ID, and availability."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Book ID, Member ID and Loan Days must be numbers."
            );
        }
    }

    private void returnBook() {

        int selectedRow =
                transactionTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a borrowed transaction."
            );

            return;
        }

        int transactionId =
                (int) tableModel.getValueAt(selectedRow, 0);

        int bookId =
                (int) tableModel.getValueAt(selectedRow, 1);

        String status =
                tableModel.getValueAt(selectedRow, 6).toString();

        if ("RETURNED".equals(status)) {

            JOptionPane.showMessageDialog(
                    this,
                    "This book has already been returned."
            );

            return;
        }

        boolean success =
                transactionService.returnBook(
                        transactionId,
                        bookId
                );

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Book returned successfully!"
            );

            loadTransactions();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to return the book."
            );
        }
    }

    private void loadTransactions() {

        tableModel.setRowCount(0);

        List<Transaction> transactions =
                transactionService.getAllTransactions();

        for (Transaction transaction : transactions) {

            tableModel.addRow(new Object[]{
                    transaction.getTransactionId(),
                    transaction.getBookId(),
                    transaction.getMemberId(),
                    transaction.getIssueDate(),
                    transaction.getDueDate(),
                    transaction.getReturnDate(),
                    transaction.getStatus()
            });
        }
    }

    private void clearFields() {

        bookIdField.setText("");
        memberIdField.setText("");
        loanDaysField.setText("14");
    }
}