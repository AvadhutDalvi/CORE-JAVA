package com.banking.gui;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.security.UserSession;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Main overview dashboard panel showing account balances and recent activity.
 */
public class DashboardPanel extends JPanel {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final MainDashboardFrame parentFrame;

    private JLabel totalBalanceLabel;
    private JLabel activeAccountsLabel;
    private JTable recentTransactionsTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.accountService = new AccountService();
        this.transactionService = new TransactionService();
        initUI();
        loadDashboardData();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BankingTheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. Metric Cards Row
        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        metricsRow.setOpaque(false);

        User currentUser = UserSession.getCurrentSession().getUser();

        JPanel balanceCard = BankingTheme.createMetricCard("Total Liquid Balance", "Loading...", BankingTheme.COLOR_ACCENT_BLUE);
        JPanel accountsCard = BankingTheme.createMetricCard("Active Bank Accounts", "Loading...", BankingTheme.COLOR_SUCCESS_GREEN);
        JPanel profileCard = BankingTheme.createMetricCard("Account Security", "2FA Active (OTP)", BankingTheme.COLOR_SECONDARY_NAVY);

        totalBalanceLabel = (JLabel) ((JPanel) balanceCard.getComponent(1)).getComponent(1);
        activeAccountsLabel = (JLabel) ((JPanel) accountsCard.getComponent(1)).getComponent(1);

        metricsRow.add(balanceCard);
        metricsRow.add(accountsCard);
        metricsRow.add(profileCard);

        // 2. Center Content: Quick Actions & Recent Transactions
        JPanel centerContainer = new JPanel(new BorderLayout(15, 15));
        centerContainer.setOpaque(false);

        // Quick Actions Card
        JPanel quickActionsCard = BankingTheme.createCardPanel();
        quickActionsCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JLabel qaLabel = new JLabel("Quick Actions:");
        qaLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        qaLabel.setForeground(BankingTheme.COLOR_TEXT_PRIMARY);

        JButton transferBtn = BankingTheme.createPrimaryButton("Send Money (Transfer)");
        transferBtn.addActionListener(e -> parentFrame.navigateToTransfer());

        JButton viewAccountsBtn = BankingTheme.createSecondaryButton("My Accounts");
        viewAccountsBtn.addActionListener(e -> parentFrame.navigateToAccounts());

        JButton statementBtn = BankingTheme.createSecondaryButton("View Full Statement");
        statementBtn.addActionListener(e -> parentFrame.navigateToHistory());

        JButton refreshBtn = BankingTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadDashboardData());

        quickActionsCard.add(qaLabel);
        quickActionsCard.add(transferBtn);
        quickActionsCard.add(viewAccountsBtn);
        quickActionsCard.add(statementBtn);
        quickActionsCard.add(refreshBtn);

        // Recent Transactions Section
        JPanel transactionsCard = BankingTheme.createCardPanel();
        transactionsCard.setLayout(new BorderLayout(10, 10));

        JPanel txnHeader = new JPanel(new BorderLayout());
        txnHeader.setOpaque(false);

        JLabel txnTitle = new JLabel("Recent Financial Activity (Last 5 Transactions)");
        txnTitle.setFont(BankingTheme.FONT_SECTION_TITLE);
        txnTitle.setForeground(BankingTheme.COLOR_TEXT_PRIMARY);

        txnHeader.add(txnTitle, BorderLayout.WEST);

        // Table
        String[] columnNames = {"Reference", "Date & Time", "Type", "Counterparty / Account", "Description", "Amount", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTransactionsTable = new JTable(tableModel);
        recentTransactionsTable.setFont(BankingTheme.FONT_BODY);
        recentTransactionsTable.setRowHeight(28);
        recentTransactionsTable.getTableHeader().setFont(BankingTheme.FONT_BODY_BOLD);
        recentTransactionsTable.getTableHeader().setBackground(BankingTheme.COLOR_HIGHLIGHT);
        recentTransactionsTable.getColumnModel().getColumn(5).setCellRenderer(new BankingTheme.TransactionAmountCellRenderer());

        JScrollPane tableScroll = new JScrollPane(recentTransactionsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BankingTheme.COLOR_BORDER));

        transactionsCard.add(txnHeader, BorderLayout.NORTH);
        transactionsCard.add(tableScroll, BorderLayout.CENTER);

        centerContainer.add(quickActionsCard, BorderLayout.NORTH);
        centerContainer.add(transactionsCard, BorderLayout.CENTER);

        add(metricsRow, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    public void loadDashboardData() {
        if (!UserSession.isAuthenticated()) return;
        long userId = UserSession.getCurrentSession().getUserId();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private BigDecimal totalBalance;
            private List<Account> accounts;
            private List<Transaction> recentTransactions;

            @Override
            protected Void doInBackground() {
                totalBalance = accountService.getTotalBalanceForUser(userId);
                accounts = accountService.getAccountsForUser(userId);
                recentTransactions = transactionService.getRecentTransactions(userId, 5);
                return null;
            }

            @Override
            protected void done() {
                totalBalanceLabel.setText(BankingTheme.formatCurrency(totalBalance));
                activeAccountsLabel.setText(String.valueOf(accounts.size()));

                tableModel.setRowCount(0);
                if (recentTransactions != null) {
                    for (Transaction txn : recentTransactions) {
                        String counterparty;
                        String formattedAmount;

                        boolean isOutgoing = false;
                        for (Account a : accounts) {
                            if (txn.getSourceAccountId() != null && txn.getSourceAccountId() == a.getAccountId()) {
                                isOutgoing = true;
                                break;
                            }
                        }

                        if (isOutgoing) {
                            counterparty = "To: " + (txn.getDestinationAccountNumber() != null ? txn.getDestinationAccountNumber() : "External");
                            formattedAmount = "- " + BankingTheme.formatCurrency(txn.getAmount());
                        } else {
                            counterparty = "From: " + (txn.getSourceAccountNumber() != null ? txn.getSourceAccountNumber() : "Deposit");
                            formattedAmount = "+ " + BankingTheme.formatCurrency(txn.getAmount());
                        }

                        tableModel.addRow(new Object[]{
                                txn.getTransactionReference(),
                                txn.getCreatedAt() != null ? txn.getCreatedAt().toString().replace('T', ' ') : "",
                                txn.getTransactionType(),
                                counterparty,
                                txn.getDescription(),
                                formattedAmount,
                                txn.getStatus()
                        });
                    }
                }
            }
        };
        worker.execute();
    }
}
