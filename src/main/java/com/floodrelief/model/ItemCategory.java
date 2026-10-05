package com.floodrelief.model;

/**
 * Categories of disaster relief items.
 */
public enum ItemCategory {
    FOOD("Food & Nutrition", "Emergency dry rations, rice, lentils, high-energy biscuits"),
    MEDICAL("Medical & ORS", "Oral rehydration salts, water purification tablets, first-aid"),
    WASH("WASH / Sanitation", "Soap, hygiene kits, jerry cans, sanitary packs"),
    SHELTER("Emergency Shelter", "Tarpaulins, ropes, blankets, temporary tents"),
    CLOTHING("Clothing", "Dry clothes, emergency garments, rainwear");

    private final String displayName;
    private final String description;

    ItemCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
