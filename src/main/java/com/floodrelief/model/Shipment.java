package com.floodrelief.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Emergency relief shipment model with logistics lifecycle tracking.
 */
public class Shipment {
    private String shipmentId;
    private String warehouseId;
    private String warehouseName;
    private String campId;
    private String campName;
    private ShipmentStatus status;
    private LocalDateTime dispatchTime;
    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String responsibleUser;
    private String driverContact;
    private String transportMode; // TRUCK, TRAWLER_BOAT, SPEEDBOAT, TRACTOR
    private String notes;
    private LocalDateTime createdAt;
    private List<ShipmentItem> items = new ArrayList<>();

    public Shipment() {
        this.status = ShipmentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.transportMode = "TRUCK";
    }

    public Shipment(String shipmentId, String warehouseId, String campId, String responsibleUser,
                    String transportMode, LocalDateTime expectedDeliveryTime) {
        this();
        this.shipmentId = shipmentId;
        this.warehouseId = warehouseId;
        this.campId = campId;
        this.responsibleUser = responsibleUser;
        this.transportMode = transportMode;
        this.expectedDeliveryTime = expectedDeliveryTime;
    }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getCampId() { return campId; }
    public void setCampId(String campId) { this.campId = campId; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }

    public LocalDateTime getDispatchTime() { return dispatchTime; }
    public void setDispatchTime(LocalDateTime dispatchTime) { this.dispatchTime = dispatchTime; }

    public LocalDateTime getExpectedDeliveryTime() { return expectedDeliveryTime; }
    public void setExpectedDeliveryTime(LocalDateTime expectedDeliveryTime) { this.expectedDeliveryTime = expectedDeliveryTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public String getResponsibleUser() { return responsibleUser; }
    public void setResponsibleUser(String responsibleUser) { this.responsibleUser = responsibleUser; }

    public String getDriverContact() { return driverContact; }
    public void setDriverContact(String driverContact) { this.driverContact = driverContact; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ShipmentItem> getItems() { return items; }
    public void setItems(List<ShipmentItem> items) { this.items = items; }

    public boolean isDelayed() {
        if (status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED) {
            return false;
        }
        return expectedDeliveryTime != null && LocalDateTime.now().isAfter(expectedDeliveryTime);
    }

    @Override
    public String toString() {
        return "Shipment #" + shipmentId + " -> " + campName + " [" + status.getDisplayName() + "]";
    }
}
