package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.Family;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FamilyDao {
    private static final Logger logger = LoggerFactory.getLogger(FamilyDao.class);

    public List<Family> getAllFamilies() {
        List<Family> list = new ArrayList<>();
        String sql = "SELECT f.family_id, f.camp_id, c.name as camp_name, f.head_name, f.member_count, " +
                     "f.vulnerability_notes, f.registration_date " +
                     "FROM families f LEFT JOIN camps c ON f.camp_id = c.camp_id " +
                     "ORDER BY f.registration_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all families: {}", e.getMessage());
        }
        return list;
    }

    public List<Family> getFamiliesByCamp(String campId) {
        List<Family> list = new ArrayList<>();
        String sql = "SELECT f.family_id, f.camp_id, c.name as camp_name, f.head_name, f.member_count, " +
                     "f.vulnerability_notes, f.registration_date " +
                     "FROM families f LEFT JOIN camps c ON f.camp_id = c.camp_id " +
                     "WHERE f.camp_id = ? ORDER BY f.head_name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching families by camp {}: {}", campId, e.getMessage());
        }
        return list;
    }

    public Family getById(String familyId) {
        String sql = "SELECT f.family_id, f.camp_id, c.name as camp_name, f.head_name, f.member_count, " +
                     "f.vulnerability_notes, f.registration_date " +
                     "FROM families f LEFT JOIN camps c ON f.camp_id = c.camp_id " +
                     "WHERE f.family_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, familyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching family by id {}: {}", familyId, e.getMessage());
        }
        return null;
    }

    public List<Family> searchFamilies(String query, String campIdFilter) {
        List<Family> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT f.family_id, f.camp_id, c.name as camp_name, f.head_name, f.member_count, " +
            "f.vulnerability_notes, f.registration_date " +
            "FROM families f LEFT JOIN camps c ON f.camp_id = c.camp_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(f.head_name) LIKE ? OR LOWER(f.family_id) LIKE ? OR LOWER(f.vulnerability_notes) LIKE ?) ");
            String term = "%" + query.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (campIdFilter != null && !campIdFilter.equalsIgnoreCase("All")) {
            sql.append("AND f.camp_id = ? ");
            params.add(campIdFilter.trim());
        }
        sql.append("ORDER BY f.registration_date DESC");

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
            logger.error("Error searching families: {}", e.getMessage());
        }
        return list;
    }

    public boolean create(Family family) {
        String sql = "INSERT INTO families (family_id, camp_id, head_name, member_count, vulnerability_notes, registration_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, family.getFamilyId());
            ps.setString(2, family.getCampId());
            ps.setString(3, family.getHeadName());
            ps.setInt(4, family.getMemberCount());
            ps.setString(5, family.getVulnerabilityNotes());
            ps.setTimestamp(6, Timestamp.valueOf(family.getRegistrationDate() != null ? family.getRegistrationDate() : LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating family: {}", e.getMessage());
            return false;
        }
    }

    public boolean update(Family family) {
        String sql = "UPDATE families SET camp_id = ?, head_name = ?, member_count = ?, vulnerability_notes = ? " +
                     "WHERE family_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, family.getCampId());
            ps.setString(2, family.getHeadName());
            ps.setInt(3, family.getMemberCount());
            ps.setString(4, family.getVulnerabilityNotes());
            ps.setString(5, family.getFamilyId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating family: {}", e.getMessage());
            return false;
        }
    }

    public boolean delete(String familyId) {
        String sql = "DELETE FROM families WHERE family_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, familyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting family: {}", e.getMessage());
            return false;
        }
    }

    private Family mapRow(ResultSet rs) throws SQLException {
        Family f = new Family();
        f.setFamilyId(rs.getString("family_id"));
        f.setCampId(rs.getString("camp_id"));
        f.setCampName(rs.getString("camp_name"));
        f.setHeadName(rs.getString("head_name"));
        f.setMemberCount(rs.getInt("member_count"));
        f.setVulnerabilityNotes(rs.getString("vulnerability_notes"));
        Timestamp ts = rs.getTimestamp("registration_date");
        if (ts != null) {
            f.setRegistrationDate(ts.toLocalDateTime());
        }
        return f;
    }
}
