package com.medicine.inventory.ui;

import com.medicine.inventory.model.Medicine;
import com.medicine.inventory.service.InventoryService;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MedicineTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {
        "Name", "Category", "Quantity", "Expiry Date", "Expiry Status", "Stock Status"
    };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final List<Medicine> medicines = new ArrayList<>();
    private final InventoryService inventoryService;

    public MedicineTableModel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines.clear();
        if (medicines != null) {
            this.medicines.addAll(medicines);
        }
        fireTableDataChanged();
    }

    public Medicine getMedicineAt(int row) {
        if (row >= 0 && row < medicines.size()) {
            return medicines.get(row);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return medicines.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 2 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Medicine m = medicines.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> m.getName();
            case 1 -> m.getCategory() != null ? m.getCategory() : "";
            case 2 -> m.getQuantity();
            case 3 -> m.getExpiryDate() != null ? m.getExpiryDate().format(DATE_FORMATTER) : "";
            case 4 -> InventoryService.calculateExpiryStatus(m.getExpiryDate());
            case 5 -> InventoryService.calculateStockStatus(m.getQuantity(), m.getLowStockThreshold());
            default -> "";
        };
    }
}