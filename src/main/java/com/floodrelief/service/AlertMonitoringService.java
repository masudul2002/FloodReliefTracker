package com.floodrelief.service;

import com.floodrelief.dao.AlertDao;
import com.floodrelief.dao.CampDao;
import com.floodrelief.dao.ReliefItemDao;
import com.floodrelief.dao.ShipmentDao;
import com.floodrelief.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automated Background Alert Monitor.
 * Periodically inspects inventory, logistics, and shelter conditions:
 * - Low stock thresholds
 * - Expired or expiring medical supplies
 * - Delayed shipments past expected delivery times (ETAs)
 * - Camps operating over maximum capacity
 * - Camps suffering from extended relief deprivation (> 7 days)
 */
public class AlertMonitoringService {
    private static final Logger logger = LoggerFactory.getLogger(AlertMonitoringService.class);
    private static AlertMonitoringService instance;

    private final AlertDao alertDao = new AlertDao();
    private final ReliefItemDao reliefItemDao = new ReliefItemDao();
    private final ShipmentDao shipmentDao = new ShipmentDao();
    private final CampDao campDao = new CampDao();

    private ScheduledExecutorService scheduler;
    private final List<AlertListener> listeners = new ArrayList<>();

    public interface AlertListener {
        void onAlertsUpdated(List<Alert> activeAlerts);
    }

    public static synchronized AlertMonitoringService getInstance() {
        if (instance == null) {
            instance = new AlertMonitoringService();
        }
        return instance;
    }

    private AlertMonitoringService() {}

    public synchronized void start(int intervalSeconds) {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FloodRelief-AlertMonitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(this::runInspection, 2, intervalSeconds, TimeUnit.SECONDS);
        logger.info("Alert monitoring background daemon started (interval: {}s)", intervalSeconds);
    }

    public synchronized void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            logger.info("Alert monitoring daemon stopped.");
        }
    }

    public void runInspectionNow() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.execute(this::runInspection);
        } else {
            new Thread(this::runInspection).start();
        }
    }

    private void runInspection() {
        try {
            // 1. Inspect low stock
            List<ReliefItem> items = reliefItemDao.getAllItems();
            for (ReliefItem item : items) {
                int avail = item.getAvailableStock();
                if (avail == 0) {
                    alertDao.createAlert(new Alert(
                            "STOCK_DEPLETED",
                            "Stock depleted! " + item.getName() + " in " + item.getWarehouseName() + " is completely out of stock.",
                            AlertSeverity.CRITICAL
                    ));
                } else if (item.isLowStock()) {
                    alertDao.createAlert(new Alert(
                            "LOW_STOCK",
                            "Low stock alert: " + item.getName() + " has only " + avail + " " + item.getUnit() + " remaining (Threshold: " + item.getLowStockThreshold() + ").",
                            AlertSeverity.WARNING
                    ));
                }

                // 2. Inspect expired / expiring items
                if (item.isExpired()) {
                    alertDao.createAlert(new Alert(
                            "EXPIRED_SUPPLIES",
                            "Expired items detected! " + item.getName() + " expired on " + item.getExpiryDate() + ". Quarantine immediately.",
                            AlertSeverity.CRITICAL
                    ));
                } else if (item.isNearExpiry(14)) {
                    alertDao.createAlert(new Alert(
                            "EXPIRING_SOON",
                            "Urgent: " + item.getName() + " expires on " + item.getExpiryDate() + " (< 14 days remaining). Prioritize dispatch.",
                            AlertSeverity.WARNING
                    ));
                }
            }

            // 3. Inspect delayed shipments
            List<Shipment> shipments = shipmentDao.getAllShipments();
            for (Shipment s : shipments) {
                if (s.isDelayed()) {
                    alertDao.createAlert(new Alert(
                            "DELAYED_SHIPMENT",
                            "Shipment #" + s.getShipmentId() + " to " + s.getCampName() + " is overdue! Expected ETA was " + s.getExpectedDeliveryTime(),
                            AlertSeverity.CRITICAL
                    ));
                }
            }

            // 4. Inspect over-capacity & neglected camps
            List<Camp> camps = campDao.getAllCamps();
            for (Camp c : camps) {
                if (c.getPopulation() > c.getCapacity()) {
                    int excess = c.getPopulation() - c.getCapacity();
                    alertDao.createAlert(new Alert(
                            "OVER_CAPACITY",
                            "Camp " + c.getName() + " is overcrowding! Population: " + c.getPopulation() + " exceeds capacity of " + c.getCapacity() + " by " + excess + " persons.",
                            AlertSeverity.WARNING
                    ));
                }

                double daysSinceRelief = PriorityEngine.getDaysSinceLastRelief(c);
                if (daysSinceRelief >= 7.0) {
                    alertDao.createAlert(new Alert(
                            "RELIEF_DEPRIVATION",
                            "CRITICAL NEGLECT: Camp " + c.getName() + " (" + c.getArea() + ") has not received relief aid for " + (int) daysSinceRelief + " days!",
                            AlertSeverity.CRITICAL
                    ));
                }
            }

            // Notify UI
            notifyListeners();

        } catch (Exception e) {
            logger.error("Error during automated alert inspection: {}", e.getMessage());
        }
    }

    public List<Alert> getActiveAlerts() {
        return alertDao.getActiveAlerts();
    }

    public boolean resolveAlert(int alertId) {
        boolean resolved = alertDao.resolveAlert(alertId);
        if (resolved) {
            notifyListeners();
        }
        return resolved;
    }

    public boolean resolveAllAlerts() {
        boolean resolved = alertDao.resolveAllAlerts();
        if (resolved) {
            notifyListeners();
        }
        return resolved;
    }

    public synchronized void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<Alert> active = alertDao.getActiveAlerts();
        SwingUtilities.invokeLater(() -> {
            for (AlertListener l : listeners) {
                try {
                    l.onAlertsUpdated(active);
                } catch (Exception e) {
                    logger.error("Error updating alert listener: {}", e.getMessage());
                }
            }
        });
    }
}
