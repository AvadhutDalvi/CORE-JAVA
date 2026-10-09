package com.banking;

import com.banking.gui.LoginFrame;
import com.banking.util.DBConnection;

import javax.swing.*;

/**
 * Main application entry point for the Banking Application with Security Features.
 */
public class Main {

    public static void main(String[] args) {
        // Set native / modern system Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Notice: Could not set system look and feel: " + e.getMessage());
        }

        // Test database connectivity
        if (!DBConnection.testConnection()) {
            System.err.println("Warning: Could not connect to MySQL database at " + DBConnection.getUrl());
            System.err.println("Please ensure MySQL is running and schema.sql has been executed.");
        } else {
            System.out.println("Successfully connected to MySQL database: " + DBConnection.getUrl());
        }

        // Launch GUI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
