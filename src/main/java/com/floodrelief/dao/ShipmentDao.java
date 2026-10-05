package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.ItemCategory;
import com.floodrelief.model.Shipment;
import com.floodrelief.model.ShipmentItem;
import com.floodrelief.model.ShipmentStatus;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipmentDao {
    private static final Logger logger = LoggerFactory.getLogger(ShipmentDao.class);

    public List<Shipment> getAllShipments() {
        List<Shipment> list = new ArrayList<>();
        String sql = "SELECT s.shipment_id, s.warehouse_id, w.name as warehouse_name, " +
                     "s.camp_id, c.name as camp_name, s.status, s.dispatch_time, " +
                     "s.expected_delivery_time, s.actual_delivery_time, s.responsible_user, " +
                     "s.driver_contact, s.transport_mode, s.notes, s.created_at " +
                     "FROM shipments s " +
                     "LEFT JOIN warehouses w ON s.warehouse_id = w.warehouse_id " +
                     "LEFT JOIN camps c ON s.camp_id = c.camp_id " +
                     "ORDER BY s.created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Shipment s = mapRow(rs);
                s.setItems(getItemsForShipment(conn, s.getShipmentId()));
                list.add(s);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all shipments: {}", e.getMessage());
        }
        return list;
    }

    public Shipment getById(String shipmentId) {
        String sql = "SELECT s.shipment_id, s.warehouse_id, w.name as warehouse_name, " +
                     "s.camp_id, c.name as camp_name, s.status, s.dispatch_time, " +
                     "s.expected_delivery_time, s.actual_delivery_time, s.responsible_user, " +
                     "s.driver_contact, s.transport_mode, s.notes, s.created_at " +
                     "FROM shipments s " +
                     "LEFT JOIN warehouses w ON s.warehouse_id = w.warehouse_id " +
                     "LEFT JOIN camps c ON s.camp_id = c.camp_id " +
                     "WHERE s.shipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shipment s = mapRow(rs);
                    s.setItems(getItemsForShipment(conn, shipmentId));
                    return s;
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching shipment {}: {}", shipmentId, e.getMessage());
        }
        return null;
    }

    public boolean create(Shipment s, List<ShipmentItem> items) {
        String sql = "INSERT INTO shipments (shipment_id, warehouse_id, camp_id, status, " +
                     "dispatch_time, expected_delivery_time, actual_delivery_time, " +
                     "responsible_user, driver_contact, transport_mode, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, s.getShipmentId());
                ps.setString(2, s.getWarehouseId());
                ps.setString(3, s.getCampId());
                ps.setString(4, s.getStatus().name());
                if (s.getDispatchTime() != null) {
                    ps.setTimestamp(5, Timestamp.valueOf(s.getDispatchTime()));
                } else {
                    ps.setNull(5, Types.TIMESTAMP);
                }
                if (s.getExpectedDeliveryTime() != null) {
                    ps.setTimestamp(6, Timestamp.valueOf(s.getExpectedDeliveryTime()));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }
                if (s.getActualDeliveryTime() != null) {
                    ps.setTimestamp(7, Timestamp.valueOf(s.getActualDeliveryTime()));
                } else {
                    ps.setNull(7, Types.TIMESTAMP);
                }
                ps.setString(8, s.getResponsibleUser());
                ps.setString(9, s.getDriverContact());
                ps.setString(10, s.getTransportMode());
                ps.setString(11, s.getNotes());
                ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            if (items != null && !items.isEmpty()) {
                String itemSql = "INSERT INTO shipment_items (shipment_id, item_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement ips = conn.prepareStatement(itemSql)) {
                    for (ShipmentItem item : items) {
                        ips.setString(1, s.getShipmentId());
                        ips.setString(2, item.getItemId());
                        ips.setInt(3, item.getQuantity());
                        ips.addBatch();
                    }
                    ips.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error creating shipment: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(String shipmentId, ShipmentStatus newStatus, LocalDateTime actualDelivery) {
        String sql = "UPDATE shipments SET status = ?, actual_delivery_time = ? WHERE shipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            if (actualDelivery != null) {
                ps.setTimestamp(2, Timestamp.valueOf(actualDelivery));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setString(3, shipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating shipment status: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateDispatchTime(String shipmentId, LocalDateTime dispatchTime) {
        String sql = "UPDATE shipments SET dispatch_time = ? WHERE shipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dispatchTime));
            ps.setString(2, shipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating shipment dispatch time: {}", e.getMessage());
            return false;
        }
    }

    public List<ShipmentItem> getItemsForShipment(Connection conn, String shipmentId) {
        List<ShipmentItem> list = new ArrayList<>();
        String sql = "SELECT si.id, si.shipment_id, si.item_id, si.quantity, " +
                     "r.name as item_name, r.category, r.unit " +
                     "FROM shipment_items si " +
                     "LEFT JOIN relief_items r ON si.item_id = r.item_id " +
                     "WHERE si.shipment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShipmentItem item = new ShipmentItem();
                    item.setId(rs.getInt("id"));
                    item.setShipmentId(rs.getString("shipment_id"));
                    item.setItemId(rs.getString("item_id"));
                    item.setItemName(rs.getString("item_name"));
                    try {
                        item.setItemCategory(ItemCategory.valueOf(rs.getString("category")));
                    } catch (Exception e) {
                        item.setItemCategory(ItemCategory.FOOD);
                    }
                    item.setUnit(rs.getString("unit"));
                    item.setQuantity(rs.getInt("quantity"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching items for shipment {}: {}", shipmentId, e.getMessage());
        }
        return list;
    }

    private Shipment mapRow(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setShipmentId(rs.getString("shipment_id"));
        s.setWarehouseId(rs.getString("warehouse_id"));
        s.setWarehouseName(rs.getString("warehouse_name"));
        s.setCampId(rs.getString("camp_id"));
        s.setCampName(rs.getString("camp_name"));
        try {
            s.setStatus(ShipmentStatus.valueOf(rs.getString("status")));
        } catch (Exception e) {
            s.setStatus(ShipmentStatus.PENDING);
        }
        Timestamp dt = rs.getTimestamp("dispatch_time");
        if (dt != null) s.setDispatchTime(dt.toLocalDateTime());
        Timestamp edt = rs.getTimestamp("expected_delivery_time");
        if (edt != null) s.setExpectedDeliveryTime(edt.toLocalDateTime());
        Timestamp adt = rs.getTimestamp("actual_delivery_time");
        if (adt != null) s.setActualDeliveryTime(adt.toLocalDateTime());
        s.setResponsibleUser(rs.getString("responsible_user"));
        s.setDriverContact(rs.getString("driver_contact"));
        s.setTransportMode(rs.getString("transport_mode"));
        s.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(ca.toLocalDateTime());
        return s;
    }
}
