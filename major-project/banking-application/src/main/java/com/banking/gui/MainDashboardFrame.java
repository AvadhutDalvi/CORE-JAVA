package com.banking.gui;

import com.banking.model.User;
import com.banking.security.UserSession;
import com.banking.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Main application window after successful authentication.
 * Hosts navigation and layered views: Dashboard, Accounts, Transfer, History, Audit, Admin.
 */
public class MainDashboardFrame extends JFrame {

    private final User currentUser;
    private final AuthService authService;

    private JPanel contentArea;
    private JButton dashboardNavBtn;
    private JButton accountsNavBtn;
    private JButton transferNavBtn;
    private JButton historyNavBtn;
    private JButton auditNavBtn;
    private JButton adminNavBtn;

    private DashboardPanel dashboardPanel;
    private AccountPanel accountPanel;
    private TransferPanel transferPanel;
    private TransactionHistoryPanel historyPanel;
    private AuditLogPanel auditLogPanel;
    private AdminPanel adminPanel;

    public MainDashboardFrame(User user) {
        this.currentUser = user;
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Apex Trust Bank - Digital Banking Portal");
        setSize(1150, 760);
        setMinimumSize(new Dimension(960, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BankingTheme.COLOR_BACKGROUND);

        // Top Header
        JPanel topHeader = createHeaderPanel();

        // Main Layout
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BankingTheme.COLOR_BACKGROUND);

        // Instantiate view panels
        dashboardPanel = new DashboardPanel(this);
        accountPanel = new AccountPanel(this);
        transferPanel = new TransferPanel(this);
        historyPanel = new TransactionHistoryPanel(this);
        auditLogPanel = new AuditLogPanel();
        if (currentUser.isAdmin()) {
            adminPanel = new AdminPanel(this);
        }

        // Default to Dashboard
        contentArea.add(dashboardPanel, BorderLayout.CENTER);
        setActiveNavButton(dashboardNavBtn);

        setLayout(new BorderLayout());
        add(topHeader, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BankingTheme.COLOR_PRIMARY_NAVY);

        // Title and User Profile
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(new EmptyBorder(14, 25, 10, 25));

        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("APEX TRUST BANK");
        brandLabel.setFont(new Font(BankingTheme.FONT_FAMILY, Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        JLabel envBadge = new JLabel("SECURE PORTAL");
        envBadge.setFont(BankingTheme.FONT_SMALL_BOLD);
        envBadge.setForeground(new Color(147, 197, 253));
        envBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));

        brandPanel.add(brandLabel);
        brandPanel.add(envBadge);

        // User & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        String roleTag = currentUser.isAdmin() ? "[ADMIN]" : "[CUSTOMER]";
        JLabel userLabel = new JLabel("Signed in: " + currentUser.getFullName() + " " + roleTag);
        userLabel.setFont(BankingTheme.FONT_BODY);
        userLabel.setForeground(new Color(226, 232, 240));

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setFont(BankingTheme.FONT_SMALL);
        dateLabel.setForeground(new Color(148, 163, 184));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(BankingTheme.FONT_SMALL_BOLD);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(BankingTheme.COLOR_DANGER_RED);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> performLogout());

        userPanel.add(userLabel);
        userPanel.add(dateLabel);
        userPanel.add(logoutBtn);

        topRow.add(brandPanel, BorderLayout.WEST);
        topRow.add(userPanel, BorderLayout.EAST);

        // Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        navBar.setBackground(BankingTheme.COLOR_SECONDARY_NAVY);
        navBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        dashboardNavBtn = createNavButton("Overview");
        accountsNavBtn = createNavButton("My Accounts");
        transferNavBtn = createNavButton("Fund Transfer");
        historyNavBtn = createNavButton("Statement & Ledger");
        auditNavBtn = createNavButton("Security Logs");

        dashboardNavBtn.addActionListener(e -> navigateToDashboard());
        accountsNavBtn.addActionListener(e -> navigateToAccounts());
        transferNavBtn.addActionListener(e -> navigateToTransfer());
        historyNavBtn.addActionListener(e -> navigateToHistory());
        auditNavBtn.addActionListener(e -> navigateToAudit());

        navBar.add(dashboardNavBtn);
        navBar.add(accountsNavBtn);
        navBar.add(transferNavBtn);
        navBar.add(historyNavBtn);
        navBar.add(auditNavBtn);

        if (currentUser.isAdmin()) {
            adminNavBtn = createNavButton("Administration");
            adminNavBtn.addActionListener(e -> navigateToAdmin());
            navBar.add(adminNavBtn);
        }

        header.add(topRow, BorderLayout.NORTH);
        header.add(navBar, BorderLayout.SOUTH);
        return header;
    }

    private JButton createNavButton(String title) {
        JButton btn = new JButton(title);
        btn.setFont(BankingTheme.FONT_BODY_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(BankingTheme.COLOR_SECONDARY_NAVY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(145, 34));
        return btn;
    }

    private void setActiveNavButton(JButton active) {
        JButton[] buttons = {dashboardNavBtn, accountsNavBtn, transferNavBtn, historyNavBtn, auditNavBtn, adminNavBtn};
        for (JButton b : buttons) {
            if (b == null) continue;
            if (b == active) {
                b.setBackground(BankingTheme.COLOR_ACCENT_BLUE);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(BankingTheme.COLOR_SECONDARY_NAVY);
                b.setForeground(new Color(203, 213, 225));
            }
        }
    }

    private void switchView(JPanel panel, JButton activeNavBtn) {
        setActiveNavButton(activeNavBtn);
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    public void navigateToDashboard() {
        dashboardPanel.loadDashboardData();
        switchView(dashboardPanel, dashboardNavBtn);
    }

    public void navigateToAccounts() {
        accountPanel.loadAccounts();
        switchView(accountPanel, accountsNavBtn);
    }

    public void navigateToTransfer() {
        transferPanel.loadSourceAccounts();
        switchView(transferPanel, transferNavBtn);
    }

    public void navigateToHistory() {
        historyPanel.loadHistory();
        switchView(historyPanel, historyNavBtn);
    }

    public void navigateToAudit() {
        auditLogPanel.loadLogs();
        switchView(auditLogPanel, auditNavBtn);
    }

    public void navigateToAdmin() {
        if (adminPanel != null) {
            adminPanel.loadAllAccounts();
            switchView(adminPanel, adminNavBtn);
        }
    }

    public void refreshAllViews() {
        if (dashboardPanel != null) dashboardPanel.loadDashboardData();
        if (accountPanel != null) accountPanel.loadAccounts();
        if (transferPanel != null) transferPanel.loadSourceAccounts();
        if (historyPanel != null) historyPanel.loadHistory();
        if (auditLogPanel != null) auditLogPanel.loadLogs();
        if (adminPanel != null) adminPanel.loadAllAccounts();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out of your banking session?",
                "Sign Out",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
