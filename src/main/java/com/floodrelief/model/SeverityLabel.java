package com.floodrelief.model;

import java.awt.Color;

/**
 * Severity labels for camps based on priority engine score (0-100).
 */
public enum SeverityLabel {
    CRITICAL("Critical", 80.0, 100.0, new Color(220, 53, 69), new Color(248, 215, 218)),
    HIGH("High", 60.0, 79.99, new Color(253, 126, 20), new Color(255, 243, 205)),
    MEDIUM("Medium", 35.0, 59.99, new Color(255, 193, 7), new Color(255, 249, 217)),
    LOW("Low", 0.0, 34.99, new Color(40, 167, 69), new Color(212, 237, 218));

    private final String displayName;
    private final double minScore;
    private final double maxScore;
    private final Color badgeColor;
    private final Color bgColor;

    SeverityLabel(String displayName, double minScore, double maxScore, Color badgeColor, Color bgColor) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.badgeColor = badgeColor;
        this.bgColor = bgColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public Color getBadgeColor() {
        return badgeColor;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public static SeverityLabel fromScore(double score) {
        if (score >= 80.0) return CRITICAL;
        if (score >= 60.0) return HIGH;
        if (score >= 35.0) return MEDIUM;
        return LOW;
    }
}
