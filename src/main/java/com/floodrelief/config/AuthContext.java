package com.floodrelief.config;

import com.floodrelief.model.User;
import com.floodrelief.model.UserRole;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe Authentication and Session Context for the current desktop application session.
 * Located in com.floodrelief.config as per project architectural layout.
 */
public class AuthContext {
    private static User currentUser = null;
    private static final List<AuthChangeListener> listeners = new ArrayList<>();

    public interface AuthChangeListener {
        void onAuthChanged(User user);
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        for (AuthChangeListener listener : listeners) {
            listener.onAuthChanged(user);
        }
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    public static synchronized void logout() {
        setCurrentUser(null);
    }

    public static synchronized void addListener(AuthChangeListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(AuthChangeListener listener) {
        listeners.remove(listener);
    }

    public static synchronized boolean hasRole(UserRole role) {
        if (currentUser == null) return false;
        if ((role == UserRole.ADMIN || role == UserRole.ADMINISTRATOR) &&
            (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.ADMINISTRATOR)) {
            return true;
        }
        return currentUser.getRole() == role;
    }

    public static synchronized boolean canManageUsers() {
        return currentUser != null && currentUser.getRole().canManageUsers();
    }

    public static synchronized boolean canManageInventory() {
        return currentUser != null && currentUser.getRole().canManageInventory();
    }

    public static synchronized boolean canManageShipments() {
        return currentUser != null && currentUser.getRole().canManageShipments();
    }

    public static synchronized boolean canRegisterCamps() {
        return currentUser != null && currentUser.getRole().canRegisterCamps();
    }

    public static synchronized boolean canRegisterFamilies() {
        return currentUser != null && currentUser.getRole().canRegisterFamilies();
    }

    public static synchronized boolean canRecordDistributions() {
        return currentUser != null && currentUser.getRole().canRecordDistributions();
    }

    public static synchronized boolean canOverrideDuplicateDistributions() {
        return currentUser != null && currentUser.getRole().canOverrideDuplicateDistributions();
    }

    public static synchronized boolean canModifySystemConfig() {
        return currentUser != null && currentUser.getRole().canModifySystemConfig();
    }

    public static synchronized boolean isReadOnly() {
        return currentUser != null && currentUser.getRole().isReadOnly();
    }
}
