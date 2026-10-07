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

        dashboardDAO = new DashboardDAO();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        createDashboard();

        loadStatistics();
    }

    private void createDashboard() {

        JLabel titleLabel = new JLabel(
                "Library Dashboard",
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 26)
        );

        add(titleLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(
                new GridLayout(2, 3, 20, 20)
        );

        totalBooksLabel = createCard(
                cardsPanel,
                "Total Books"
        );

        availableBooksLabel = createCard(
                cardsPanel,
                "Available Books"
        );

        issuedBooksLabel = createCard(
                cardsPanel,
                "Issued Books"
        );

        totalMembersLabel = createCard(
                cardsPanel,
                "Total Members"
        );

        overdueBooksLabel = createCard(
                cardsPanel,
                "Overdue Books"
        );

        add(cardsPanel, BorderLayout.CENTER);
    }

    private JLabel createCard(
            JPanel panel,
            String title
    ) {

        JPanel card = new JPanel(
                new BorderLayout(10, 10)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(
                                20, 20, 20, 20
                        )
                )
        );

        JLabel titleLabel = new JLabel(
                title,
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        JLabel valueLabel = new JLabel(
                "0",
                SwingConstants.CENTER
        );

        valueLabel.setFont(
                new Font("Arial", Font.BOLD, 32)
        );

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        panel.add(card);

        return valueLabel;
    }

    private void loadStatistics() {

        totalBooksLabel.setText(
                String.valueOf(
                        dashboardDAO.getTotalBooks()
                )
        );

        availableBooksLabel.setText(
                String.valueOf(
                        dashboardDAO.getAvailableBooks()
                )
        );

        issuedBooksLabel.setText(
                String.valueOf(
                        dashboardDAO.getIssuedBooks()
                )
        );

        totalMembersLabel.setText(
                String.valueOf(
                        dashboardDAO.getTotalMembers()
                )
        );

        overdueBooksLabel.setText(
                String.valueOf(
                        dashboardDAO.getOverdueBooks()
                )
        );
    }
}