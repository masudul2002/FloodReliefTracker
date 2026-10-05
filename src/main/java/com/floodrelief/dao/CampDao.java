package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.Camp;
import com.floodrelief.model.SeverityLabel;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampDao {
    private static final Logger logger = LoggerFactory.getLogger(CampDao.class);

    public List<Camp> getAllCamps() {
        List<Camp> list = new ArrayList<>();
        String sql = "SELECT camp_id, name, district, area, population, capacity, waterlogged_days, " +
                     "medical_need, last_relief_date, severity_score, severity_label, latitude, longitude, created_at, updated_at " +
                     "FROM camps ORDER BY severity_score DESC, waterlogged_days DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all camps: {}", e.getMessage());
        }
        return list;
    }

    public Camp getById(String campId) {
        String sql = "SELECT camp_id, name, district, area, population, capacity, waterlogged_days, " +
                     "medical_need, last_relief_date, severity_score, severity_label, latitude, longitude, created_at, updated_at " +
                     "FROM camps WHERE camp_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching camp by id {}: {}", campId, e.getMessage());
        }
        return null;
    }

    public List<Camp> searchCamps(String keyword, String districtFilter, SeverityLabel severityFilter) {
        List<Camp> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT camp_id, name, district, area, population, capacity, waterlogged_days, " +
            "medical_need, last_relief_date, severity_score, severity_label, latitude, longitude, created_at, updated_at " +
            "FROM camps WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(area) LIKE ? OR LOWER(camp_id) LIKE ?) ");
            String term = "%" + keyword.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (districtFilter != null && !districtFilter.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(district) = LOWER(?) ");
            params.add(districtFilter.trim());
        }
        if (severityFilter != null) {
            sql.append("AND severity_label = ? ");
            params.add(severityFilter.name());
        }
        sql.append("ORDER BY severity_score DESC, waterlogged_days DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching camps: {}", e.getMessage());
        }
        return list;
    }

    public boolean create(Camp camp) {
        String sql = "INSERT INTO camps (camp_id, name, district, area, population, capacity, " +
                     "waterlogged_days, medical_need, last_relief_date, severity_score, severity_label, " +
                     "latitude, longitude, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, camp.getCampId());
            ps.setString(2, camp.getName());
            ps.setString(3, camp.getDistrict());
            ps.setString(4, camp.getArea());
            ps.setInt(5, camp.getPopulation());
            ps.setInt(6, camp.getCapacity());
            ps.setInt(7, camp.getWaterloggedDays());
            ps.setInt(8, camp.getMedicalNeed());
            if (camp.getLastReliefDate() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(camp.getLastReliefDate()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }
            ps.setDouble(10, camp.getSeverityScore());
            ps.setString(11, camp.getSeverityLabel().name());
            ps.setDouble(12, camp.getLatitude());
            ps.setDouble(13, camp.getLongitude());
            ps.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating camp: {}", e.getMessage());
            return false;
        }
    }

    public boolean update(Camp camp) {
        String sql = "UPDATE camps SET name = ?, district = ?, area = ?, population = ?, capacity = ?, " +
                     "waterlogged_days = ?, medical_need = ?, last_relief_date = ?, severity_score = ?, " +
                     "severity_label = ?, latitude = ?, longitude = ?, updated_at = ? WHERE camp_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, camp.getName());
            ps.setString(2, camp.getDistrict());
            ps.setString(3, camp.getArea());
            ps.setInt(4, camp.getPopulation());
            ps.setInt(5, camp.getCapacity());
            ps.setInt(6, camp.getWaterloggedDays());
            ps.setInt(7, camp.getMedicalNeed());
            if (camp.getLastReliefDate() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(camp.getLastReliefDate()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }
            ps.setDouble(9, camp.getSeverityScore());
            ps.setString(10, camp.getSeverityLabel().name());
            ps.setDouble(11, camp.getLatitude());
            ps.setDouble(12, camp.getLongitude());
            ps.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(14, camp.getCampId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating camp: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateSeverity(String campId, double score, SeverityLabel label) {
        String sql = "UPDATE camps SET severity_score = ?, severity_label = ?, updated_at = ? WHERE camp_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setString(2, label.name());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, campId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating camp severity: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateLastReliefDate(String campId, LocalDateTime reliefDate) {
        String sql = "UPDATE camps SET last_relief_date = ?, updated_at = ? WHERE camp_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(reliefDate));
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, campId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating last relief date: {}", e.getMessage());
            return false;
        }
    }

    public boolean delete(String campId) {
        String sql = "DELETE FROM camps WHERE camp_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting camp: {}", e.getMessage());
            return false;
        }
    }

    private Camp mapRow(ResultSet rs) throws SQLException {
        Camp c = new Camp();
        c.setCampId(rs.getString("camp_id"));
        c.setName(rs.getString("name"));
        c.setDistrict(rs.getString("district"));
        c.setArea(rs.getString("area"));
        c.setPopulation(rs.getInt("population"));
        c.setCapacity(rs.getInt("capacity"));
        c.setWaterloggedDays(rs.getInt("waterlogged_days"));
        c.setMedicalNeed(rs.getInt("medical_need"));
        Timestamp lastRelief = rs.getTimestamp("last_relief_date");
        if (lastRelief != null) {
            c.setLastReliefDate(lastRelief.toLocalDateTime());
        }
        c.setSeverityScore(rs.getDouble("severity_score"));
        try {
            c.setSeverityLabel(SeverityLabel.valueOf(rs.getString("severity_label")));
        } catch (Exception e) {
            c.setSeverityLabel(SeverityLabel.fromScore(c.getSeverityScore()));
        }
        c.setLatitude(rs.getDouble("latitude"));
        c.setLongitude(rs.getDouble("longitude"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) c.setUpdatedAt(ua.toLocalDateTime());
        return c;
    }
}
