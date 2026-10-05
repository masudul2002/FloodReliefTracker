package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemConfigDao {
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigDao.class);

    public String get(String key, String defaultValue) {
        String sql = "SELECT config_value FROM system_configurations WHERE config_key = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("config_value");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching config for key {}: {}", key, e.getMessage());
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        String val = get(key, null);
        if (val != null) {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public boolean set(String key, String value) {
        String sql = "INSERT INTO system_configurations (config_key, config_value) VALUES (?, ?) " +
                     "ON CONFLICT (config_key) DO UPDATE SET config_value = EXCLUDED.config_value";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error setting config {}={}: {}", key, value, e.getMessage());
            return false;
        }
    }

    public Map<String, String> getAll() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT config_key, config_value FROM system_configurations";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("config_key"), rs.getString("config_value"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all system configurations: {}", e.getMessage());
        }
        return map;
    }
}
