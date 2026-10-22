package com.medicine.inventory.ui;

import com.medicine.inventory.model.Medicine;
import com.medicine.inventory.service.InventoryService;
import com.medicine.inventory.ui.renderers.ExpiryStatusRenderer;
import com.medicine.inventory.ui.renderers.StockStatusRenderer;
import com.medicine.inventory.util.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService;
    private final MedicineTableModel tableModel;
    private final JTable medicineTable;
    private final TableRowSorter<MedicineTableModel> sorter;

    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> expiryFilter;
    private JComboBox<String> stockFilter;
    private JLabel recordCountLabel;
    private JLabel statusLabel;

    private Timer searchDebounceTimer;

    public InventoryPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.tableModel = new MedicineTableModel(inventoryService);
        this.medicineTable = new JTable(tableModel);
        this.sorter = new TableRowSorter<>(tableModel);
        initComponents();
        setupTable();
        setupFilters();
        refresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JLabel("Search: "));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by medicine name");
        toolbar.add(searchField);

        toolbar.addSeparator();
        toolbar.add(new JLabel("Category: "));
        categoryFilter = new JComboBox<>(new String[]{"All"});
        toolbar.add(categoryFilter);

        toolbar.addSeparator();
        toolbar.add(new JLabel("Expiry: "));
        expiryFilter = new JComboBox<>(new String[]{"All", "Expired", "Expiring Soon", "Valid"});
        toolbar.add(expiryFilter);

        toolbar.addSeparator();
        toolbar.add(new JLabel("Stock: "));
        stockFilter = new JComboBox<>(new String[]{"All", "Low Stock", "In Stock"});
        toolbar.add(stockFilter);

        toolbar.addSeparator();
        JButton clearBtn = new JButton("Clear Filters");
        clearBtn.addActionListener(this::onClearFilters);
        toolbar.add(clearBtn);

        toolbar.add(Box.createHorizontalGlue());
        JButton addBtn = new JButton("Add Medicine");
        addBtn.addActionListener(this::onAddMedicine);
        toolbar.add(addBtn);

        add(toolbar, BorderLayout.NORTH);

        // Table
        JScrollPane scrollPane = new JScrollPane(medicineTable);
        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        recordCountLabel = new JLabel(" ");
        statusLabel = new JLabel("Ready");
        statusPanel.add(recordCountLabel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupTable() {
        medicineTable.setRowSorter(sorter);
        medicineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medicineTable.setAutoCreateRowSorter(true);
        medicineTable.getTableHeader().setReorderingAllowed(false);

        // Custom renderers for status columns
        medicineTable.getColumnModel().getColumn(4).setCellRenderer(new ExpiryStatusRenderer());
        medicineTable.getColumnModel().getColumn(5).setCellRenderer(new StockStatusRenderer());

        // Column widths
        medicineTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
        medicineTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Category
        medicineTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Quantity
        medicineTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Expiry Date
        medicineTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Expiry Status
        medicineTable.getColumnModel().getColumn(5).setPreferredWidth(110); // Stock Status

        // Double-click to edit
        medicineTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelected();
                }
            }
        });

        // Keyboard shortcuts
        InputMap im = medicineTable.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = medicineTable.getActionMap();
        im.put(KeyStroke.getKeyStroke("DELETE"), "deleteMedicine");
        am.put("deleteMedicine", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onDeleteSelected(); }
        });
        im.put(KeyStroke.getKeyStroke("ENTER"), "editMedicine");
        am.put("editMedicine", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onEditSelected(); }
        });
    }

    private void setupFilters() {
        // Search with debounce
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { scheduleRefresh(); }
            @Override public void removeUpdate(DocumentEvent e) { scheduleRefresh(); }
            @Override public void changedUpdate(DocumentEvent e) { scheduleRefresh(); }
        });

        categoryFilter.addActionListener(e -> refresh());
        expiryFilter.addActionListener(e -> refresh());
        stockFilter.addActionListener(e -> refresh());

        loadCategories();
    }

    private void scheduleRefresh() {
        if (searchDebounceTimer != null) {
            searchDebounceTimer.cancel();
        }
        searchDebounceTimer = new Timer();
        searchDebounceTimer.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(InventoryPanel.this::refresh);
            }
        }, 300);
    }

    private void loadCategories() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override protected List<String> doInBackground() throws Exception {
                return inventoryService.getCategories();
            }
            @Override protected void done() {
                try {
                    List<String> cats = get();
                    String current = (String) categoryFilter.getSelectedItem();
                    categoryFilter.removeAllItems();
                    categoryFilter.addItem("All");
                    for (String c : cats) categoryFilter.addItem(c);
                    if (current != null) categoryFilter.setSelectedItem(current);
                } catch (Exception ex) {
                    UIUtils.showDatabaseError(InventoryPanel.this, "loading categories", ex);
                }
            }
        };
        worker.execute();
    }

    public void refresh() {
        SwingWorker<List<Medicine>, Void> worker = new SwingWorker<>() {
            @Override protected List<Medicine> doInBackground() throws Exception {
                String search = searchField.getText().trim();
                String cat = (String) categoryFilter.getSelectedItem();
                String exp = (String) expiryFilter.getSelectedItem();
                String stock = (String) stockFilter.getSelectedItem();
                return inventoryService.getMedicinesWithFilters(
                    search.isEmpty() ? null : search,
                    cat == null || cat.equals("All") ? null : cat,
                    exp == null || exp.equals("All") ? null : exp,
                    stock == null || stock.equals("All") ? null : stock
                );
            }
            @Override protected void done() {
                try {
                    List<Medicine> meds = get();
                    tableModel.setMedicines(meds);
                    updateRecordCount(meds.size());
                    UIUtils.setStatus(statusLabel, "Refreshed");
                } catch (Exception ex) {
                    UIUtils.showDatabaseError(InventoryPanel.this, "refreshing inventory", ex);
                }
            }
        };
        worker.execute();
    }

    private void updateRecordCount(int count) {
        recordCountLabel.setText("Showing " + count + " medicine" + (count != 1 ? "s" : ""));
    }

    private void onAddMedicine(ActionEvent e) {
        MedicineFormDialog dialog = new MedicineFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), inventoryService, null);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            refresh();
            loadCategories();
            UIUtils.setStatus(statusLabel, "Medicine added");
        }
    }

    private void onEditSelected() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow == -1) {
            UIUtils.showWarning(this, "Select a medicine to edit", "No Selection");
            return;
        }
        int modelRow = medicineTable.convertRowIndexToModel(selectedRow);
        Medicine medicine = tableModel.getMedicineAt(modelRow);
        if (medicine == null) return;

        MedicineFormDialog dialog = new MedicineFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), inventoryService, medicine);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            refresh();
            loadCategories();
            UIUtils.setStatus(statusLabel, "Medicine updated");
        }
    }

    private void onDeleteSelected() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow == -1) {
            UIUtils.showWarning(this, "Select a medicine to delete", "No Selection");
            return;
        }
        int modelRow = medicineTable.convertRowIndexToModel(selectedRow);
        Medicine medicine = tableModel.getMedicineAt(modelRow);
        if (medicine == null) return;

        boolean confirm = UIUtils.confirm(this,
            "Delete '" + medicine.getName() + "'?\nThis cannot be undone.",
            "Confirm Delete");
        if (confirm) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() throws Exception {
                    inventoryService.deleteMedicine(medicine.getId());
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        refresh();
                        loadCategories();
                        UIUtils.setStatus(statusLabel, "Medicine deleted");
                    } catch (Exception ex) {
                        UIUtils.showDatabaseError(InventoryPanel.this, "deleting medicine", ex);
                    }
                }
            };
            worker.execute();
        }
    }

    private void onClearFilters(ActionEvent e) {
        searchField.setText("");
        categoryFilter.setSelectedIndex(0);
        expiryFilter.setSelectedIndex(0);
        stockFilter.setSelectedIndex(0);
        refresh();
    }
}