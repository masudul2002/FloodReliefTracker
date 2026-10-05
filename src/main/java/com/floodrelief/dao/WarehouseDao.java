package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.Warehouse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarehouseDao {
    private static final Logger logger = LoggerFactory.getLogger(WarehouseDao.class);

    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> list = new ArrayList<>();
        String sql = "SELECT warehouse_id, name, location, capacity_sqft, contact_person " +
                     "FROM warehouses ORDER BY name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Warehouse w = new Warehouse();
                w.setWarehouseId(rs.getString("warehouse_id"));
                w.setName(rs.getString("name"));
                w.setLocation(rs.getString("location"));
                w.setCapacitySqft(rs.getInt("capacity_sqft"));
                w.setContactPerson(rs.getString("contact_person"));
                list.add(w);
            }
        } catch (SQLException e) {
            logger.error("Error fetching warehouses: {}", e.getMessage());
        }
        return list;
    }

    public Warehouse getById(String id) {
        String sql = "SELECT warehouse_id, name, location, capacity_sqft, contact_person " +
                     "FROM warehouses WHERE warehouse_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Warehouse w = new Warehouse();
                    w.setWarehouseId(rs.getString("warehouse_id"));
                    w.setName(rs.getString("name"));
                    w.setLocation(rs.getString("location"));
                    w.setCapacitySqft(rs.getInt("capacity_sqft"));
                    w.setContactPerson(rs.getString("contact_person"));
                    return w;
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching warehouse by id {}: {}", id, e.getMessage());
        }
        return null;
    }

    public boolean create(Warehouse w) {
        String sql = "INSERT INTO warehouses (warehouse_id, name, location, capacity_sqft, contact_person) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, w.getWarehouseId());
            ps.setString(2, w.getName());
            ps.setString(3, w.getLocation());
            ps.setInt(4, w.getCapacitySqft());
            ps.setString(5, w.getContactPerson());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating warehouse: {}", e.getMessage());
            return false;
        }
    }
}
