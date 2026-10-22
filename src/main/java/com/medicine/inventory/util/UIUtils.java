package com.medicine.inventory.util;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

public class UIUtils {
    private static final Logger LOGGER = Logger.getLogger(UIUtils.class.getName());

    public static void showValidationErrors(Component parent, List<String> errors) {
        String message = String.join("\n", errors);
        JOptionPane.showMessageDialog(parent, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showDatabaseError(Component parent, String operation, Exception e) {
        LOGGER.severe(operation + " failed: " + e.getMessage());
        JOptionPane.showMessageDialog(parent,
            "Database error during " + operation + ":\n" + e.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static void setStatus(JLabel statusLabel, String message) {
        statusLabel.setText(message);
        if (message != null && !message.isEmpty()) {
            Timer timer = new Timer(5000, e -> statusLabel.setText("Ready"));
            timer.setRepeats(false);
            timer.start();
        }
    }

    public static void centerOnScreen(Window window) {
        window.setLocationRelativeTo(null);
    }
}