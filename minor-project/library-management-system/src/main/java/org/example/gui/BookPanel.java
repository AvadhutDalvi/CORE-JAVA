package org.example.gui;

import org.example.model.Book;
import org.example.service.BookService;
import org.example.util.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
        this.bookService = new BookService();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createForm();
        createTable();

        loadBooks();
    }

    private void createForm() {
        JPanel formContainer = new JPanel(new BorderLayout(8, 8));
        formContainer.setBorder(BorderFactory.createTitledBorder("Book Details"));

        JPanel formGrid = new JPanel(new GridLayout(3, 4, 12, 10));
        formGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        titleField = new JTextField();
        authorField = new JTextField();
        categoryField = new JTextField();
        isbnField = new JTextField();
        quantityField = new JTextField();
        yearField = new JTextField();

        formGrid.add(new JLabel("Title: *"));
        formGrid.add(titleField);

        formGrid.add(new JLabel("Author: *"));
        formGrid.add(authorField);

        formGrid.add(new JLabel("Category:"));
        formGrid.add(categoryField);

        formGrid.add(new JLabel("ISBN:"));
        formGrid.add(isbnField);

        formGrid.add(new JLabel("Total Quantity: *"));
        formGrid.add(quantityField);

        formGrid.add(new JLabel("Published Year: *"));
        formGrid.add(yearField);

        formContainer.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        JButton addButton = new JButton("Add Book");
        JButton updateButton = new JButton("Update Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton clearButton = new JButton("Clear Fields");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(formContainer, BorderLayout.NORTH);

        addButton.addActionListener(e -> addBook());
        updateButton.addActionListener(e -> updateBook());
        deleteButton.addActionListener(e -> deleteBook());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBooks();
        });
    }

    private void createTable() {
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Book Catalog"));

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchField = new JTextField(24);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchPanel.add(new JLabel("Search (Title / Author / ISBN / Category):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        tableContainer.add(searchPanel, BorderLayout.NORTH);

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

        String[] columns = {
                "ID",
                "Title",
                "Author",
                "Category",
                "ISBN",
                "Total Qty",
                "Available Qty",
                "Year"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(24);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setAutoCreateRowSorter(true);

        // Center align ID, Quantities, and Year
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        bookTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        bookTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        bookTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        bookTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    private void populateFieldsFromSelection() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = bookTable.convertRowIndexToModel(selectedRow);

            titleField.setText(getSafeString(tableModel.getValueAt(modelRow, 1)));
            authorField.setText(getSafeString(tableModel.getValueAt(modelRow, 2)));
            categoryField.setText(getSafeString(tableModel.getValueAt(modelRow, 3)));
            isbnField.setText(getSafeString(tableModel.getValueAt(modelRow, 4)));
            quantityField.setText(getSafeString(tableModel.getValueAt(modelRow, 5)));
            yearField.setText(getSafeString(tableModel.getValueAt(modelRow, 7)));
        }
    }

    private String getSafeString(Object value) {
        return value != null ? value.toString() : "";
    }

    private void addBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String isbn = isbnField.getText().trim();
            String qtyText = quantityField.getText().trim();
            String yearText = yearField.getText().trim();

            if (qtyText.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Total Quantity and Published Year are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int quantity = Integer.parseInt(qtyText);
            int year = Integer.parseInt(yearText);

            Book book = new Book(
                    title,
                    author,
                    category.isEmpty() ? null : category,
                    isbn.isEmpty() ? null : isbn,
                    quantity,
                    quantity,
                    year
            );

            bookService.addBook(book);

            JOptionPane.showMessageDialog(this,
                    "Book added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadBooks();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Total Quantity and Published Year must be valid integers.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred while adding the book: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book from the table to update.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        int bookId = (int) tableModel.getValueAt(modelRow, 0);

        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String isbn = isbnField.getText().trim();
            String qtyText = quantityField.getText().trim();
            String yearText = yearField.getText().trim();

            if (qtyText.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Total Quantity and Published Year are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int quantity = Integer.parseInt(qtyText);
            int year = Integer.parseInt(yearText);

            Book book = new Book(
                    bookId,
                    title,
                    author,
                    category.isEmpty() ? null : category,
                    isbn.isEmpty() ? null : isbn,
                    quantity,
                    0, // Service computes available quantity properly based on loans delta
                    year
            );

            bookService.updateBook(book);

            JOptionPane.showMessageDialog(this,
                    "Book updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadBooks();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Total Quantity and Published Year must be valid integers.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred while updating the book: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book from the table to delete.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        int bookId = (int) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 1);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete book '" + title + "' (ID: " + bookId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                bookService.deleteBook(bookId);

                JOptionPane.showMessageDialog(this,
                        "Book deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearFields();
                loadBooks();

            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this,
                        e.getMessage(),
                        "Unable to Delete",
                        JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete book: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
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
                    book.getCategory() != null ? book.getCategory() : "",
                    book.getIsbn() != null ? book.getIsbn() : "",
                    book.getQuantity(),
                    book.getAvailableQuantity(),
                    book.getPublishedYear()
            });
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
                    book.getCategory() != null ? book.getCategory() : "",
                    book.getIsbn() != null ? book.getIsbn() : "",
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
        bookTable.clearSelection();
    }
}
