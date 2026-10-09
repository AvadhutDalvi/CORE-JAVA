package com.banking.gui;

import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.security.UserSession;
import com.banking.service.AccountService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * System Administration Panel for bank staff and admins.
 */
public class AdminPanel extends JPanel {

    private final AccountService accountService;
    private final MainDashboardFrame parentFrame;

    private JTable accountsTable;
    private DefaultTableModel tableModel;

    public AdminPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.accountService = new AccountService();
        initUI();
        loadAllAccounts();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BankingTheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 2, 2));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("System Administration & Account Management");
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Administrative control over customer accounts, security freeze, and account status");
        subLabel.setFont(BankingTheme.FONT_BODY);
        subLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(subLabel);

        // Actions toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        JButton blockBtn = new JButton("Freeze / Block Account");
        blockBtn.setFont(BankingTheme.FONT_BODY_BOLD);
        blockBtn.setForeground(Color.WHITE);
        blockBtn.setBackground(BankingTheme.COLOR_DANGER_RED);
        blockBtn.setFocusPainted(false);
        blockBtn.setBorderPainted(false);
        blockBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        blockBtn.addActionListener(e -> changeSelectedAccountStatus(AccountStatus.BLOCKED));

        JButton unblockBtn = new JButton("Activate / Unblock Account");
        unblockBtn.setFont(BankingTheme.FONT_BODY_BOLD);
        unblockBtn.setForeground(Color.WHITE);
        unblockBtn.setBackground(BankingTheme.COLOR_SUCCESS_GREEN);
        unblockBtn.setFocusPainted(false);
        unblockBtn.setBorderPainted(false);
        unblockBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        unblockBtn.addActionListener(e -> changeSelectedAccountStatus(AccountStatus.ACTIVE));

        JButton refreshBtn = BankingTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadAllAccounts());

        toolbar.add(blockBtn);
        toolbar.add(unblockBtn);
        toolbar.add(refreshBtn);

        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // Table Card
        JPanel card = BankingTheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        String[] columns = {"Account ID", "Account Number", "Owner Name", "Type", "Balance", "Current Status", "Created Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        accountsTable = new JTable(tableModel);
        accountsTable.setFont(BankingTheme.FONT_BODY);
        accountsTable.setRowHeight(30);
        accountsTable.getTableHeader().setFont(BankingTheme.FONT_BODY_BOLD);
        accountsTable.getTableHeader().setBackground(BankingTheme.COLOR_HIGHLIGHT);

        accountsTable.getColumnModel().getColumn(4).setCellRenderer(new BankingTheme.TransactionAmountCellRenderer());

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BankingTheme.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void loadAllAccounts() {
        if (!UserSession.isAuthenticated() || !UserSession.getCurrentSession().isAdmin()) return;

        SwingWorker<List<Account>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Account> doInBackground() {
                return accountService.getAllAccounts();
            }

            @Override
            protected void done() {
                try {
                    List<Account> accounts = get();
                    tableModel.setRowCount(0);
                    for (Account acc : accounts) {
                        tableModel.addRow(new Object[]{
                                acc.getAccountId(),
                                acc.getAccountNumber(),
                                acc.getOwnerName() != null ? acc.getOwnerName() : "User #" + acc.getUserId(),
                                acc.getAccountType(),
                                BankingTheme.formatCurrency(acc.getBalance()),
                                acc.getStatus(),
                                acc.getCreatedAt() != null ? acc.getCreatedAt().toLocalDate() : ""
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading admin accounts: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void changeSelectedAccountStatus(AccountStatus targetStatus) {
        int row = accountsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account row first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long accountId = (Long) tableModel.getValueAt(row, 0);
        String accNum = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to change account " + accNum + " status to " + targetStatus + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean updated = accountService.updateAccountStatus(accountId, targetStatus);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Account " + accNum + " is now " + targetStatus + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAllAccounts();
                    parentFrame.refreshAllViews();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update account status.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
