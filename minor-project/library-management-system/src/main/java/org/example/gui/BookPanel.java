package org.example.gui;


import org.example.model.Book;
import org.example.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookPanel extends JPanel {

    private final BookService bookService;

    private JTextField titleField;
    private JTextField authorField;
    private JTextField categoryField;
    private JTextField isbnField;
    private JTextField quantityField;
    private JTextField yearField;
    private JTextField searchField;

    private JTable bookTable;
    private DefaultTableModel tableModel;

    public BookPanel() {

        bookService = new BookService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createForm();
        createTable();

        loadBooks();
    }

    private void createForm() {

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));

        titleField = new JTextField();
        authorField = new JTextField();
        categoryField = new JTextField();
        isbnField = new JTextField();
        quantityField = new JTextField();
        yearField = new JTextField();

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);

        formPanel.add(new JLabel("Author:"));
        formPanel.add(authorField);

        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);

        formPanel.add(new JLabel("ISBN:"));
        formPanel.add(isbnField);

        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);

        formPanel.add(new JLabel("Published Year:"));
        formPanel.add(yearField);

        JButton addButton = new JButton("Add Book");
        JButton updateButton = new JButton("Update Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton refreshButton = new JButton("Refresh");

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        addButton.addActionListener(e -> addBook());
        refreshButton.addActionListener(e -> loadBooks());
        updateButton.addActionListener(e -> updateBook());
        deleteButton.addActionListener(e -> deleteBook());
    }

    private void createTable() {

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(25);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchButton.addActionListener(e -> {

            String keyword = searchField.getText().trim();

            if (keyword.isEmpty()) {
                loadBooks();
            } else {
                loadSearchResults(keyword);
            }
        });

        showAllButton.addActionListener(e -> {
            searchField.setText("");
            loadBooks();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        String[] columns = {
                "ID",
                "Title",
                "Author",
                "Category",
                "ISBN",
                "Quantity",
                "Available",
                "Year"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);

        bookTable.getSelectionModel().addListSelectionListener(e -> {

            int selectedRow = bookTable.getSelectedRow();

            if (selectedRow != -1) {

                titleField.setText(
                        tableModel.getValueAt(selectedRow, 1).toString()
                );

                authorField.setText(
                        tableModel.getValueAt(selectedRow, 2).toString()
                );

                categoryField.setText(
                        tableModel.getValueAt(selectedRow, 3).toString()
                );

                isbnField.setText(
                        tableModel.getValueAt(selectedRow, 4).toString()
                );

                quantityField.setText(
                        tableModel.getValueAt(selectedRow, 5).toString()
                );

                yearField.setText(
                        tableModel.getValueAt(selectedRow, 7).toString()
                );
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);

        JPanel tablePanel = new JPanel(new BorderLayout());

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);


    }

    private void addBook() {

        try {

            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String isbn = isbnField.getText().trim();

            int quantity = Integer.parseInt(quantityField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());

            Book book = new Book(
                    title,
                    author,
                    category,
                    isbn,
                    quantity,
                    quantity,
                    year
            );

            boolean success = bookService.addBook(book);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book added successfully!"
                );

                clearFields();
                loadBooks();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Failed to add book."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Quantity and year must be valid numbers."
            );
        }
    }

    private void loadBooks() {

        tableModel.setRowCount(0);

        List<Book> books = bookService.getAllBooks();

        for (Book book : books) {

            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getIsbn(),
                    book.getQuantity(),
                    book.getAvailableQuantity(),
                    book.getPublishedYear()
            });
        }
    }

    private void clearFields() {

        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        isbnField.setText("");
        quantityField.setText("");
        yearField.setText("");
    }

    private void updateBook() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book to update."
            );
            return;
        }

        try {

            int bookId = (int) tableModel.getValueAt(selectedRow, 0);

            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String isbn = isbnField.getText().trim();

            int quantity = Integer.parseInt(quantityField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());

            Book book = new Book(
                    bookId,
                    title,
                    author,
                    category,
                    isbn,
                    quantity,
                    quantity,
                    year
            );

            boolean success = bookService.updateBook(book);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book updated successfully!"
                );

                clearFields();
                loadBooks();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update book."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Quantity and year must be valid numbers."
            );
        }
    }

    private void deleteBook() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book to delete."
            );
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this book?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            boolean success = bookService.deleteBook(bookId);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book deleted successfully!"
                );

                clearFields();
                loadBooks();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Failed to delete book."
                );
            }
        }
    }

    private void loadSearchResults(String keyword) {

        tableModel.setRowCount(0);

        List<Book> books = bookService.searchBooks(keyword);

        for (Book book : books) {

            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getIsbn(),
                    book.getQuantity(),
                    book.getAvailableQuantity(),
                    book.getPublishedYear()
            });
        }
    }
}
