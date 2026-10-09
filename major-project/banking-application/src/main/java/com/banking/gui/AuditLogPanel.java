package com.banking.gui;

import com.banking.model.AuditLog;
import com.banking.security.UserSession;
import com.banking.service.AuditService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Security audit events and access log viewer.
 */
public class AuditLogPanel extends JPanel {

    private final AuditService auditService;
    private JTable logTable;
    private DefaultTableModel tableModel;

    public AuditLogPanel() {
        this.auditService = new AuditService();
        initUI();
        loadLogs();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BankingTheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 2, 2));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Security & Audit Logs");
        titleLabel.setFont(BankingTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(BankingTheme.COLOR_PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Transparent immutable log of all authentication and financial security events");
        subLabel.setFont(BankingTheme.FONT_BODY);
        subLabel.setForeground(BankingTheme.COLOR_TEXT_MUTED);

        titleContainer.add(titleLabel);
        titleContainer.add(subLabel);

        JButton refreshBtn = BankingTheme.createSecondaryButton("Refresh Logs");
        refreshBtn.addActionListener(e -> loadLogs());

        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Table Card
        JPanel card = BankingTheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        String[] columns = {"Log ID", "Timestamp", "Security Event", "Origin / IP", "Event Description", "Outcome"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        logTable = new JTable(tableModel);
        logTable.setFont(BankingTheme.FONT_BODY);
        logTable.setRowHeight(28);
        logTable.getTableHeader().setFont(BankingTheme.FONT_BODY_BOLD);
        logTable.getTableHeader().setBackground(BankingTheme.COLOR_HIGHLIGHT);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BankingTheme.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void loadLogs() {
        if (!UserSession.isAuthenticated()) return;
        long userId = UserSession.getCurrentSession().getUserId();
        boolean isAdmin = UserSession.getCurrentSession().isAdmin();

        SwingWorker<List<AuditLog>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AuditLog> doInBackground() {
                if (isAdmin) {
                    return auditService.getRecentLogs(200);
                } else {
                    return auditService.getLogsForUser(userId);
                }
            }

            @Override
            protected void done() {
                try {
                    List<AuditLog> logs = get();
                    tableModel.setRowCount(0);
                    for (AuditLog log : logs) {
                        tableModel.addRow(new Object[]{
                                log.getLogId(),
                                log.getCreatedAt() != null ? log.getCreatedAt().toString().replace('T', ' ') : "",
                                log.getEventType(),
                                log.getSourceIp(),
                                log.getDetails(),
                                log.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading audit logs: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
