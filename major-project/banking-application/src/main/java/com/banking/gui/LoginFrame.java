package com.banking.gui;

import com.banking.model.User;
import com.banking.service.AuthService;
import com.banking.util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Authentication login window.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;

    private JTextField identifierField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JButton loginButton;
    private JButton openRegisterButton;
    private JLabel statusLabel;

    public LoginFrame() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Secure Banking Portal - Login");
        setSize(480, 560);
        setMinimumSize(new Dimension(420, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BankingTheme.COLOR_BACKGROUND);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BankingTheme.COLOR_BACKGROUND);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel titleLabel = new JLabel("Apex Trust Bank", SwingConstants.CENTER);
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Online Banking Portal with Multi-Factor Security", SwingConstants.CENTER);
        subLabel.setFont(BankingTheme.FONT_BODY);
        subLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);

        // Form Card
        JPanel card = BankingTheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;

        // Username / Email
        JLabel idLabel = new JLabel("Username or Email");
        idLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        idLabel.setForeground(BankingTheme.COLOR_TEXT_PRIMARY);
        gbc.gridy = row++;
        card.add(idLabel, gbc);

        identifierField = BankingTheme.createTextField(20);
        identifierField.setToolTipText("Enter your registered username or email");
        gbc.gridy = row++;
        card.add(identifierField, gbc);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(BankingTheme.FONT_BODY_BOLD);
        passLabel.setForeground(BankingTheme.COLOR_TEXT_PRIMARY);
        gbc.gridy = row++;
        card.add(passLabel, gbc);

        passwordField = BankingTheme.createPasswordField(20);
        gbc.gridy = row++;
        card.add(passwordField, gbc);

        // Show password checkbox
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(BankingTheme.FONT_SMALL);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        gbc.gridy = row++;
        card.add(showPasswordCheck, gbc);

        // Status / Error message
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(BankingTheme.FONT_SMALL_BOLD);
        statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
        gbc.gridy = row++;
        card.add(statusLabel, gbc);

        // Login Button
        loginButton = BankingTheme.createPrimaryButton("Secure Sign In");
        loginButton.setPreferredSize(new Dimension(0, 42));
        gbc.gridy = row++;
        card.add(loginButton, gbc);

        // Enter key action
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        identifierField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
        loginButton.addActionListener(e -> performLogin());

        // Footer / Registration Link
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        footerPanel.setOpaque(false);

        JLabel registerHint = new JLabel("New customer?");
        registerHint.setFont(BankingTheme.FONT_BODY);
        registerHint.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        openRegisterButton = new JButton("Open an Account");
        openRegisterButton.setFont(BankingTheme.FONT_BODY_BOLD);
        openRegisterButton.setForeground(BankingTheme.COLOR_ACCENT_BLUE);
        openRegisterButton.setBorderPainted(false);
        openRegisterButton.setContentAreaFilled(false);
        openRegisterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openRegisterButton.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });

        footerPanel.add(registerHint);
        footerPanel.add(openRegisterButton);

        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(card, BorderLayout.CENTER);
        mainContainer.add(footerPanel, BorderLayout.SOUTH);

        add(mainContainer);
    }

    private void performLogin() {
        String identifier = identifierField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
            statusLabel.setText("Please enter both username/email and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Verifying Credentials...");
        statusLabel.setText(" ");

        // Non-blocking SwingWorker for authentication
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.login(identifier, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Secure Sign In");
                try {
                    User user = get();
                    // Transition to Dashboard
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainDashboardFrame(user).setVisible(true));
                } catch (Exception ex) {
                    String message = ex.getCause() instanceof ValidationException ?
                            ex.getCause().getMessage() : "Authentication failed. Please verify your credentials.";
                    statusLabel.setForeground(BankingTheme.COLOR_DANGER_RED);
                    statusLabel.setText(message);
                }
            }
        };
        worker.execute();
    }
}
