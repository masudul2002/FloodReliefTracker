package com.floodrelief.service;

import com.floodrelief.dao.CampDao;
import com.floodrelief.dao.ShipmentDao;
import com.floodrelief.model.Shipment;
import com.floodrelief.model.ShipmentItem;
import com.floodrelief.model.ShipmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipmentService {
    private static final Logger logger = LoggerFactory.getLogger(ShipmentService.class);
    private final ShipmentDao shipmentDao = new ShipmentDao();
    private final InventoryService inventoryService = new InventoryService();
    private final CampDao campDao = new CampDao();

    public List<Shipment> getAllShipments() {
        return shipmentDao.getAllShipments();
    }

    public Shipment getShipmentById(String shipmentId) {
        return shipmentDao.getById(shipmentId);
    }

    /**
     * Creates a new shipment, automatically reserving inventory for each requested item.
     */
    public synchronized boolean createShipment(String shipmentId, String warehouseId, String campId,
                                               String responsibleUser, String driverContact, String transportMode,
                                               LocalDateTime expectedDelivery, String notes,
                                               List<ShipmentItem> items) {
        // First verify and reserve inventory for all items
        for (ShipmentItem item : items) {
            boolean reserved = inventoryService.reserveStock(item.getItemId(), item.getQuantity());
            if (!reserved) {
                // Rollback any previously reserved items in this loop
                for (ShipmentItem rollbackItem : items) {
                    if (rollbackItem == item) break;
                    inventoryService.releaseReservation(rollbackItem.getItemId(), rollbackItem.getQuantity());
                }
                logger.warn("Failed to reserve required quantity for item {}. Shipment creation aborted.", item.getItemId());
                return false;
            }
        }

        Shipment shipment = new Shipment(shipmentId, warehouseId, campId, responsibleUser, transportMode, expectedDelivery);
        shipment.setDriverContact(driverContact);
        shipment.setNotes(notes);
        shipment.setStatus(ShipmentStatus.PENDING);

        boolean created = shipmentDao.create(shipment, items);
        if (!created) {
            // Release reservations
            for (ShipmentItem item : items) {
                inventoryService.releaseReservation(item.getItemId(), item.getQuantity());
            }
            return false;
        }

        logger.info("Created shipment {} with {} item categories.", shipmentId, items.size());
        return true;
    }

    /**
     * Advances or changes shipment status through lifecycle:
     * Pending ➔ Packed ➔ Dispatched ➔ In Transit ➔ Delivered (or Cancelled)
     */
    public synchronized boolean updateStatus(String shipmentId, ShipmentStatus targetStatus) {
        Shipment shipment = shipmentDao.getById(shipmentId);
        if (shipment == null) {
            logger.warn("Shipment {} not found", shipmentId);
            return false;
        }

        if (!shipment.getStatus().canTransitionTo(targetStatus)) {
            logger.warn("Invalid transition from {} to {}", shipment.getStatus(), targetStatus);
            return false;
        }

        LocalDateTime actualDelivery = null;

        // Transition handling
        if (targetStatus == ShipmentStatus.DISPATCHED) {
            shipmentDao.updateDispatchTime(shipmentId, LocalDateTime.now());
            // Move from reserved to dispatched in inventory
            if (shipment.getItems() != null) {
                for (ShipmentItem item : shipment.getItems()) {
                    inventoryService.dispatchStock(item.getItemId(), item.getQuantity());
                }
            }
        } else if (targetStatus == ShipmentStatus.DELIVERED) {
            actualDelivery = LocalDateTime.now();
            // Automatically update target camp's last relief date
            if (shipment.getCampId() != null) {
                campDao.updateLastReliefDate(shipment.getCampId(), actualDelivery);
                logger.info("Updated last relief date for camp {} upon delivery of shipment {}",
                        shipment.getCampId(), shipmentId);
            }
        } else if (targetStatus == ShipmentStatus.CANCELLED) {
            // If cancelled before dispatch, release reservations
            if (shipment.getStatus() == ShipmentStatus.PENDING || shipment.getStatus() == ShipmentStatus.PACKED) {
                if (shipment.getItems() != null) {
                    for (ShipmentItem item : shipment.getItems()) {
                        inventoryService.releaseReservation(item.getItemId(), item.getQuantity());
                    }
                }
            }
        }

        boolean updated = shipmentDao.updateStatus(shipmentId, targetStatus, actualDelivery);
        if (updated) {
            logger.info("Shipment {} transitioned to {}", shipmentId, targetStatus);
        }
        return updated;
    }
}
