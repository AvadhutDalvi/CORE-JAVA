package com.banking.gui;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.security.UserSession;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Filterable and searchable transaction statement and financial history panel.
 */
public class TransactionHistoryPanel extends JPanel {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final MainDashboardFrame parentFrame;

    private JComboBox<String> accountFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JTextField searchField;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel summaryLabel;

    private List<Account> userAccounts = new ArrayList<>();
    private List<Transaction> allTransactions = new ArrayList<>();

    public TransactionHistoryPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.transactionService = new TransactionService();
        this.accountService = new AccountService();
        initUI();
        loadHistory();
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

        JLabel titleLabel = new JLabel("Account Statement & Transaction Ledger");
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        summaryLabel = new JLabel("Complete audit trail of all credits, debits, and transfers");
        summaryLabel.setFont(BankingTheme.FONT_BODY);
        summaryLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(summaryLabel);

        headerPanel.add(titleContainer, BorderLayout.WEST);

        // Filter Bar Card
        JPanel filterCard = BankingTheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        // Filter: Account
        JLabel accLabel = new JLabel("Account:");
        accLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        accountFilterCombo = new JComboBox<>(new String[]{"All Accounts"});
        accountFilterCombo.setFont(BankingTheme.FONT_BODY);
        accountFilterCombo.addActionListener(e -> applyFilters());

        // Filter: Type
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        typeFilterCombo = new JComboBox<>(new String[]{"All Activity", "Debits (Outgoing)", "Credits (Incoming)"});
        typeFilterCombo.setFont(BankingTheme.FONT_BODY);
        typeFilterCombo.addActionListener(e -> applyFilters());

        // Search
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        searchField = BankingTheme.createTextField(14);
        searchField.setToolTipText("Filter by reference or description");

        JButton searchBtn = BankingTheme.createSecondaryButton("Filter");
        searchBtn.addActionListener(e -> applyFilters());

        JButton refreshBtn = BankingTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadHistory());

        filterCard.add(accLabel);
        filterCard.add(accountFilterCombo);
        filterCard.add(typeLabel);
        filterCard.add(typeFilterCombo);
        filterCard.add(searchLabel);
        filterCard.add(searchField);
        filterCard.add(searchBtn);
        filterCard.add(refreshBtn);

        // Table Card
        JPanel tableCard = BankingTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] columns = {
                "Reference Number", "Date & Time", "Type", "Direction", "Amount", "Counterparty", "Description", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(BankingTheme.FONT_BODY);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(BankingTheme.FONT_BODY_BOLD);
        historyTable.getTableHeader().setBackground(BankingTheme.COLOR_HIGHLIGHT);

        rowSorter = new TableRowSorter<>(tableModel);
        historyTable.setRowSorter(rowSorter);

        // Amount coloring
        historyTable.getColumnModel().getColumn(4).setCellRenderer(new BankingTheme.TransactionAmountCellRenderer());

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BankingTheme.COLOR_BORDER));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(filterCard, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadHistory() {
        if (!UserSession.isAuthenticated()) return;
        long userId = UserSession.getCurrentSession().getUserId();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                userAccounts = accountService.getAccountsForUser(userId);
                allTransactions = transactionService.getTransactionsForUser(userId);
                return null;
            }

            @Override
            protected void done() {
                // Populate Account Filter
                accountFilterCombo.removeAllItems();
                accountFilterCombo.addItem("All Accounts");
                for (Account acc : userAccounts) {
                    accountFilterCombo.addItem(acc.getAccountNumber() + " (" + acc.getAccountType() + ")");
                }

                applyFilters();
            }
        };
        worker.execute();
    }

    private void applyFilters() {
        tableModel.setRowCount(0);

        String selectedAccountStr = (String) accountFilterCombo.getSelectedItem();
        String selectedType = (String) typeFilterCombo.getSelectedItem();
        String searchTerm = searchField.getText().trim().toLowerCase();

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Transaction txn : allTransactions) {
            boolean isDebit = false;
            boolean matchesAccount = true;

            // Check if outgoing or incoming for this user
            for (Account acc : userAccounts) {
                if (txn.getSourceAccountId() != null && txn.getSourceAccountId() == acc.getAccountId()) {
                    isDebit = true;
                    break;
                }
            }

            // Account filter
            if (selectedAccountStr != null && !selectedAccountStr.equals("All Accounts")) {
                String accNum = selectedAccountStr.split(" ")[0];
                boolean isSource = txn.getSourceAccountNumber() != null && txn.getSourceAccountNumber().equals(accNum);
                boolean isDest = txn.getDestinationAccountNumber() != null && txn.getDestinationAccountNumber().equals(accNum);
                if (!isSource && !isDest) {
                    matchesAccount = false;
                }
            }

            if (!matchesAccount) continue;

            // Type filter
            if (selectedType != null) {
                if (selectedType.startsWith("Debits") && !isDebit) continue;
                if (selectedType.startsWith("Credits") && isDebit) continue;
            }

            // Search query filter
            if (!searchTerm.isEmpty()) {
                String ref = txn.getTransactionReference().toLowerCase();
                String desc = txn.getDescription().toLowerCase();
                if (!ref.contains(searchTerm) && !desc.contains(searchTerm)) {
                    continue;
                }
            }

            String direction = isDebit ? "DEBIT" : "CREDIT";
            String formattedAmount = (isDebit ? "- " : "+ ") + BankingTheme.formatCurrency(txn.getAmount());
            String counterparty = isDebit ?
                    ("To: " + (txn.getDestinationAccountNumber() != null ? txn.getDestinationAccountNumber() : "External")) :
                    ("From: " + (txn.getSourceAccountNumber() != null ? txn.getSourceAccountNumber() : "Cash Deposit"));

            if (isDebit) {
                totalDebits = totalDebits.add(txn.getAmount());
            } else {
                totalCredits = totalCredits.add(txn.getAmount());
            }

            tableModel.addRow(new Object[]{
                    txn.getTransactionReference(),
                    txn.getCreatedAt() != null ? txn.getCreatedAt().toString().replace('T', ' ') : "",
                    txn.getTransactionType(),
                    direction,
                    formattedAmount,
                    counterparty,
                    txn.getDescription(),
                    txn.getStatus()
            });
        }

        summaryLabel.setText(String.format(
                "Statement Summary: %d records found | Total Debits: -%s | Total Credits: +%s",
                tableModel.getRowCount(),
                BankingTheme.formatCurrency(totalDebits),
                BankingTheme.formatCurrency(totalCredits)
        ));
    }
}
