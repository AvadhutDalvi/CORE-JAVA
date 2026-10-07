package org.example.gui;

import org.example.dao.DashboardDAO;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO;

    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel issuedBooksLabel;
    private JLabel totalMembersLabel;
    private JLabel overdueBooksLabel;

    public DashboardPanel() {
        this.dashboardDAO = new DashboardDAO();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 30, 30));

        createDashboard();
        loadStatistics();
    }

    private void createDashboard() {
        // Top Header with Title and Refresh Button
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Library Overview Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshButton.addActionListener(e -> loadStatistics());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Cards Panel: 2 rows, 3 columns
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));

        totalBooksLabel = createCard(
                cardsPanel,
                "Total Book Titles",
                "Unique book records registered",
                new Color(41, 128, 185) // Blue accent
        );

        availableBooksLabel = createCard(
                cardsPanel,
                "Available Copies",
                "Copies currently on shelf",
                new Color(39, 174, 96) // Green accent
        );

        issuedBooksLabel = createCard(
                cardsPanel,
                "Issued Copies",
                "Copies currently with members",
                new Color(243, 156, 18) // Orange accent
        );

        totalMembersLabel = createCard(
                cardsPanel,
                "Total Members",
                "Registered library members",
                new Color(142, 68, 173) // Purple accent
        );

        overdueBooksLabel = createCard(
                cardsPanel,
                "Overdue Books",
                "Active loans past due date",
                new Color(192, 57, 43) // Red accent
        );

        // System status / Quick Info Card for 6th slot
        createSummaryCard(cardsPanel);

        add(cardsPanel, BorderLayout.CENTER);
    }

    private JLabel createCard(JPanel panel, String title, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                )
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(44, 62, 80));

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(Color.GRAY);

        textPanel.add(titleLabel);
        textPanel.add(subLabel);

        JLabel valueLabel = new JLabel("0", SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(accentColor);

        card.add(textPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        panel.add(card);
        return valueLabel;
    }

    private void createSummaryCard(JPanel panel) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(52, 73, 94)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                )
        ));

        JLabel titleLabel = new JLabel("Quick Actions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(44, 62, 80));

        JTextArea infoText = new JTextArea(
                "Use the top navigation bar to:\n" +
                "• Manage Book Catalog & stock\n" +
                "• Register & manage Members\n" +
                "• Issue and Return books"
        );
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoText.setForeground(new Color(80, 90, 100));
        infoText.setEditable(false);
        infoText.setOpaque(false);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(infoText, BorderLayout.CENTER);

        panel.add(card);
    }

    public void loadStatistics() {
        totalBooksLabel.setText(String.valueOf(dashboardDAO.getTotalBooks()));
        availableBooksLabel.setText(String.valueOf(dashboardDAO.getAvailableBooks()));
        issuedBooksLabel.setText(String.valueOf(dashboardDAO.getIssuedBooks()));
        totalMembersLabel.setText(String.valueOf(dashboardDAO.getTotalMembers()));
        overdueBooksLabel.setText(String.valueOf(dashboardDAO.getOverdueBooks()));
    }
}