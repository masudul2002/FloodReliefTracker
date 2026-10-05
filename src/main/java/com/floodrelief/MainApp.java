package com.floodrelief;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.db.SchemaInitializer;
import com.floodrelief.config.AuthContext;
import com.floodrelief.service.AlertMonitoringService;
import com.floodrelief.ui.LoginDialog;
import com.floodrelief.ui.MainDashboardFrame;
import com.floodrelief.ui.ThemeManager;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flood Relief and Disaster Supply Chain Tracker (`FloodReliefTracker`)
 * Cloud-Connected Java Swing Desktop Application.
 */
public class MainApp {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        logger.info("Starting FloodReliefTracker application...");

        // 1. Setup FlatLaf modern UI theme
        ThemeManager.setup();

        // 2. Initialize Database & Seed initial data in worker thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database pool
                DatabaseManager.initialize();

                // Initialize schema and seed data
                SchemaInitializer.initializeSchemaAndSeed();

                // Start Automated Alert Monitoring background daemon (every 30 seconds)
                AlertMonitoringService.getInstance().start(30);

                // Launch Login and Main window
                launchLoginAndMain();

            } catch (Exception e) {
                logger.error("Startup error: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                        "Failed to initialize application:\n" + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void launchLoginAndMain() {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        if (loginDialog.isLoginSuccessful() && AuthContext.isAuthenticated()) {
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame dashboard = new MainDashboardFrame();
                dashboard.setVisible(true);
            });
        } else {
            logger.info("Application closed: No authenticated session.");
            System.exit(0);
        }
    }
}
