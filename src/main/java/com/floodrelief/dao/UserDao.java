package com.floodrelief.dao;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.model.User;
import com.floodrelief.model.UserRole;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    public User getByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, full_name, role, active_status, created_at " +
                     "FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching user by username {}: {}", username, e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, role, active_status, created_at " +
                     "FROM users ORDER BY username ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users: {}", e.getMessage());
        }
        return list;
    }

    public boolean create(User user) {
        String sql = "INSERT INTO users (user_id, username, password_hash, full_name, role, active_status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActiveStatus());
            ps.setTimestamp(7, Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return false;
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, role = ?, active_status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole().name());
            ps.setBoolean(3, user.isActiveStatus());
            ps.setString(4, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(String userId, String newHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password: {}", e.getMessage());
            return false;
        }
    }

    public boolean delete(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getString("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        try {
            u.setRole(UserRole.valueOf(rs.getString("role")));
        } catch (Exception e) {
            u.setRole(UserRole.VOLUNTEER);
        }
        u.setActiveStatus(rs.getBoolean("active_status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}
