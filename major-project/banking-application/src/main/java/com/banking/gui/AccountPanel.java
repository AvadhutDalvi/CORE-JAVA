package com.banking.gui;

import com.banking.model.Account;
import com.banking.model.AccountType;
import com.banking.security.UserSession;
import com.banking.service.AccountService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Customer Account management panel.
 */
public class AccountPanel extends JPanel {

    private final AccountService accountService;
    private final MainDashboardFrame parentFrame;

    private JTable accountsTable;
    private DefaultTableModel tableModel;
    private JLabel totalAccountsSummaryLabel;

    public AccountPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.accountService = new AccountService();
        initUI();
        loadAccounts();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BankingTheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 2, 2));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("My Bank Accounts");
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        totalAccountsSummaryLabel = new JLabel("Manage your savings, checking, and deposit accounts");
        totalAccountsSummaryLabel.setFont(BankingTheme.FONT_BODY);
        totalAccountsSummaryLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(totalAccountsSummaryLabel);

        // Actions toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        JButton openAccountBtn = BankingTheme.createPrimaryButton("+ Open Additional Account");
        openAccountBtn.addActionListener(e -> promptOpenNewAccount());

        JButton copyAccBtn = BankingTheme.createSecondaryButton("Copy Selected Account #");
        copyAccBtn.addActionListener(e -> copySelectedAccountNumber());

        JButton refreshBtn = BankingTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadAccounts());

        toolbar.add(copyAccBtn);
        toolbar.add(openAccountBtn);
        toolbar.add(refreshBtn);

        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // Table Card
        JPanel card = BankingTheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        String[] columnNames = {"Account ID", "Account Number", "Account Type", "Available Balance", "Status", "Opened On"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountsTable = new JTable(tableModel);
        accountsTable.setFont(BankingTheme.FONT_BODY);
        accountsTable.setRowHeight(32);
        accountsTable.getTableHeader().setFont(BankingTheme.FONT_BODY_BOLD);
        accountsTable.getTableHeader().setBackground(BankingTheme.COLOR_HIGHLIGHT);

        // Balance right-aligned renderer
        accountsTable.getColumnModel().getColumn(3).setCellRenderer(new BankingTheme.TransactionAmountCellRenderer());

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BankingTheme.COLOR_BORDER));

        card.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void loadAccounts() {
        if (!UserSession.isAuthenticated()) return;
        long userId = UserSession.getCurrentSession().getUserId();

        SwingWorker<List<Account>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Account> doInBackground() {
                return accountService.getAccountsForUser(userId);
            }

            @Override
            protected void done() {
                try {
                    List<Account> list = get();
                    tableModel.setRowCount(0);
                    for (Account acc : list) {
                        tableModel.addRow(new Object[]{
                                acc.getAccountId(),
                                acc.getAccountNumber(),
                                acc.getAccountType(),
                                BankingTheme.formatCurrency(acc.getBalance()),
                                acc.getStatus(),
                                acc.getCreatedAt() != null ? acc.getCreatedAt().toLocalDate() : ""
                        });
                    }
                    totalAccountsSummaryLabel.setText("You have " + list.size() + " registered accounts with Apex Trust Bank.");
                } catch (Exception ex) {
                    System.err.println("Error loading accounts: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void promptOpenNewAccount() {
        JComboBox<AccountType> typeBox = new JComboBox<>(AccountType.values());
        int option = JOptionPane.showConfirmDialog(
                this,
                new Object[]{"Select Account Type to Open:", typeBox},
                "Open Additional Bank Account",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            AccountType selectedType = (AccountType) typeBox.getSelectedItem();
            long userId = UserSession.getCurrentSession().getUserId();

            try {
                Account newAcc = accountService.createAdditionalAccount(userId, selectedType);
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully opened new " + selectedType + " Account!\nAccount Number: " + newAcc.getAccountNumber(),
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadAccounts();
                parentFrame.refreshAllViews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copySelectedAccountNumber() {
        int row = accountsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account row first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String accNum = tableModel.getValueAt(row, 1).toString();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(accNum), null);
        JOptionPane.showMessageDialog(this, "Copied account number to clipboard: " + accNum, "Copied", JOptionPane.INFORMATION_MESSAGE);
    }
}
