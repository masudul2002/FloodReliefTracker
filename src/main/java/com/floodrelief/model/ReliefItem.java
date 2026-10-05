package com.floodrelief.model;

import java.time.LocalDate;

/**
 * Relief Item inventory model across warehouses.
 */
public class ReliefItem {
    private String itemId;
    private String name;
    private ItemCategory category;
    private String unit; // bags, cartons, packets, liters, kits
    private LocalDate expiryDate;
    private int lowStockThreshold;
    private int stockIn;
    private int reserved;
    private int dispatched;
    private int damaged;
    private int expired;
    private String warehouseId;
    private String warehouseName;

    public ReliefItem() {
        this.lowStockThreshold = 100;
    }

    public ReliefItem(String itemId, String name, ItemCategory category, String unit,
                      LocalDate expiryDate, int lowStockThreshold, String warehouseId) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.lowStockThreshold = lowStockThreshold;
        this.warehouseId = warehouseId;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public int getStockIn() { return stockIn; }
    public void setStockIn(int stockIn) { this.stockIn = stockIn; }

    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }

    public int getDispatched() { return dispatched; }
    public void setDispatched(int dispatched) { this.dispatched = dispatched; }

    public int getDamaged() { return damaged; }
    public void setDamaged(int damaged) { this.damaged = damaged; }

    public int getExpired() { return expired; }
    public void setExpired(int expired) { this.expired = expired; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public int getAvailableStock() {
        return Math.max(0, stockIn - reserved - dispatched - damaged - expired);
    }

    public int getTotalActiveStock() {
        return Math.max(0, stockIn - dispatched - damaged - expired);
    }

    public boolean isLowStock() {
        return getAvailableStock() <= lowStockThreshold;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isNearExpiry(int daysThreshold) {
        if (expiryDate == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(daysThreshold);
        return !expiryDate.isBefore(LocalDate.now()) && expiryDate.isBefore(threshold);
    }

    @Override
    public String toString() {
        return name + " [" + category.getDisplayName() + "] - " + getAvailableStock() + " " + unit + " avail";
    }
}
