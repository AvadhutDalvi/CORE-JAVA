package com.banking.gui;

import com.banking.model.AccountType;
import com.banking.model.User;
import com.banking.service.AuthService;
import com.banking.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Customer Registration window.
 */
public class RegisterFrame extends JFrame {

    private final AuthService authService;
    private final JFrame parentFrame;

    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<AccountType> accountTypeCombo;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel statusLabel;

    public RegisterFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Open New Bank Account - Registration");
        setSize(520, 680);
        setMinimumSize(new Dimension(460, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BankingTheme.COLOR_BACKGROUND);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BankingTheme.COLOR_BACKGROUND);
        mainContainer.setBorder(new EmptyBorder(20, 35, 20, 35));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Join Apex Trust Bank", SwingConstants.CENTER);
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Instant Registration with ₹1,000 Welcome Balance", SwingConstants.CENTER);
        subLabel.setFont(BankingTheme.FONT_BODY);
        subLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);

        // Form Card
        JPanel card = BankingTheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;

        // Full Name
        JLabel fnLabel = new JLabel("Full Legal Name");
        fnLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(fnLabel, gbc);

        fullNameField = BankingTheme.createTextField(20);
        gbc.gridy = row++;
        card.add(fullNameField, gbc);

        // Username
        JLabel uLabel = new JLabel("Preferred Username (alphanumeric)");
        uLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(uLabel, gbc);

        usernameField = BankingTheme.createTextField(20);
        gbc.gridy = row++;
        card.add(usernameField, gbc);

        // Email
        JLabel eLabel = new JLabel("Email Address");
        eLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(eLabel, gbc);

        emailField = BankingTheme.createTextField(20);
        gbc.gridy = row++;
        card.add(emailField, gbc);

        // Phone
        JLabel pLabel = new JLabel("Phone Number (min 10 digits)");
        pLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(pLabel, gbc);

        phoneField = BankingTheme.createTextField(20);
        gbc.gridy = row++;
        card.add(phoneField, gbc);

        // Account Type
        JLabel atLabel = new JLabel("Initial Account Type");
        atLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(atLabel, gbc);

        accountTypeCombo = new JComboBox<>(AccountType.values());
        accountTypeCombo.setFont(BankingTheme.FONT_BODY);
        gbc.gridy = row++;
        card.add(accountTypeCombo, gbc);

        // Password
        JLabel passLabel = new JLabel("Password (min 8 characters)");
        passLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(passLabel, gbc);

        passwordField = BankingTheme.createPasswordField(20);
        gbc.gridy = row++;
        card.add(passwordField, gbc);

        // Confirm Password
        JLabel cpassLabel = new JLabel("Confirm Password");
        cpassLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        gbc.gridy = row++;
        card.add(cpassLabel, gbc);

        confirmPasswordField = BankingTheme.createPasswordField(20);
        gbc.gridy = row++;
        card.add(confirmPasswordField, gbc);

        // Status Label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(BankingTheme.FONT_SMALL_BOLD);
        statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
        gbc.gridy = row++;
        card.add(statusLabel, gbc);

        // Register Button
        registerButton = BankingTheme.createPrimaryButton("Create Banking Profile");
        registerButton.setPreferredSize(new Dimension(0, 40));
        registerButton.addActionListener(e -> performRegister());
        gbc.gridy = row++;
        card.add(registerButton, gbc);

        // Footer / Back to Login
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        footerPanel.setOpaque(false);

        JLabel loginHint = new JLabel("Already have an account?");
        loginHint.setFont(BankingTheme.FONT_BODY);
        loginHint.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        backToLoginButton = new JButton("Sign In Here");
        backToLoginButton.setFont(BankingTheme.FONT_BODY_BOLD);
        backToLoginButton.setForeground(BankingTheme.COLOR_ACCENT_BLUE);
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.addActionListener(e -> {
            dispose();
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            } else {
                new LoginFrame().setVisible(true);
            }
        });

        footerPanel.add(loginHint);
        footerPanel.add(backToLoginButton);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(scrollPane, BorderLayout.CENTER);
        mainContainer.add(footerPanel, BorderLayout.SOUTH);

        add(mainContainer);
    }

    private void performRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        AccountType accountType = (AccountType) accountTypeCombo.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        registerButton.setEnabled(false);
        registerButton.setText("Creating Account...");
        statusLabel.setText(" ");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.register(username, fullName, email, phone, password, confirmPassword, accountType);
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                registerButton.setText("Create Banking Profile");
                try {
                    User newUser = get();
                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Registration successful!\nWelcome to Apex Trust Bank, " + newUser.getFullName() +
                            ".\nA primary bank account has been created with ₹1,000.00 welcome balance.\nPlease sign in.",
                            "Account Created",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose();
                    if (parentFrame != null) {
                        parentFrame.setVisible(true);
                    } else {
                        new LoginFrame().setVisible(true);
                    }
                } catch (Exception ex) {
                    String message = ex.getCause() instanceof ValidationException ?
                            ex.getCause().getMessage() : "Registration failed: " + ex.getMessage();
                    statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
                    statusLabel.setText(message);
                }
            }
        };
        worker.execute();
    }
}
