package com.medicine.inventory.ui;

import com.medicine.inventory.service.InventoryService;
import com.medicine.inventory.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private final InventoryService inventoryService;
    private final DashboardPanel dashboardPanel;
    private final InventoryPanel inventoryPanel;
    private final JTabbedPane tabbedPane;
    private final JLabel statusLabel;

    public MainFrame(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.dashboardPanel = new DashboardPanel(inventoryService);
        this.inventoryPanel = new InventoryPanel(inventoryService);
        this.tabbedPane = new JTabbedPane();
        this.statusLabel = new JLabel("Ready");

        initComponents();
        setupWindowListener();
    }

    private void initComponents() {
        setTitle("Medicine Inventory Management");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        UIUtils.centerOnScreen(this);

        // Menu bar
        setJMenuBar(createMenuBar());

        // Tabbed pane
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        tabbedPane.addChangeListener(e -> onTabChanged());
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem exitItem = new JMenuItem("Exit", 'X');
        exitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exitItem.addActionListener(e -> confirmExit());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        JMenuItem dashboardItem = new JMenuItem("Dashboard", 'D');
        dashboardItem.setAccelerator(KeyStroke.getKeyStroke("control 1"));
        dashboardItem.addActionListener(e -> showDashboard());
        viewMenu.add(dashboardItem);

        JMenuItem inventoryItem = new JMenuItem("Inventory", 'I');
        inventoryItem.setAccelerator(KeyStroke.getKeyStroke("control 2"));
        inventoryItem.addActionListener(e -> showInventory());
        viewMenu.add(inventoryItem);
        menuBar.add(viewMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem aboutItem = new JMenuItem("About", 'A');
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void onTabChanged() {
        int selected = tabbedPane.getSelectedIndex();
        if (selected == 0) {
            dashboardPanel.refresh();
        } else if (selected == 1) {
            inventoryPanel.refresh();
        }
    }

    private void showDashboard() {
        tabbedPane.setSelectedIndex(0);
    }

    private void showInventory() {
        tabbedPane.setSelectedIndex(1);
    }

    private void confirmExit() {
        boolean confirm = UIUtils.confirm(this,
            "Are you sure you want to exit?",
            "Confirm Exit");
        if (confirm) {
            shutdown();
        }
    }

    private void showAboutDialog() {
        UIUtils.showInfo(this,
            "Medicine Inventory Management System\n" +
            "Version 1.0\n\n" +
            "A simple desktop application for tracking medicine inventory.\n" +
            "Built with Java 17, Swing, and SQLite.",
            "About");
    }

    public void shutdown() {
        inventoryService.shutdown();
        dispose();
        System.exit(0);
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}