package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.RouteEdge;
import com.floodrelief.model.RouteNode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteDao {
    private static final Logger logger = LoggerFactory.getLogger(RouteDao.class);

    public List<RouteNode> getAllNodes() {
        List<RouteNode> list = new ArrayList<>();
        String sql = "SELECT node_id, name, type, district, pos_x, pos_y FROM route_nodes ORDER BY name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RouteNode node = new RouteNode();
                node.setNodeId(rs.getString("node_id"));
                node.setName(rs.getString("name"));
                node.setType(rs.getString("type"));
                node.setDistrict(rs.getString("district"));
                node.setPosX(rs.getInt("pos_x"));
                node.setPosY(rs.getInt("pos_y"));
                list.add(node);
            }
        } catch (SQLException e) {
            logger.error("Error fetching route nodes: {}", e.getMessage());
        }
        return list;
    }

    public List<RouteEdge> getAllEdges() {
        List<RouteEdge> list = new ArrayList<>();
        String sql = "SELECT edge_id, from_node, to_node, distance_km, route_type, is_passable FROM route_edges";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RouteEdge edge = new RouteEdge();
                edge.setEdgeId(rs.getInt("edge_id"));
                edge.setFromNode(rs.getString("from_node"));
                edge.setToNode(rs.getString("to_node"));
                edge.setDistanceKm(rs.getDouble("distance_km"));
                edge.setRouteType(rs.getString("route_type"));
                edge.setPassable(rs.getBoolean("is_passable"));
                list.add(edge);
            }
        } catch (SQLException e) {
            logger.error("Error fetching route edges: {}", e.getMessage());
        }
        return list;
    }

    public boolean createNode(RouteNode node) {
        String sql = "INSERT INTO route_nodes (node_id, name, type, district, pos_x, pos_y) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (node_id) DO UPDATE SET name = EXCLUDED.name, pos_x = EXCLUDED.pos_x, pos_y = EXCLUDED.pos_y";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, node.getNodeId());
            ps.setString(2, node.getName());
            ps.setString(3, node.getType());
            ps.setString(4, node.getDistrict());
            ps.setInt(5, node.getPosX());
            ps.setInt(6, node.getPosY());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating route node: {}", e.getMessage());
            return false;
        }
    }

    public boolean createEdge(RouteEdge edge) {
        String sql = "INSERT INTO route_edges (from_node, to_node, distance_km, route_type, is_passable) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, edge.getFromNode());
            ps.setString(2, edge.getToNode());
            ps.setDouble(3, edge.getDistanceKm());
            ps.setString(4, edge.getRouteType());
            ps.setBoolean(5, edge.isPassable());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating route edge: {}", e.getMessage());
            return false;
        }
    }

    public boolean toggleEdgePassability(int edgeId, boolean passable) {
        String sql = "UPDATE route_edges SET is_passable = ? WHERE edge_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, passable);
            ps.setInt(2, edgeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error toggling edge passability: {}", e.getMessage());
            return false;
        }
    }
}
