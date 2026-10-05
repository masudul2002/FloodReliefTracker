package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.Alert;
import com.floodrelief.model.AlertSeverity;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertDao {
    private static final Logger logger = LoggerFactory.getLogger(AlertDao.class);

    public List<Alert> getActiveAlerts() {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT alert_id, type, message, severity, created_at, status " +
                     "FROM alerts WHERE status = 'ACTIVE' ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching active alerts: {}", e.getMessage());
        }
        return list;
    }

    public List<Alert> getAllAlerts() {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT alert_id, type, message, severity, created_at, status " +
                     "FROM alerts ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all alerts: {}", e.getMessage());
        }
        return list;
    }

    public boolean createAlert(Alert alert) {
        // Prevent exact duplicates of active alerts within the last hour
        String checkSql = "SELECT COUNT(*) FROM alerts WHERE type = ? AND message = ? AND status = 'ACTIVE' " +
                          "AND created_at >= ?";
        String insertSql = "INSERT INTO alerts (type, message, severity, created_at, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement cs = conn.prepareStatement(checkSql)) {
                cs.setString(1, alert.getType());
                cs.setString(2, alert.getMessage());
                cs.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusHours(2)));
                try (ResultSet rs = cs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Already logged recently
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, alert.getType());
                ps.setString(2, alert.getMessage());
                ps.setString(3, alert.getSeverity().name());
                ps.setTimestamp(4, Timestamp.valueOf(alert.getCreatedAt() != null ? alert.getCreatedAt() : LocalDateTime.now()));
                ps.setString(5, alert.getStatus() != null ? alert.getStatus() : "ACTIVE");
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error creating alert: {}", e.getMessage());
            return false;
        }
    }

    public boolean resolveAlert(int alertId) {
        String sql = "UPDATE alerts SET status = 'RESOLVED' WHERE alert_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error resolving alert {}: {}", alertId, e.getMessage());
            return false;
        }
    }

    public boolean resolveAllAlerts() {
        String sql = "UPDATE alerts SET status = 'RESOLVED' WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error resolving all alerts: {}", e.getMessage());
            return false;
        }
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        Alert a = new Alert();
        a.setAlertId(rs.getInt("alert_id"));
        a.setType(rs.getString("type"));
        a.setMessage(rs.getString("message"));
        try {
            a.setSeverity(AlertSeverity.valueOf(rs.getString("severity")));
        } catch (Exception e) {
            a.setSeverity(AlertSeverity.WARNING);
        }
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        a.setStatus(rs.getString("status"));
        return a;
    }
}
