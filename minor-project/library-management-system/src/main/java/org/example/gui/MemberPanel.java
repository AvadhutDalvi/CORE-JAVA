package org.example.gui;

import org.example.model.Member;
import org.example.service.MemberService;

import javax.swing.*;
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

        memberService = new MemberService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createForm();
        createTable();

        loadMembers();
    }

    private void createForm() {

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);

        JButton addButton = new JButton("Add Member");
        JButton updateButton = new JButton("Update Member");
        JButton deleteButton = new JButton("Delete Member");
        JButton clearButton = new JButton("Clear");

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        addButton.addActionListener(e -> addMember());
        updateButton.addActionListener(e -> updateMember());
        deleteButton.addActionListener(e -> deleteMember());
        clearButton.addActionListener(e -> clearFields());
    }

    private void createTable() {

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(25);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

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

        memberTable.getSelectionModel().addListSelectionListener(e -> {

            int selectedRow = memberTable.getSelectedRow();

            if (selectedRow != -1) {

                nameField.setText(
                        tableModel.getValueAt(selectedRow, 1).toString()
                );

                emailField.setText(
                        tableModel.getValueAt(selectedRow, 2).toString()
                );

                phoneField.setText(
                        tableModel.getValueAt(selectedRow, 3).toString()
                );

                addressField.setText(
                        tableModel.getValueAt(selectedRow, 4).toString()
                );
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberTable);

        JPanel tablePanel = new JPanel(new BorderLayout());

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

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
    }

    private void addMember() {

        Member member = getMemberFromFields();

        if (member == null) {
            return;
        }

        boolean success = memberService.addMember(member);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Member added successfully!"
            );

            clearFields();
            loadMembers();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid member details or email already exists."
            );
        }
    }

    private void updateMember() {

        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a member to update."
            );

            return;
        }

        int memberId =
                (int) tableModel.getValueAt(selectedRow, 0);

        Member member = getMemberFromFields();

        if (member == null) {
            return;
        }

        member.setMemberId(memberId);

        boolean success = memberService.updateMember(member);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Member updated successfully!"
            );

            clearFields();
            loadMembers();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update member."
            );
        }
    }

    private void deleteMember() {

        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a member to delete."
            );

            return;
        }

        int memberId =
                (int) tableModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this member?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            boolean success =
                    memberService.deleteMember(memberId);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Member deleted successfully!"
                );

                clearFields();
                loadMembers();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to delete member."
                );
            }
        }
    }

    private Member getMemberFromFields() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Name and Email are required."
            );

            return null;
        }

        return new Member(
                name,
                email,
                phone,
                address
        );
    }

    private void loadMembers() {

        tableModel.setRowCount(0);

        List<Member> members =
                memberService.getAllMembers();

        for (Member member : members) {

            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getAddress(),
                    member.getRegistrationDate()
            });
        }
    }

    private void loadSearchResults(String keyword) {

        tableModel.setRowCount(0);

        List<Member> members =
                memberService.searchMembers(keyword);

        for (Member member : members) {

            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getAddress(),
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