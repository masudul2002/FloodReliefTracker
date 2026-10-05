package com.floodrelief.ui.components;

import com.floodrelief.model.AlertSeverity;
import com.floodrelief.model.SeverityLabel;
import com.floodrelief.model.ShipmentStatus;
import com.floodrelief.model.UserRole;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Custom table cell renderer that displays modern colored pill badges for statuses.
 */
public class BadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (value == null) {
            return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));

        Color badgeColor = new Color(108, 117, 125);
        Color textColor = Color.WHITE;

        if (value instanceof SeverityLabel sl) {
            badgeColor = sl.getBadgeColor();
            label.setText(sl.getDisplayName());
        } else if (value instanceof ShipmentStatus ss) {
            badgeColor = ss.getBadgeColor();
            label.setText(ss.getDisplayName());
        } else if (value instanceof AlertSeverity as) {
            badgeColor = as.getBadgeColor();
            label.setText(as.getDisplayName());
        } else if (value instanceof UserRole ur) {
            label.setText(ur.getDisplayName());
            badgeColor = switch (ur) {
                case ADMINISTRATOR, ADMIN -> new Color(220, 53, 69);
                case RELIEF_COORDINATOR -> new Color(13, 110, 253);
                case CAMP_MANAGER -> new Color(40, 167, 69);
                case VOLUNTEER -> new Color(23, 162, 184);
                case DONOR -> new Color(111, 66, 193);
            };
        } else {
            String text = value.toString();
            if ("CRITICAL".equalsIgnoreCase(text) || "High Urgent".equalsIgnoreCase(text)) {
                badgeColor = new Color(220, 53, 69);
            } else if ("HIGH".equalsIgnoreCase(text) || "ACTIVE".equalsIgnoreCase(text)) {
                badgeColor = new Color(253, 126, 20);
            } else if ("MEDIUM".equalsIgnoreCase(text) || "WARNING".equalsIgnoreCase(text)) {
                badgeColor = new Color(255, 193, 7);
                textColor = Color.BLACK;
            } else if ("LOW".equalsIgnoreCase(text) || "DELIVERED".equalsIgnoreCase(text) || "RESOLVED".equalsIgnoreCase(text)) {
                badgeColor = new Color(40, 167, 69);
            }
        }

        if (!isSelected) {
            label.setBackground(badgeColor);
            label.setForeground(textColor);
            label.setOpaque(true);
        }

        return label;
    }
}
