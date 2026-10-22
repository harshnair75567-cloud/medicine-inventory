package com.medicine.inventory.ui.renderers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StockStatusRenderer extends DefaultTableCellRenderer {
    private static final Color LOW_STOCK_BG = new Color(255, 220, 220);
    private static final Color IN_STOCK_BG = new Color(220, 255, 220);
    private static final Color TEXT_COLOR = Color.DARK_GRAY;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(CENTER);

        String text = value != null ? value.toString() : "";
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setForeground(TEXT_COLOR);
            if ("Low Stock".equals(text)) {
                setBackground(LOW_STOCK_BG);
            } else {
                setBackground(IN_STOCK_BG);
            }
        }
        setToolTipText(text);
        return c;
    }
}