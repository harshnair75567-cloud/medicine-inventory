package com.medicine.inventory;

import com.medicine.inventory.dao.DatabaseManager;
import com.medicine.inventory.dao.MedicineDAO;
import com.medicine.inventory.service.InventoryService;
import com.medicine.inventory.ui.MainFrame;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.warning("Could not set system look and feel: " + e.getMessage());
        }

        // Start on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                startup();
            } catch (Exception e) {
                LOGGER.severe("Failed to start application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to start application:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static void startup() throws Exception {
        LOGGER.info("Starting Medicine Inventory Management System...");

        // Initialize database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.initialize();

        // Create DAO and Service
        MedicineDAO medicineDAO = new MedicineDAO(dbManager);
        InventoryService inventoryService = new InventoryService(medicineDAO);

        // Create and show main frame
        MainFrame mainFrame = new MainFrame(inventoryService);
        mainFrame.setVisible(true);

        LOGGER.info("Application started successfully");
    }
}
