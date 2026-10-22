package com.medicine.inventory.ui;

import com.medicine.inventory.model.Medicine;
import com.medicine.inventory.service.InventoryService;
import com.medicine.inventory.util.DateUtils;
import com.medicine.inventory.util.UIUtils;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MedicineFormDialog extends JDialog {
    private static final String[] CATEGORIES = {
        "Tablet", "Syrup", "Capsule", "Injection", "Cream", "Drops", "First Aid", "Other"
    };
    private static final int DEFAULT_THRESHOLD = 5;

    private final InventoryService inventoryService;
    private Medicine medicine;
    private Medicine result;
    private boolean isEditMode;

    private JTextField nameField;
    private JComboBox<String> categoryCombo;
    private JSpinner quantitySpinner;
    private JFormattedTextField purchaseDateField;
    private JFormattedTextField expiryDateField;
    private JSpinner thresholdSpinner;
    private JTextArea notesArea;

    public MedicineFormDialog(Frame parent, InventoryService inventoryService, Medicine medicineToEdit) {
        super(parent, medicineToEdit == null ? "Add Medicine" : "Edit Medicine", true);
        this.inventoryService = inventoryService;
        this.medicine = medicineToEdit;
        this.isEditMode = medicineToEdit != null;
        initComponents();
        if (isEditMode) populateFields();
        pack();
        setMinimumSize(new Dimension(450, 500));
        UIUtils.centerOnScreen(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name (required)
        addField(formPanel, gbc, 0, "Name *:", createNameField());

        // Category
        addField(formPanel, gbc, 1, "Category:", createCategoryCombo());

        // Quantity
        addField(formPanel, gbc, 2, "Quantity:", createQuantitySpinner());

        // Purchase Date
        addField(formPanel, gbc, 3, "Purchase Date:", createDateField("Purchase Date (YYYY-MM-DD)"));

        // Expiry Date (required)
        addField(formPanel, gbc, 4, "Expiry Date *:", createDateField("Expiry Date (YYYY-MM-DD)"));

        // Low Stock Threshold
        addField(formPanel, gbc, 5, "Low Stock Threshold:", createThresholdSpinner());

        // Notes
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(notesArea), gbc);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private JTextField createNameField() {
        nameField = new JTextField(20);
        return nameField;
    }

    private JComboBox<String> createCategoryCombo() {
        categoryCombo = new JComboBox<>(CATEGORIES);
        categoryCombo.setEditable(true);
        return categoryCombo;
    }

    private JSpinner createQuantitySpinner() {
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        return quantitySpinner;
    }

    private JFormattedTextField createDateField(String tooltip) {
        JFormattedTextField field;
        try {
            MaskFormatter formatter = new MaskFormatter("####-##-##");
            formatter.setPlaceholderCharacter('_');
            field = new JFormattedTextField(formatter);
        } catch (ParseException e) {
            field = new JFormattedTextField();
        }
        field.setColumns(12);
        field.setToolTipText(tooltip);
        if (tooltip.contains("Purchase")) {
            purchaseDateField = field;
        } else {
            expiryDateField = field;
        }
        return field;
    }

    private JSpinner createThresholdSpinner() {
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_THRESHOLD, 1, 100, 1));
        return thresholdSpinner;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(this::onSave);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);
        panel.add(saveBtn);
        panel.add(cancelBtn);
        return panel;
    }

    private void populateFields() {
        nameField.setText(medicine.getName());
        if (medicine.getCategory() != null) {
            categoryCombo.setSelectedItem(medicine.getCategory());
        }
        quantitySpinner.setValue(medicine.getQuantity());
        purchaseDateField.setText(DateUtils.format(medicine.getPurchaseDate()));
        expiryDateField.setText(DateUtils.format(medicine.getExpiryDate()));
        thresholdSpinner.setValue(medicine.getLowStockThreshold());
        notesArea.setText(medicine.getNotes() != null ? medicine.getNotes() : "");
    }

    private void onSave(ActionEvent e) {
        List<String> errors = validateInput();
        if (!errors.isEmpty()) {
            UIUtils.showValidationErrors(this, errors);
            return;
        }

        Medicine m = isEditMode ? medicine : new Medicine();
        m.setName(nameField.getText().trim());
        m.setCategory((String) categoryCombo.getSelectedItem());
        m.setQuantity((Integer) quantitySpinner.getValue());
        m.setPurchaseDate(DateUtils.parse(purchaseDateField.getText()));
        m.setExpiryDate(DateUtils.parse(expiryDateField.getText()));
        m.setLowStockThreshold((Integer) thresholdSpinner.getValue());
        m.setNotes(notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim());

        try {
            if (isEditMode) {
                inventoryService.updateMedicine(m);
            } else {
                inventoryService.addMedicine(m);
            }
            result = m;
            dispose();
        } catch (Exception ex) {
            UIUtils.showDatabaseError(this, isEditMode ? "updating" : "adding", ex);
        }
    }

    private List<String> validateInput() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errors.add("Medicine name is required");
        }

        String expiryText = expiryDateField.getText().replace('_', ' ').trim();
        LocalDate expiryDate = DateUtils.parse(expiryText);
        if (expiryDate == null) {
            errors.add("Expiry date is required (format: YYYY-MM-DD)");
        }

        String purchaseText = purchaseDateField.getText().replace('_', ' ').trim();
        LocalDate purchaseDate = DateUtils.parse(purchaseText);
        if (purchaseDate != null && purchaseDate.isAfter(LocalDate.now())) {
            errors.add("Purchase date cannot be in the future");
        }

        if (expiryDate != null && purchaseDate != null && expiryDate.isBefore(purchaseDate)) {
            errors.add("Expiry date cannot be before purchase date");
        }

        int qty = (Integer) quantitySpinner.getValue();
        if (qty < 0) {
            errors.add("Quantity must be non-negative");
        }

        int threshold = (Integer) thresholdSpinner.getValue();
        if (threshold <= 0) {
            errors.add("Low-stock threshold must be positive");
        }

        return errors;
    }

    public Medicine getResult() {
        return result;
    }
}