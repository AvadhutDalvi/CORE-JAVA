package com.banking.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Aesthetic design tokens, typography, and reusable Swing component factories
 * for a modern banking desktop interface.
 */
public class BankingTheme {

    // Color Palette
    public static final Color COLOR_PRIMARY_NAVY = new Color(15, 36, 68);     // #0F2444
    public static final Color COLOR_SECONDARY_NAVY = new Color(26, 54, 93);   // #1A365D
    public static final Color COLOR_ACCENT_BLUE = new Color(37, 99, 235);     // #2563EB
    public static final Color COLOR_ACCENT_HOVER = new Color(29, 78, 216);    // #1D4ED8
    public static final Color COLOR_SUCCESS_GREEN = new Color(16, 149, 93);   // #10955D
    public static final Color COLOR_SUCCESS_BG = new Color(236, 253, 245);    // #ECFDF5
    public static final Color COLOR_DANGER_RED = new Color(220, 38, 38);      // #DC2626
    public static final Color COLOR_DANGER_BG = new Color(254, 242, 242);     // #FEF2F2
    public static final Color COLOR_WARNING_ORANGE = new Color(217, 119, 6);  // #D97706
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);    // #F8FAFC
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);        // #E2E8F0
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);     // #0F172A
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);    // #64748B
    public static final Color COLOR_HIGHLIGHT = new Color(241, 245, 249);     // #F1F5F9

    // Typography
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_HEADER_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 17);
    public static final Font FONT_CARD_TITLE = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_CARD_METRIC = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 11);

    /**
     * Creates a styled primary action button (e.g., Transfer, Submit).
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_ACCENT_BLUE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_ACCENT_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_ACCENT_BLUE);
                }
            }
        });
        return button;
    }

    /**
     * Creates a styled secondary button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setBackground(COLOR_CARD_BG);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return button;
    }

    /**
     * Creates a standard white card container panel with subtle borders.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));
        return panel;
    }

    /**
     * Creates a styled text field with comfortable padding.
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    /**
     * Creates a styled password field with comfortable padding.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    /**
     * Creates a standardized summary metric card.
     */
    public static JPanel createMetricCard(String title, String value, Color accentColor) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(5, 5));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(FONT_SMALL_BOLD);
        titleLabel.setForeground(COLOR_TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_CARD_METRIC);
        valueLabel.setForeground(accentColor != null ? accentColor : COLOR_TEXT_PRIMARY);

        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(4, 0));
        strip.setBackground(accentColor != null ? accentColor : COLOR_ACCENT_BLUE);

        card.add(strip, BorderLayout.WEST);
        JPanel content = new JPanel(new GridLayout(2, 1, 2, 2));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 10, 0, 0));
        content.add(titleLabel);
        content.add(valueLabel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Formats currency amounts with Indian Rupee symbol.
     */
    public static String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return String.format("₹%,.2f", amount);
    }

    /**
     * Table renderer that aligns right and highlights Debits in red and Credits in green.
     */
    public static class TransactionAmountCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (!isSelected && value != null) {
                String str = value.toString();
                if (str.startsWith("-") || str.contains("DEBIT")) {
                    c.setForeground(COLOR_DANGER_RED);
                } else if (str.startsWith("+") || str.contains("CREDIT")) {
                    c.setForeground(COLOR_SUCCESS_GREEN);
                } else {
                    c.setForeground(COLOR_TEXT_PRIMARY);
                }
            }
            return c;
        }
    }
}
