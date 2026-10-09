package com.banking.gui;

import com.banking.model.Transaction;
import com.banking.model.TransferDraft;
import com.banking.security.SimulatedOtpDeliveryService;
import com.banking.service.TransferService;
import com.banking.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Security OTP verification modal dialog.
 * Prompts user for 6-digit one-time password with attempt tracking and simulation banner.
 */
public class OtpDialog extends JDialog {

    private final TransferService transferService;
    private final TransferDraft draft;
    private Transaction completedTransaction;

    private JTextField otpField;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JLabel timerLabel;
    private Timer countdownTimer;
    private int remainingSeconds = 300; // 5 minutes

    public OtpDialog(Frame parent, TransferDraft draft, TransferService transferService) {
        super(parent, "Two-Factor Security Verification", true);
        this.draft = draft;
        this.transferService = transferService;
        initUI();
    }

    private void initUI() {
        setSize(520, 500);
        setResizable(false);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(BankingTheme.COLOR_BACKGROUND);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BankingTheme.COLOR_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Authorize Fund Transfer", SwingConstants.CENTER);
        titleLabel.setFont(BankingTheme.FONT_SECTION_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        JLabel subtitleLabel = new JLabel("A one-time security password is required to finalize this payment", SwingConstants.CENTER);
        subtitleLabel.setFont(BankingTheme.FONT_SMALL);
        subtitleLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        // Center Content Container
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Payment Summary Box
        JPanel summaryBox = BankingTheme.createCardPanel();
        summaryBox.setLayout(new GridLayout(4, 2, 8, 6));

        addSummaryRow(summaryBox, "Amount to Transfer:", BankingTheme.formatCurrency(draft.getAmount()), true);
        addSummaryRow(summaryBox, "Source Account:", draft.getSourceAccountNumber(), false);
        addSummaryRow(summaryBox, "Recipient Account:", draft.getDestinationAccountNumber(), false);
        addSummaryRow(summaryBox, "Beneficiary Name:", draft.getDestinationOwnerName() != null ? draft.getDestinationOwnerName() : "Verified Account", false);

        centerPanel.add(summaryBox);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Development Simulation Notice
        JPanel devBanner = new JPanel(new BorderLayout(8, 8));
        devBanner.setBackground(new Color(254, 243, 199)); // Amber / yellow notice
        devBanner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(245, 158, 11), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        String devOtp = SimulatedOtpDeliveryService.getInstance().getLastDeliveredOtp();
        JLabel devBannerText = new JLabel(
                "<html><b>[DEVELOPMENT SIMULATION]</b><br>" +
                "In production, OTP is dispatched to verified SMS/Email.<br>" +
                "Simulated Test OTP: <font color='#B45309' size='+1'><b>" + (devOtp != null ? devOtp : "******") + "</b></font></html>"
        );
        devBannerText.setFont(BankingTheme.FONT_SMALL);
        devBannerText.setForeground(new Color(146, 64, 14));
        devBanner.add(devBannerText, BorderLayout.CENTER);

        centerPanel.add(devBanner);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // OTP Input Card
        JPanel inputCard = BankingTheme.createCardPanel();
        inputCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel enterLabel = new JLabel("Enter 6-Digit OTP Code", SwingConstants.CENTER);
        enterLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        enterLabel.setForeground(BankingTheme.COLOR_TEXT_PRIMARY);
        gbc.gridy = 0;
        inputCard.add(enterLabel, gbc);

        otpField = new JTextField(10);
        otpField.setFont(new Font(BankingTheme.FONT_FAMILY, Font.BOLD, 22));
        otpField.setHorizontalAlignment(JTextField.CENTER);
        otpField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BankingTheme.COLOR_BORDER, 2),
                new EmptyBorder(6, 12, 6, 12)
        ));
        gbc.gridy = 1;
        inputCard.add(otpField, gbc);

        timerLabel = new JLabel("Expires in: 05:00", SwingConstants.CENTER);
        timerLabel.setFont(BankingTheme.FONT_SMALL_BOLD);
        timerLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);
        gbc.gridy = 2;
        inputCard.add(timerLabel, gbc);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(BankingTheme.FONT_SMALL_BOLD);
        statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
        gbc.gridy = 3;
        inputCard.add(statusLabel, gbc);

        centerPanel.add(inputCard);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        cancelButton = BankingTheme.createSecondaryButton("Cancel Transfer");
        cancelButton.addActionListener(e -> {
            stopTimer();
            dispose();
        });

        submitButton = BankingTheme.createPrimaryButton("Verify & Transfer");
        submitButton.addActionListener(e -> verifyAndTransfer());

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Enter key submits
        otpField.addActionListener(e -> verifyAndTransfer());

        // Start countdown timer
        startTimer();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopTimer();
            }
        });
    }

    private void addSummaryRow(JPanel panel, String label, String value, boolean highlight) {
        JLabel l = new JLabel(label);
        l.setFont(BankingTheme.FONT_SMALL);
        l.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(highlight ? BankingTheme.FONT_BODY_BOLD : BankingTheme.FONT_BODY);
        v.setForeground(highlight ? BankingTheme.COLOR_ACCENT_BLUE : BankingTheme.COLOR_TEXT_PRIMARY);

        panel.add(l);
        panel.add(v);
    }

    private void startTimer() {
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            int mins = remainingSeconds / 60;
            int secs = remainingSeconds % 60;
            timerLabel.setText(String.format("Expires in: %02d:%02d", mins, secs));

            if (remainingSeconds <= 0) {
                stopTimer();
                otpField.setEnabled(false);
                submitButton.setEnabled(false);
                statusLabel.setText("OTP expired. Please initiate a new transfer.");
            }
        });
        countdownTimer.start();
    }

    private void stopTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }

    private void verifyAndTransfer() {
        String enteredOtp = otpField.getText().trim();
        if (enteredOtp.length() != 6) {
            statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
            statusLabel.setText("Please enter the complete 6-digit OTP code.");
            return;
        }

        submitButton.setEnabled(false);
        cancelButton.setEnabled(false);
        submitButton.setText("Verifying & Executing...");
        statusLabel.setText(" ");

        SwingWorker<Transaction, Void> worker = new SwingWorker<>() {
            @Override
            protected Transaction doInBackground() {
                return transferService.completeTransfer(draft.getDraftId(), enteredOtp);
            }

            @Override
            protected void done() {
                try {
                    completedTransaction = get();
                    stopTimer();
                    dispose();
                } catch (Exception ex) {
                    submitButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    submitButton.setText("Verify & Transfer");

                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
                    statusLabel.setText(msg);

                    if (msg.contains("Maximum OTP attempts exceeded") || msg.contains("expired")) {
                        otpField.setEnabled(false);
                        submitButton.setEnabled(false);
                        stopTimer();
                    }
                }
            }
        };
        worker.execute();
    }

    public Transaction getCompletedTransaction() {
        return completedTransaction;
    }
}
