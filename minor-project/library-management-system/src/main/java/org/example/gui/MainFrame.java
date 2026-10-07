package org.example.gui;


import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;

    public MainFrame() {

        setTitle("Library Management System");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createLayout();
    }

    private void createLayout() {

        JLabel titleLabel = new JLabel(
                "Library Management System",
                SwingConstants.CENTER
        );

        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel navigationPanel = new JPanel(new FlowLayout());

        JButton dashboardButton = new JButton("Dashboard");
        JButton booksButton = new JButton("Books");
        JButton membersButton = new JButton("Members");
        JButton transactionsButton = new JButton("Transactions");

        navigationPanel.add(dashboardButton);
        navigationPanel.add(booksButton);
        navigationPanel.add(membersButton);
        navigationPanel.add(transactionsButton);

        // Content area
        contentPanel = new JPanel(new BorderLayout());

        contentPanel.add(
                new DashboardPanel(),
                BorderLayout.CENTER
        );

        // Books button action
        booksButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(new BookPanel(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        membersButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(new MemberPanel(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        transactionsButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(new TransactionPanel(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        dashboardButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(new DashboardPanel(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(navigationPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
}