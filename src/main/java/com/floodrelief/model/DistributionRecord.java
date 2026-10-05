package com.floodrelief.model;

import java.time.LocalDateTime;

/**
 * Record of relief aid distributed to a beneficiary family.
 */
public class DistributionRecord {
    private String recordId;
    private String familyId;
    private String headName;
    private String campId;
    private String campName;
    private ItemCategory itemCategory;
    private String itemId;
    private String itemName;
    private int quantity;
    private LocalDateTime dateTime;
    private String recordedBy;
    private String shipmentId;
    private boolean isOverride;
    private String overrideReason;

    public DistributionRecord() {
        this.dateTime = LocalDateTime.now();
        this.isOverride = false;
    }

    public DistributionRecord(String recordId, String familyId, String campId, ItemCategory itemCategory,
                              String itemId, int quantity, String recordedBy, String shipmentId) {
        this();
        this.recordId = recordId;
        this.familyId = familyId;
        this.campId = campId;
        this.itemCategory = itemCategory;
        this.itemId = itemId;
        this.quantity = quantity;
        this.recordedBy = recordedBy;
        this.shipmentId = shipmentId;
    }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public String getHeadName() { return headName; }
    public void setHeadName(String headName) { this.headName = headName; }

    public String getCampId() { return campId; }
    public void setCampId(String campId) { this.campId = campId; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public ItemCategory getItemCategory() { return itemCategory; }
    public void setItemCategory(ItemCategory itemCategory) { this.itemCategory = itemCategory; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public boolean isOverride() { return isOverride; }
    public void setOverride(boolean override) { isOverride = override; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    @Override
    public String toString() {
        return "Dist #" + recordId + " - Family " + familyId + " (" + itemCategory + " x " + quantity + ")";
    }
}
