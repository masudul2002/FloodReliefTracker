package com.floodrelief.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);
    private static boolean isDark = false;

    public static final Color PRIMARY = new Color(13, 110, 253);
    public static final Color SUCCESS = new Color(40, 167, 69);
    public static final Color WARNING = new Color(255, 193, 7);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color INFO = new Color(23, 162, 184);

    public static void setup() {
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.rowHeight", 30);
            FlatLightLaf.setup();
        } catch (Exception e) {
            logger.error("Error setting FlatLaf: {}", e.getMessage());
        }
    }

    public static boolean isDarkMode() {
        return isDark;
    }

    public static void toggleTheme(JFrame rootFrame) {
        try {
            if (isDark) {
                FlatLightLaf.setup();
                isDark = false;
            } else {
                FlatDarkLaf.setup();
                isDark = true;
            }
            FlatLaf.updateUI();
            if (rootFrame != null) {
                SwingUtilities.updateComponentTreeUI(rootFrame);
            }
        } catch (Exception e) {
            logger.error("Error toggling theme: {}", e.getMessage());
        }
    }
}
