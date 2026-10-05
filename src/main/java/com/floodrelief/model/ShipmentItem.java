package com.floodrelief.model;

/**
 * Item manifest detail within a shipment.
 */
public class ShipmentItem {
    private int id;
    private String shipmentId;
    private String itemId;
    private String itemName;
    private ItemCategory itemCategory;
    private String unit;
    private int quantity;

    public ShipmentItem() {}

    public ShipmentItem(String shipmentId, String itemId, int quantity) {
        this.shipmentId = shipmentId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public ItemCategory getItemCategory() { return itemCategory; }
    public void setItemCategory(ItemCategory itemCategory) { this.itemCategory = itemCategory; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return (itemName != null ? itemName : itemId) + ": " + quantity + " " + (unit != null ? unit : "units");
    }
}
