package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.ItemCategory;
import com.floodrelief.model.ReliefItem;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReliefItemDao {
    private static final Logger logger = LoggerFactory.getLogger(ReliefItemDao.class);

    public List<ReliefItem> getAllItems() {
        List<ReliefItem> list = new ArrayList<>();
        String sql = "SELECT r.item_id, r.name, r.category, r.unit, r.expiry_date, r.low_stock_threshold, " +
                     "r.stock_in, r.reserved, r.dispatched, r.damaged, r.expired, r.warehouse_id, w.name as warehouse_name " +
                     "FROM relief_items r LEFT JOIN warehouses w ON r.warehouse_id = w.warehouse_id " +
                     "ORDER BY r.category ASC, r.name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching relief items: {}", e.getMessage());
        }
        return list;
    }

    public ReliefItem getById(String itemId) {
        String sql = "SELECT r.item_id, r.name, r.category, r.unit, r.expiry_date, r.low_stock_threshold, " +
                     "r.stock_in, r.reserved, r.dispatched, r.damaged, r.expired, r.warehouse_id, w.name as warehouse_name " +
                     "FROM relief_items r LEFT JOIN warehouses w ON r.warehouse_id = w.warehouse_id " +
                     "WHERE r.item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching relief item by id {}: {}", itemId, e.getMessage());
        }
        return null;
    }

    /**
     * Database-level row locking: Selects item with FOR UPDATE to prevent concurrency race conditions.
     */
    public ReliefItem getByIdForUpdate(Connection conn, String itemId) throws SQLException {
        String sql = "SELECT r.item_id, r.name, r.category, r.unit, r.expiry_date, r.low_stock_threshold, " +
                     "r.stock_in, r.reserved, r.dispatched, r.damaged, r.expired, r.warehouse_id, w.name as warehouse_name " +
                     "FROM relief_items r LEFT JOIN warehouses w ON r.warehouse_id = w.warehouse_id " +
                     "WHERE r.item_id = ? FOR UPDATE OF r";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean create(ReliefItem item) {
        String sql = "INSERT INTO relief_items (item_id, name, category, unit, expiry_date, low_stock_threshold, " +
                     "stock_in, reserved, dispatched, damaged, expired, warehouse_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory().name());
            ps.setString(4, item.getUnit());
            if (item.getExpiryDate() != null) {
                ps.setDate(5, Date.valueOf(item.getExpiryDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setInt(6, item.getLowStockThreshold());
            ps.setInt(7, item.getStockIn());
            ps.setInt(8, item.getReserved());
            ps.setInt(9, item.getDispatched());
            ps.setInt(10, item.getDamaged());
            ps.setInt(11, item.getExpired());
            ps.setString(12, item.getWarehouseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating relief item: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateStockIn(Connection conn, String itemId, int additionalQty) throws SQLException {
        String sql = "UPDATE relief_items SET stock_in = stock_in + ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, additionalQty);
            ps.setString(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean reserveStock(Connection conn, String itemId, int qty) throws SQLException {
        String sql = "UPDATE relief_items SET reserved = reserved + ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean releaseReservation(Connection conn, String itemId, int qty) throws SQLException {
        String sql = "UPDATE relief_items SET reserved = GREATEST(0, reserved - ?) WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean dispatchStock(Connection conn, String itemId, int qty) throws SQLException {
        String sql = "UPDATE relief_items SET reserved = GREATEST(0, reserved - ?), dispatched = dispatched + ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, qty);
            ps.setString(3, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean recordDamaged(Connection conn, String itemId, int qty) throws SQLException {
        String sql = "UPDATE relief_items SET damaged = damaged + ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean recordExpired(Connection conn, String itemId, int qty) throws SQLException {
        String sql = "UPDATE relief_items SET expired = expired + ? WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    private ReliefItem mapRow(ResultSet rs) throws SQLException {
        ReliefItem item = new ReliefItem();
        item.setItemId(rs.getString("item_id"));
        item.setName(rs.getString("name"));
        try {
            item.setCategory(ItemCategory.valueOf(rs.getString("category")));
        } catch (Exception e) {
            item.setCategory(ItemCategory.FOOD);
        }
        item.setUnit(rs.getString("unit"));
        Date exp = rs.getDate("expiry_date");
        if (exp != null) item.setExpiryDate(exp.toLocalDate());
        item.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        item.setStockIn(rs.getInt("stock_in"));
        item.setReserved(rs.getInt("reserved"));
        item.setDispatched(rs.getInt("dispatched"));
        item.setDamaged(rs.getInt("damaged"));
        item.setExpired(rs.getInt("expired"));
        item.setWarehouseId(rs.getString("warehouse_id"));
        item.setWarehouseName(rs.getString("warehouse_name"));
        return item;
    }
}
