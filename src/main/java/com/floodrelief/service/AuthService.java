package com.floodrelief.service;

import com.floodrelief.dao.UserDao;
import com.floodrelief.model.User;
import com.floodrelief.model.UserRole;
import com.floodrelief.config.AuthContext;
import com.floodrelief.service.PasswordSecurity;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao = new UserDao();

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        User user = userDao.getByUsername(username.trim());
        if (user == null || !user.isActiveStatus()) {
            logger.warn("Login failed: User {} not found or inactive", username);
            return false;
        }

        if (PasswordSecurity.verifyPassword(password, user.getPasswordHash())) {
            AuthContext.setCurrentUser(user);
            logger.info("User {} logged in successfully with role {}", username, user.getRole());
            return true;
        } else {
            logger.warn("Login failed: Invalid password for user {}", username);
            return false;
        }
    }

    public void logout() {
        AuthContext.logout();
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public boolean createUser(String username, String password, String fullName, UserRole role) {
        if (!AuthContext.canManageUsers()) {
            throw new SecurityException("Only administrators can create users.");
        }
        String id = "USR-" + System.currentTimeMillis() % 100000;
        String hash = PasswordSecurity.hashPassword(password);
        User u = new User(id, username.trim(), hash, fullName.trim(), role, true);
        return userDao.create(u);
    }

    public boolean updateUserRoleAndStatus(String userId, String fullName, UserRole role, boolean active) {
        if (!AuthContext.canManageUsers()) {
            throw new SecurityException("Only administrators can modify users.");
        }
        User u = new User();
        u.setUserId(userId);
        u.setFullName(fullName);
        u.setRole(role);
        u.setActiveStatus(active);
        return userDao.update(u);
    }

    public boolean changePassword(String userId, String newPassword) {
        String hash = PasswordSecurity.hashPassword(newPassword);
        return userDao.updatePassword(userId, hash);
    }
}
