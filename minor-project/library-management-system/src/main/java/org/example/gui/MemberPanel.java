package org.example.gui;

import org.example.model.Member;
import org.example.service.MemberService;
import org.example.util.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MemberPanel extends JPanel {

    private final MemberService memberService;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField searchField;

    private JTable memberTable;
    private DefaultTableModel tableModel;

    public MemberPanel() {
        this.memberService = new MemberService();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createForm();
        createTable();

        loadMembers();
    }

    private void createForm() {
        JPanel formContainer = new JPanel(new BorderLayout(8, 8));
        formContainer.setBorder(BorderFactory.createTitledBorder("Member Details"));

        JPanel formGrid = new JPanel(new GridLayout(2, 4, 12, 10));
        formGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();

        formGrid.add(new JLabel("Name: *"));
        formGrid.add(nameField);

        formGrid.add(new JLabel("Email: *"));
        formGrid.add(emailField);

        formGrid.add(new JLabel("Phone (10 digits):"));
        formGrid.add(phoneField);

        formGrid.add(new JLabel("Address:"));
        formGrid.add(addressField);

        formContainer.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        JButton addButton = new JButton("Add Member");
        JButton updateButton = new JButton("Update Member");
        JButton deleteButton = new JButton("Delete Member");
        JButton clearButton = new JButton("Clear Fields");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(formContainer, BorderLayout.NORTH);

        addButton.addActionListener(e -> addMember());
        updateButton.addActionListener(e -> updateMember());
        deleteButton.addActionListener(e -> deleteMember());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadMembers();
        });
    }

    private void createTable() {
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Registered Members"));

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchField = new JTextField(24);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchPanel.add(new JLabel("Search (Name / Email / Phone):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        tableContainer.add(searchPanel, BorderLayout.NORTH);

        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                loadMembers();
            } else {
                loadSearchResults(keyword);
            }
        });

        showAllButton.addActionListener(e -> {
            searchField.setText("");
            loadMembers();
        });

        String[] columns = {
                "ID",
                "Name",
                "Email",
                "Phone",
                "Address",
                "Registration Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setRowHeight(24);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setAutoCreateRowSorter(true);

        // Center align ID and Registration Date
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        memberTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        memberTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    private void populateFieldsFromSelection() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = memberTable.convertRowIndexToModel(selectedRow);

            nameField.setText(getSafeString(tableModel.getValueAt(modelRow, 1)));
            emailField.setText(getSafeString(tableModel.getValueAt(modelRow, 2)));
            phoneField.setText(getSafeString(tableModel.getValueAt(modelRow, 3)));
            addressField.setText(getSafeString(tableModel.getValueAt(modelRow, 4)));
        }
    }

    private String getSafeString(Object value) {
        return value != null ? value.toString() : "";
    }

    private void addMember() {
        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            Member member = new Member(
                    name,
                    email,
                    phone.isEmpty() ? null : phone,
                    address.isEmpty() ? null : address
            );

            memberService.addMember(member);

            JOptionPane.showMessageDialog(this,
                    "Member added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadMembers();

        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred while adding member: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMember() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a member from the table to update.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = memberTable.convertRowIndexToModel(selectedRow);
        int memberId = (int) tableModel.getValueAt(modelRow, 0);

        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            Member member = new Member(
                    name,
                    email,
                    phone.isEmpty() ? null : phone,
                    address.isEmpty() ? null : address
            );
            member.setMemberId(memberId);

            memberService.updateMember(member);

            JOptionPane.showMessageDialog(this,
                    "Member updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            loadMembers();

        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred while updating member: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMember() {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a member from the table to delete.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = memberTable.convertRowIndexToModel(selectedRow);
        int memberId = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete member '" + name + "' (ID: " + memberId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                memberService.deleteMember(memberId);

                JOptionPane.showMessageDialog(this,
                        "Member deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearFields();
                loadMembers();

            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this,
                        e.getMessage(),
                        "Unable to Delete",
                        JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete member: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadMembers() {
        tableModel.setRowCount(0);
        List<Member> members = memberService.getAllMembers();

        for (Member member : members) {
            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone() != null ? member.getPhone() : "",
                    member.getAddress() != null ? member.getAddress() : "",
                    member.getRegistrationDate()
            });
        }
    }

    private void loadSearchResults(String keyword) {
        tableModel.setRowCount(0);
        List<Member> members = memberService.searchMembers(keyword);

        for (Member member : members) {
            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone() != null ? member.getPhone() : "",
                    member.getAddress() != null ? member.getAddress() : "",
                    member.getRegistrationDate()
            });
        }
    }

    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        memberTable.clearSelection();
    }
}