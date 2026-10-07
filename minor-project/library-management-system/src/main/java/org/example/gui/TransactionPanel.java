package org.example.gui;

import org.example.model.Transaction;
import org.example.service.TransactionService;
import org.example.util.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TransactionPanel extends JPanel {

    private final TransactionService transactionService;

    private JTextField bookIdField;
    private JTextField memberIdField;
    private JTextField loanDaysField;

    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public TransactionPanel() {
        this.transactionService = new TransactionService();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createForm();
        createTable();

        loadTransactions();
    }

    private void createForm() {
        JPanel formContainer = new JPanel(new BorderLayout(8, 8));
        formContainer.setBorder(BorderFactory.createTitledBorder("Issue / Return Book"));

        JPanel formGrid = new JPanel(new GridLayout(2, 4, 12, 10));
        formGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        bookIdField = new JTextField();
        memberIdField = new JTextField();
        loanDaysField = new JTextField("14");

        formGrid.add(new JLabel("Book ID: *"));
        formGrid.add(bookIdField);

        formGrid.add(new JLabel("Member ID: *"));
        formGrid.add(memberIdField);

        formGrid.add(new JLabel("Loan Days: *"));
        formGrid.add(loanDaysField);

        // Empty label to balance grid
        formGrid.add(new JLabel(""));
        formGrid.add(new JLabel(""));

        formContainer.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        JButton issueButton = new JButton("Issue Book");
        JButton returnButton = new JButton("Return Selected Book");
        JButton clearButton = new JButton("Clear Fields");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(issueButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(formContainer, BorderLayout.NORTH);

        issueButton.addActionListener(e -> issueBook());
        returnButton.addActionListener(e -> returnBook());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadTransactions());
    }

    private void createTable() {
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Transaction Records"));

        String[] columns = {
                "Tx ID",
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
        transactionTable.setRowHeight(24);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setAutoCreateRowSorter(true);

        // Center align ID and Date columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        transactionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Custom renderer for Status column highlighting OVERDUE in red, RETURNED in green
        transactionTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);

                if (value != null) {
                    String status = value.toString();
                    if ("OVERDUE".equalsIgnoreCase(status)) {
                        c.setForeground(new Color(180, 0, 0)); // Dark red
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if ("RETURNED".equalsIgnoreCase(status)) {
                        c.setForeground(new Color(0, 130, 40)); // Dark green
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    } else { // BORROWED
                        c.setForeground(new Color(0, 70, 180)); // Blue
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    int modelRow = table.convertRowIndexToModel(row);
                    String status = table.getModel().getValueAt(modelRow, 6).toString();
                    if ("OVERDUE".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 235, 235)); // Soft red tint for overdue rows
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    }
                }

                return c;
            }
        });

        // When a row is selected, populate fields for convenience
        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = transactionTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
                    bookIdField.setText(tableModel.getValueAt(modelRow, 1).toString());
                    memberIdField.setText(tableModel.getValueAt(modelRow, 2).toString());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    private void issueBook() {
        try {
            String bookIdText = bookIdField.getText().trim();
            String memberIdText = memberIdField.getText().trim();
            String loanDaysText = loanDaysField.getText().trim();

            if (bookIdText.isEmpty() || memberIdText.isEmpty() || loanDaysText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Book ID, Member ID, and Loan Days are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int bookId = Integer.parseInt(bookIdText);
            int memberId = Integer.parseInt(memberIdText);
            int loanDays = Integer.parseInt(loanDaysText);

            transactionService.issueBook(bookId, memberId, loanDays);

            JOptionPane.showMessageDialog(this,
                    "Book issued successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadTransactions();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Book ID, Member ID, and Loan Days must be valid numbers.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an active (BORROWED / OVERDUE) transaction to return.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
        int transactionId = (int) tableModel.getValueAt(modelRow, 0);
        int bookId = (int) tableModel.getValueAt(modelRow, 1);
        String displayStatus = tableModel.getValueAt(modelRow, 6).toString();

        if ("RETURNED".equalsIgnoreCase(displayStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This book has already been returned.",
                    "Already Returned",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            transactionService.returnBook(transactionId, bookId);

            JOptionPane.showMessageDialog(this,
                    "Book returned successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadTransactions();

        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Return Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred while returning book: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = transactionService.getAllTransactions();

        for (Transaction tx : transactions) {
            // Overdue is calculated dynamically without changing database enum value
            String displayStatus = tx.getDisplayStatus();

            tableModel.addRow(new Object[]{
                    tx.getTransactionId(),
                    tx.getBookId(),
                    tx.getMemberId(),
                    tx.getIssueDate(),
                    tx.getDueDate(),
                    tx.getReturnDate() != null ? tx.getReturnDate() : "-",
                    displayStatus
            });
        }
    }

    private void clearFields() {
        bookIdField.setText("");
        memberIdField.setText("");
        loanDaysField.setText("14");
        transactionTable.clearSelection();
    }
}