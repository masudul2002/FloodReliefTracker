package com.floodrelief.dao;

import com.floodrelief.model.DistributionRecord;
import com.floodrelief.model.ItemCategory;
import com.floodrelief.service.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for Relief Distributions.
 * Named DistributionDao as per architectural roadmap.
 */
public class DistributionDao {
    private static final Logger logger = LoggerFactory.getLogger(DistributionDao.class);

    public List<DistributionRecord> getAllRecords() {
        List<DistributionRecord> list = new ArrayList<>();
        String sql = "SELECT d.record_id, d.family_id, f.head_name, d.camp_id, c.name as camp_name, " +
                     "d.item_category, d.item_id, r.name as item_name, d.quantity, d.date_time, " +
                     "d.recorded_by, d.shipment_id, d.is_override, d.override_reason " +
                     "FROM distribution_records d " +
                     "LEFT JOIN families f ON d.family_id = f.family_id " +
                     "LEFT JOIN camps c ON d.camp_id = c.camp_id " +
                     "LEFT JOIN relief_items r ON d.item_id = r.item_id " +
                     "ORDER BY d.date_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching distribution records: {}", e.getMessage());
        }
        return list;
    }

    public List<DistributionRecord> getRecordsByFamily(String familyId) {
        List<DistributionRecord> list = new ArrayList<>();
        String sql = "SELECT d.record_id, d.family_id, f.head_name, d.camp_id, c.name as camp_name, " +
                     "d.item_category, d.item_id, r.name as item_name, d.quantity, d.date_time, " +
                     "d.recorded_by, d.shipment_id, d.is_override, d.override_reason " +
                     "FROM distribution_records d " +
                     "LEFT JOIN families f ON d.family_id = f.family_id " +
                     "LEFT JOIN camps c ON d.camp_id = c.camp_id " +
                     "LEFT JOIN relief_items r ON d.item_id = r.item_id " +
                     "WHERE d.family_id = ? ORDER BY d.date_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, familyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching records for family {}: {}", familyId, e.getMessage());
        }
        return list;
    }

    /**
     * Checks if a family received relief of this category within cooldown window (e.g. past X days).
     */
    public DistributionRecord getRecentDistribution(String familyId, ItemCategory category, int cooldownDays) {
        String sql = "SELECT d.record_id, d.family_id, f.head_name, d.camp_id, c.name as camp_name, " +
                     "d.item_category, d.item_id, r.name as item_name, d.quantity, d.date_time, " +
                     "d.recorded_by, d.shipment_id, d.is_override, d.override_reason " +
                     "FROM distribution_records d " +
                     "LEFT JOIN families f ON d.family_id = f.family_id " +
                     "LEFT JOIN camps c ON d.camp_id = c.camp_id " +
                     "LEFT JOIN relief_items r ON d.item_id = r.item_id " +
                     "WHERE d.family_id = ? AND d.item_category = ? " +
                     "AND d.date_time >= ? " +
                     "ORDER BY d.date_time DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, familyId);
            ps.setString(2, category.name());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusDays(cooldownDays)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking recent distribution for family {}: {}", familyId, e.getMessage());
        }
        return null;
    }

    public boolean create(DistributionRecord d) {
        String sql = "INSERT INTO distribution_records (record_id, family_id, camp_id, item_category, " +
                     "item_id, quantity, date_time, recorded_by, shipment_id, is_override, override_reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getRecordId());
            ps.setString(2, d.getFamilyId());
            ps.setString(3, d.getCampId());
            ps.setString(4, d.getItemCategory().name());
            ps.setString(5, d.getItemId());
            ps.setInt(6, d.getQuantity());
            ps.setTimestamp(7, Timestamp.valueOf(d.getDateTime() != null ? d.getDateTime() : LocalDateTime.now()));
            ps.setString(8, d.getRecordedBy());
            ps.setString(9, d.getShipmentId());
            ps.setBoolean(10, d.isOverride());
            ps.setString(11, d.getOverrideReason());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating distribution record: {}", e.getMessage());
            return false;
        }
    }

    private DistributionRecord mapRow(ResultSet rs) throws SQLException {
        DistributionRecord d = new DistributionRecord();
        d.setRecordId(rs.getString("record_id"));
        d.setFamilyId(rs.getString("family_id"));
        d.setHeadName(rs.getString("head_name"));
        d.setCampId(rs.getString("camp_id"));
        d.setCampName(rs.getString("camp_name"));
        try {
            d.setItemCategory(ItemCategory.valueOf(rs.getString("item_category")));
        } catch (Exception e) {
            d.setItemCategory(ItemCategory.FOOD);
        }
        d.setItemId(rs.getString("item_id"));
        d.setItemName(rs.getString("item_name"));
        d.setQuantity(rs.getInt("quantity"));
        Timestamp dt = rs.getTimestamp("date_time");
        if (dt != null) d.setDateTime(dt.toLocalDateTime());
        d.setRecordedBy(rs.getString("recorded_by"));
        d.setShipmentId(rs.getString("shipment_id"));
        d.setOverride(rs.getBoolean("is_override"));
        d.setOverrideReason(rs.getString("override_reason"));
        return d;
    }
}
