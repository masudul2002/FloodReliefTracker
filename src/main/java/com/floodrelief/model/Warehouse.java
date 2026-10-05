package com.floodrelief.model;

/**
 * Relief storage warehouse or regional hub.
 */
public class Warehouse {
    private String warehouseId;
    private String name;
    private String location;
    private int capacitySqft;
    private String contactPerson;

    public Warehouse() {
        this.capacitySqft = 5000;
    }

    public Warehouse(String warehouseId, String name, String location, int capacitySqft, String contactPerson) {
        this.warehouseId = warehouseId;
        this.name = name;
        this.location = location;
        this.capacitySqft = capacitySqft;
        this.contactPerson = contactPerson;
    }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacitySqft() { return capacitySqft; }
    public void setCapacitySqft(int capacitySqft) { this.capacitySqft = capacitySqft; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
}
