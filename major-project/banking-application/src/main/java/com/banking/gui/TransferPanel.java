package com.banking.gui;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.TransferDraft;
import com.banking.security.UserSession;
import com.banking.service.AccountService;
import com.banking.service.TransferService;
import com.banking.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * High-security fund transfer interface.
 * Features beneficiary account lookup, duplicate-submission guards, and 2FA OTP handoff.
 */
public class TransferPanel extends JPanel {

    private final AccountService accountService;
    private final TransferService transferService;
    private final MainDashboardFrame parentFrame;

    private JComboBox<Account> sourceAccountCombo;
    private JTextField destAccountField;
    private JButton verifyBeneficiaryBtn;
    private JLabel beneficiaryStatusLabel;
    private JTextField amountField;
    private JTextField remarksField;
    private JButton initiateTransferBtn;
    private JLabel statusLabel;

    public TransferPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.accountService = new AccountService();
        this.transferService = new TransferService();
        initUI();
        loadSourceAccounts();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BankingTheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Fund Transfer & Payments");
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        JLabel subtitleLabel = new JLabel("Atomic inter-account funds transfer protected by Two-Factor Authentication (OTP)");
        subtitleLabel.setFont(BankingTheme.FONT_BODY);
        subtitleLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        // Form Card
        JPanel card = BankingTheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;

        // 1. Source Account
        JLabel srcLabel = new JLabel("From Account (Debit)");
        srcLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(srcLabel, gbc);

        sourceAccountCombo = new JComboBox<>();
        sourceAccountCombo.setFont(BankingTheme.FONT_BODY);
        sourceAccountCombo.setPreferredSize(new Dimension(0, 36));
        gbc.gridy = row++;
        card.add(sourceAccountCombo, gbc);

        // 2. Destination Account + Lookup
        JLabel dstLabel = new JLabel("To Account Number (Credit)");
        dstLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(dstLabel, gbc);

        JPanel destPanel = new JPanel(new BorderLayout(10, 0));
        destPanel.setOpaque(false);

        destAccountField = BankingTheme.createTextField(15);
        destAccountField.setToolTipText("Enter 11-digit recipient account number");

        verifyBeneficiaryBtn = BankingTheme.createSecondaryButton("Verify Beneficiary");
        verifyBeneficiaryBtn.addActionListener(e -> verifyBeneficiary());

        destPanel.add(destAccountField, BorderLayout.CENTER);
        destPanel.add(verifyBeneficiaryBtn, BorderLayout.EAST);

        gbc.gridy = row++;
        card.add(destPanel, gbc);

        beneficiaryStatusLabel = new JLabel("Enter destination account number and click Verify Beneficiary");
        beneficiaryStatusLabel.setFont(BankingTheme.FONT_SMALL);
        beneficiaryStatusLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);
        gbc.gridy = row++;
        card.add(beneficiaryStatusLabel, gbc);

        // 3. Amount
        JLabel amtLabel = new JLabel("Transfer Amount (₹)");
        amtLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(amtLabel, gbc);

        amountField = BankingTheme.createTextField(15);
        gbc.gridy = row++;
        card.add(amountField, gbc);

        // Quick amount buttons
        JPanel quickAmtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickAmtPanel.setOpaque(false);

        int[] quickAmounts = {500, 1000, 5000, 10000, 25000};
        for (int q : quickAmounts) {
            JButton qBtn = new JButton("+₹" + q);
            qBtn.setFont(BankingTheme.FONT_SMALL_BOLD);
            qBtn.setForeground(BankingTheme.COLOR_ACCENT_BLUE);
            qBtn.setBackground(BankingTheme.COLOR_HIGHLIGHT);
            qBtn.setBorder(BorderFactory.createLineBorder(BankingTheme.COLOR_BORDER));
            qBtn.setFocusPainted(false);
            qBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            qBtn.addActionListener(e -> {
                try {
                    BigDecimal cur = amountField.getText().trim().isEmpty() ?
                            BigDecimal.ZERO : new BigDecimal(amountField.getText().trim());
                    amountField.setText(cur.add(BigDecimal.valueOf(q)).toPlainString());
                } catch (Exception ex) {
                    amountField.setText(String.valueOf(q));
                }
            });
            quickAmtPanel.add(qBtn);
        }
        gbc.gridy = row++;
        card.add(quickAmtPanel, gbc);

        // 4. Remarks
        JLabel remLabel = new JLabel("Payment Remarks / Description");
        remLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(remLabel, gbc);

        remarksField = BankingTheme.createTextField(20);
        remarksField.setText("Bill payment / Fund transfer");
        gbc.gridy = row++;
        card.add(remarksField, gbc);

        // Status Label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(BankingTheme.FONT_SMALL_BOLD);
        statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
        gbc.gridy = row++;
        card.add(statusLabel, gbc);

        // Transfer Button
        initiateTransferBtn = BankingTheme.createPrimaryButton("Proceed to Two-Factor Verification");
        initiateTransferBtn.setPreferredSize(new Dimension(0, 42));
        initiateTransferBtn.addActionListener(e -> initiateTransferFlow());
        gbc.gridy = row++;
        card.add(initiateTransferBtn, gbc);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadSourceAccounts() {
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
                    List<Account> accounts = get();
                    sourceAccountCombo.removeAllItems();
                    for (Account a : accounts) {
                        sourceAccountCombo.addItem(a);
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading source accounts: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void verifyBeneficiary() {
        String destNum = destAccountField.getText().trim();
        if (destNum.isEmpty()) {
            beneficiaryStatusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
            beneficiaryStatusLabel.setText("Please enter an account number.");
            return;
        }

        verifyBeneficiaryBtn.setEnabled(false);
        beneficiaryStatusLabel.setText("Looking up account details...");

        SwingWorker<Optional<Account>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<Account> doInBackground() {
                return accountService.getAccountByNumber(destNum);
            }

            @Override
            protected void done() {
                verifyBeneficiaryBtn.setEnabled(true);
                try {
                    Optional<Account> accOpt = get();
                    if (accOpt.isPresent()) {
                        Account acc = accOpt.get();
                        beneficiaryStatusLabel.setForeground(BankingTheme.COLOR_SUCCESS_GREEN);
                        beneficiaryStatusLabel.setText("✓ Verified Beneficiary: " + acc.getOwnerName() +
                                                       " (" + acc.getAccountType() + ", Status: " + acc.getStatus() + ")");
                    } else {
                        beneficiaryStatusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
                        beneficiaryStatusLabel.setText("✗ Account not found. Please re-check the account number.");
                    }
                } catch (Exception ex) {
                    beneficiaryStatusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
                    beneficiaryStatusLabel.setText("Error verifying account: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void initiateTransferFlow() {
        Account sourceAccount = (Account) sourceAccountCombo.getSelectedItem();
        String destAccountNumber = destAccountField.getText().trim();
        String amountText = amountField.getText().trim();
        String remarks = remarksField.getText().trim();

        if (sourceAccount == null) {
            statusLabel.setText("Please select a source account.");
            return;
        }

        if (destAccountNumber.isEmpty()) {
            statusLabel.setText("Please enter the destination account number.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount format. Please enter a valid number (e.g. 500.00).");
            return;
        }

        // Disable button during draft creation to prevent duplicate submission
        initiateTransferBtn.setEnabled(false);
        initiateTransferBtn.setText("Generating Secure OTP Challenge...");
        statusLabel.setText(" ");

        long userId = UserSession.getCurrentSession().getUserId();

        SwingWorker<TransferDraft, Void> worker = new SwingWorker<>() {
            @Override
            protected TransferDraft doInBackground() {
                return transferService.initiateTransfer(
                        userId,
                        sourceAccount.getAccountId(),
                        destAccountNumber,
                        amount,
                        remarks
                );
            }

            @Override
            protected void done() {
                initiateTransferBtn.setEnabled(true);
                initiateTransferBtn.setText("Proceed to Two-Factor Verification");

                try {
                    TransferDraft draft = get();

                    // Open OTP verification modal dialog
                    OtpDialog otpDialog = new OtpDialog(parentFrame, draft, transferService);
                    otpDialog.setVisible(true);

                    Transaction completedTxn = otpDialog.getCompletedTransaction();
                    if (completedTxn != null) {
                        showTransferSuccessDialog(completedTxn);
                        resetForm();
                        parentFrame.refreshAllViews();
                    }

                } catch (Exception ex) {
                    String msg = ex.getCause() instanceof ValidationException ?
                            ex.getCause().getMessage() : "Failed to initiate transfer: " + ex.getMessage();
                    statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
                    statusLabel.setText(msg);
                }
            }
        };
        worker.execute();
    }

    private void showTransferSuccessDialog(Transaction txn) {
        String message = String.format(
                "Payment Executed Successfully!\n\n" +
                "Transaction Reference: %s\n" +
                "Amount: %s\n" +
                "Source Account: %s\n" +
                "Recipient Account: %s\n" +
                "Status: %s\n" +
                "Timestamp: %s\n\n" +
                "Your account balance has been updated.",
                txn.getTransactionReference(),
                BankingTheme.formatCurrency(txn.getAmount()),
                txn.getSourceAccountNumber(),
                txn.getDestinationAccountNumber(),
                txn.getStatus(),
                txn.getCreatedAt() != null ? txn.getCreatedAt().toString().replace('T', ' ') : "Just now"
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Transfer Successful",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void resetForm() {
        destAccountField.setText("");
        amountField.setText("");
        remarksField.setText("Bill payment / Fund transfer");
        beneficiaryStatusLabel.setText("Enter destination account number and click Verify Beneficiary");
        beneficiaryStatusLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);
        statusLabel.setText(" ");
        loadSourceAccounts();
    }
}
