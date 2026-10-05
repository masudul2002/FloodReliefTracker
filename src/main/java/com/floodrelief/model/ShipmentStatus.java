package com.floodrelief.model;

import java.awt.Color;

/**
 * Lifecycle stages of an emergency relief shipment:
 * Pending -> Packed -> Dispatched -> In Transit -> Delivered (or Cancelled)
 */
public enum ShipmentStatus {
    PENDING("Pending", "Awaiting packaging and inventory allocation", new Color(108, 117, 125)),
    PACKED("Packed", "Supplies verified, packed and sealed at warehouse", new Color(23, 162, 184)),
    DISPATCHED("Dispatched", "Handed over to transport carrier", new Color(0, 123, 255)),
    IN_TRANSIT("In Transit", "En route to destination camp", new Color(255, 193, 7)),
    DELIVERED("Delivered", "Successfully received and verified at camp", new Color(40, 167, 69)),
    CANCELLED("Cancelled", "Shipment cancelled or rerouted", new Color(220, 53, 69));

    private final String displayName;
    private final String description;
    private final Color badgeColor;

    ShipmentStatus(String displayName, String description, Color badgeColor) {
        this.displayName = displayName;
        this.description = description;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Color getBadgeColor() {
        return badgeColor;
    }

    public boolean canTransitionTo(ShipmentStatus next) {
        if (this == CANCELLED || this == DELIVERED) return false;
        if (next == CANCELLED) return true;
        return switch (this) {
            case PENDING -> next == PACKED || next == DISPATCHED;
            case PACKED -> next == DISPATCHED;
            case DISPATCHED -> next == IN_TRANSIT || next == DELIVERED;
            case IN_TRANSIT -> next == DELIVERED;
            default -> false;
        };
    }
}
