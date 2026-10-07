package org.example;


import org.example.dao.BookDAO;
import org.example.gui.MainFrame;
import org.example.model.Book;
import org.example.util.DBConnection;

import java.sql.Connection;



import java.util.List;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}