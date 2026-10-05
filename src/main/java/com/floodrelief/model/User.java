package com.floodrelief.model;

import java.time.LocalDateTime;

/**
 * User account model.
 */
public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private UserRole role;
    private boolean activeStatus;
    private LocalDateTime createdAt;

    public User() {
        this.activeStatus = true;
        this.createdAt = LocalDateTime.now();
    }

    public User(String userId, String username, String passwordHash, String fullName, UserRole role, boolean activeStatus) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.activeStatus = activeStatus;
        this.createdAt = LocalDateTime.now();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActiveStatus() { return activeStatus; }
    public void setActiveStatus(boolean activeStatus) { this.activeStatus = activeStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName != null ? fullName + " (" + role.getDisplayName() + ")" : username;
    }
}
