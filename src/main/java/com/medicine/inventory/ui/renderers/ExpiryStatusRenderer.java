package com.medicine.inventory.ui.renderers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ExpiryStatusRenderer extends DefaultTableCellRenderer {
    private static final Color EXPIRED_BG = new Color(255, 200, 200);
    private static final Color EXPIRING_SOON_BG = new Color(255, 240, 180);
    private static final Color VALID_BG = new Color(200, 255, 200);
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
            switch (text) {
                case "Expired" -> setBackground(EXPIRED_BG);
                case "Expiring Soon" -> setBackground(EXPIRING_SOON_BG);
                case "Valid" -> setBackground(VALID_BG);
                default -> setBackground(Color.WHITE);
            }
        }
        setToolTipText(text);
        return c;
    }
}