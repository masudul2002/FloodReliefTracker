package com.floodrelief.model;

/**
 * Roles for Role-Based Access Control (RBAC).
 * ADMINISTRATOR / ADMIN: Full access.
 * RELIEF_COORDINATOR: Manage inventory, shipments, routes, and approve overrides.
 * CAMP_MANAGER: Register camps, families, record distributions.
 * VOLUNTEER: View camps, log distributions (cannot override).
 * DONOR: Read-only access to dashboards, reports, and stock levels.
 */
public enum UserRole {
    ADMINISTRATOR("Administrator", "Full system administration and user management"),
    ADMIN("Administrator", "Full system administration and user management"),
    RELIEF_COORDINATOR("Relief Coordinator", "Logistics, shipments, inventory, and override approvals"),
    CAMP_MANAGER("Camp Manager", "Camp registration, family records, distribution management"),
    VOLUNTEER("Volunteer", "Distribution logging and camp assistance"),
    DONOR("Donor", "Read-only transparency dashboard and relief reports");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean canManageUsers() {
        return this == ADMINISTRATOR || this == ADMIN;
    }

    public boolean canManageInventory() {
        return this == ADMINISTRATOR || this == ADMIN || this == RELIEF_COORDINATOR;
    }

    public boolean canManageShipments() {
        return this == ADMINISTRATOR || this == ADMIN || this == RELIEF_COORDINATOR;
    }

    public boolean canRegisterCamps() {
        return this == ADMINISTRATOR || this == ADMIN || this == RELIEF_COORDINATOR || this == CAMP_MANAGER;
    }

    public boolean canRegisterFamilies() {
        return this == ADMINISTRATOR || this == ADMIN || this == RELIEF_COORDINATOR || this == CAMP_MANAGER || this == VOLUNTEER;
    }

    public boolean canRecordDistributions() {
        return this == ADMINISTRATOR || this == ADMIN || this == RELIEF_COORDINATOR || this == CAMP_MANAGER || this == VOLUNTEER;
    }

    public boolean canOverrideDuplicateDistributions() {
        return this == ADMINISTRATOR || this == ADMIN || this == RELIEF_COORDINATOR;
    }

    public boolean canModifySystemConfig() {
        return this == ADMINISTRATOR || this == ADMIN;
    }

    public boolean isReadOnly() {
        return this == DONOR;
    }

    public static UserRole fromString(String val) {
        if (val == null) return VOLUNTEER;
        String upper = val.trim().toUpperCase();
        if ("ADMIN".equals(upper) || "ADMINISTRATOR".equals(upper)) return ADMINISTRATOR;
        try {
            return UserRole.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return VOLUNTEER;
        }
    }
}
