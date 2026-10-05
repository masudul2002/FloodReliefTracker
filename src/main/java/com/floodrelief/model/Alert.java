package com.floodrelief.model;

import java.time.LocalDateTime;

/**
 * Real-time automated system alert for logistics and camp operations.
 */
public class Alert {
    private int alertId;
    private String type; // LOW_STOCK, EXPIRED_ITEM, DELAYED_SHIPMENT, OVER_CAPACITY, NEGLECTED_CAMP
    private String message;
    private AlertSeverity severity;
    private LocalDateTime createdAt;
    private String status; // ACTIVE, ACKNOWLEDGED, RESOLVED

    public Alert() {
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Alert(String type, String message, AlertSeverity severity) {
        this();
        this.type = type;
        this.message = message;
        this.severity = severity;
    }

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "[" + severity + "] " + type + ": " + message;
    }
}
