package com.floodrelief.model;

import java.awt.Color;

/**
 * Severity levels for automated system alerts.
 */
public enum AlertSeverity {
    INFO("Info", new Color(23, 162, 184)),
    WARNING("Warning", new Color(255, 193, 7)),
    CRITICAL("Critical", new Color(220, 53, 69));

    private final String displayName;
    private final Color badgeColor;

    AlertSeverity(String displayName, Color badgeColor) {
        this.displayName = displayName;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getBadgeColor() {
        return badgeColor;
    }
}
