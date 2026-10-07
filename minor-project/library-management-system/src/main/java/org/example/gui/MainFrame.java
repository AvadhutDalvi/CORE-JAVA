package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private JButton dashboardButton;
    private JButton booksButton;
    private JButton membersButton;
    private JButton transactionsButton;

    public MainFrame() {
        setTitle("Library Management System");
        setSize(1050, 700);
        setMinimumSize(new Dimension(880, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createLayout();
    }

    private void createLayout() {
        // Top Header
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(44, 62, 80)); // Dark modern header

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Minor Project — Core Java, Swing & JDBC");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(189, 195, 199));

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Navigation Bar
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        navPanel.setBackground(new Color(52, 73, 94));

        dashboardButton = createNavButton("Dashboard");
        booksButton = createNavButton("Books");
        membersButton = createNavButton("Members");
        transactionsButton = createNavButton("Transactions");

        navPanel.add(dashboardButton);
        navPanel.add(booksButton);
        navPanel.add(membersButton);
        navPanel.add(transactionsButton);

        topContainer.add(titlePanel, BorderLayout.NORTH);
        topContainer.add(navPanel, BorderLayout.SOUTH);

        // Content Area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new DashboardPanel(), BorderLayout.CENTER);
        setActiveButton(dashboardButton);

        // Button Listeners
        dashboardButton.addActionListener(e -> {
            setActiveButton(dashboardButton);
            switchView(new DashboardPanel());
        });

        booksButton.addActionListener(e -> {
            setActiveButton(booksButton);
            switchView(new BookPanel());
        });

        membersButton.addActionListener(e -> {
            setActiveButton(membersButton);
            switchView(new MemberPanel());
        });

        transactionsButton.addActionListener(e -> {
            setActiveButton(transactionsButton);
            switchView(new TransactionPanel());
        });

        add(topContainer, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setForeground(new Color(236, 240, 241));
        button.setBackground(new Color(41, 128, 185));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 32));
        return button;
    }

    private void setActiveButton(JButton activeBtn) {
        JButton[] buttons = {dashboardButton, booksButton, membersButton, transactionsButton};
        for (JButton btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackground(new Color(26, 188, 156)); // Turquoise active accent
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(41, 128, 185)); // Default blue
                btn.setForeground(new Color(236, 240, 241));
            }
        }
    }

    private void switchView(JPanel newView) {
        contentPanel.removeAll();
        contentPanel.add(newView, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}