package com.medicine.inventory.ui;

import com.medicine.inventory.model.Medicine;
import com.medicine.inventory.service.DashboardSummary;
import com.medicine.inventory.service.InventoryService;
import com.medicine.inventory.util.DateUtils;
import com.medicine.inventory.util.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final InventoryService inventoryService;

    private JLabel totalLabel;
    private JLabel expiredLabel;
    private JLabel expiringSoonLabel;
    private JLabel lowStockLabel;
    private DefaultListModel<String> attentionListModel;
    private JList<String> attentionList;
    private JLabel statusLabel;

    public DashboardPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4; gbc.weightx = 1.0;
        add(titleLabel, gbc);

        // Summary cards row
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        add(createSummaryCard("Total Medicines", "0", new Color(70, 130, 180)), gbc);
        gbc.gridx = 1;
        add(createSummaryCard("Expired", "0", new Color(200, 60, 60)), gbc);
        gbc.gridx = 2;
        add(createSummaryCard("Expiring Soon", "0", new Color(220, 160, 50)), gbc);
        gbc.gridx = 3;
        add(createSummaryCard("Low Stock", "0", new Color(200, 60, 60)), gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 10, 0);
        add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        // Attention section
        gbc.gridy = 3; gbc.gridwidth = 4; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel attentionTitle = new JLabel("Medicines Needing Attention");
        attentionTitle.setFont(attentionTitle.getFont().deriveFont(Font.BOLD, 16f));
        add(attentionTitle, gbc);

        gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        attentionListModel = new DefaultListModel<>();
        attentionList = new JList<>(attentionListModel);
        attentionList.setCellRenderer(new AttentionCellRenderer());
        attentionList.setVisibleRowCount(8);
        attentionList.setFixedCellHeight(36);
        JScrollPane scrollPane = new JScrollPane(attentionList);
        scrollPane.setPreferredSize(new Dimension(0, 250));
        add(scrollPane, gbc);

        // Refresh button and status
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; gbc.anchor = GridBagConstraints.EAST;
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(this::onRefresh);
        statusLabel = new JLabel("Ready");
        bottomPanel.add(refreshBtn, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.EAST);
        add(bottomPanel, gbc);
    }

    private JPanel createSummaryCard(String title, String initialValue, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(new CompoundBorder(
            new LineBorder(accentColor.darker(), 2),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(accentColor.darker());

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 36f));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        // Store reference to value label
        switch (title) {
            case "Total Medicines" -> totalLabel = valueLabel;
            case "Expired" -> expiredLabel = valueLabel;
            case "Expiring Soon" -> expiringSoonLabel = valueLabel;
            case "Low Stock" -> lowStockLabel = valueLabel;
        }

        return card;
    }

    private void onRefresh(ActionEvent e) {
        refresh();
    }

    public void refresh() {
        SwingWorker<DashboardSummary, Void> worker = new SwingWorker<>() {
            @Override protected DashboardSummary doInBackground() throws Exception {
                return inventoryService.getDashboardSummary();
            }
            @Override protected void done() {
                try {
                    DashboardSummary summary = get();
                    updateSummary(summary);
                    UIUtils.setStatus(statusLabel, "Dashboard refreshed");
                } catch (Exception ex) {
                    UIUtils.showDatabaseError(DashboardPanel.this, "loading dashboard", ex);
                }
            }
        };
        worker.execute();
    }

    private void updateSummary(DashboardSummary summary) {
        totalLabel.setText(String.valueOf(summary.getTotalMedicines()));
        expiredLabel.setText(String.valueOf(summary.getExpiredCount()));
        expiringSoonLabel.setText(String.valueOf(summary.getExpiringSoonCount()));
        lowStockLabel.setText(String.valueOf(summary.getLowStockCount()));

        attentionListModel.clear();
        List<Medicine> attention = summary.getMedicinesNeedingAttention();
        if (attention.isEmpty()) {
            attentionListModel.addElement("✓ All medicines are in good status");
        } else {
            for (Medicine m : attention) {
                attentionListModel.addElement(formatAttentionItem(m));
            }
        }
    }

    private String formatAttentionItem(Medicine m) {
        StringBuilder sb = new StringBuilder();
        String expiryStatus = m.getExpiryStatus();
        String stockStatus = m.getStockStatus();

        if ("Expired".equals(expiryStatus)) {
            sb.append("🔴 EXPIRED");
        } else if ("Expiring Soon".equals(expiryStatus)) {
            long days = ChronoUnit.DAYS.between(java.time.LocalDate.now(), m.getExpiryDate());
            sb.append("🟡 EXPIRING IN ").append(days).append(" DAYS");
        }

        if ("Low Stock".equals(stockStatus)) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("🔴 LOW STOCK (").append(m.getQuantity()).append(")");
        }

        return m.getName() + "  —  " + sb;
    }

    private static class AttentionCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            if (!isSelected) {
                if (text.contains("EXPIRED") && text.contains("LOW STOCK")) {
                    setBackground(new Color(255, 220, 220));
                } else if (text.contains("EXPIRED")) {
                    setBackground(new Color(255, 230, 230));
                } else if (text.contains("EXPIRING")) {
                    setBackground(new Color(255, 248, 220));
                } else if (text.contains("LOW STOCK")) {
                    setBackground(new Color(255, 235, 235));
                } else {
                    setBackground(new Color(230, 255, 230));
                }
            }
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return c;
        }
    }
}