package com.floodrelief.service;

import com.floodrelief.dao.ReliefItemDao;
import com.floodrelief.model.ItemCategory;
import com.floodrelief.model.ReliefItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages disaster relief items across warehouses.
 * Implements robust Concurrency Synchronization:
 * 1. Java-level monitor locks (`synchronized` methods/blocks) for intra-process thread-safety.
 * 2. Cloud Database-level row locking (`SELECT ... FOR UPDATE` inside atomic transactions)
 *    to prevent race conditions across multiple desktop client instances.
 */
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final ReliefItemDao reliefItemDao = new ReliefItemDao();

    public List<ReliefItem> getAllItems() {
        return reliefItemDao.getAllItems();
    }

    public ReliefItem getItemById(String itemId) {
        return reliefItemDao.getById(itemId);
    }

    public synchronized boolean createItem(String itemId, String name, ItemCategory category, String unit,
                                           LocalDate expiryDate, int lowStockThreshold, int initialStock, String warehouseId) {
        ReliefItem item = new ReliefItem(itemId.trim().toUpperCase(), name.trim(), category, unit.trim(),
                expiryDate, lowStockThreshold, warehouseId);
        item.setStockIn(initialStock);
        return reliefItemDao.create(item);
    }

    /**
     * Concurrency-safe Stock-In addition.
     */
    public synchronized boolean stockIn(String itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lock row
                ReliefItem lockedItem = reliefItemDao.getByIdForUpdate(conn, itemId);
                if (lockedItem == null) {
                    conn.rollback();
                    return false;
                }

                boolean updated = reliefItemDao.updateStockIn(conn, itemId, quantity);
                conn.commit();
                logger.info("Stock-in successful for item {}: +{} {}", itemId, quantity, lockedItem.getUnit());
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction rolled back for stock-in {}: {}", itemId, e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Database error in stockIn: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Concurrency-safe Stock Reservation for shipment.
     * Guarantees that available stock cannot be over-allocated even if multiple coordinators click simultaneously.
     */
    public synchronized boolean reserveStock(String itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lock item row exclusively
                ReliefItem lockedItem = reliefItemDao.getByIdForUpdate(conn, itemId);
                if (lockedItem == null) {
                    conn.rollback();
                    return false;
                }

                if (lockedItem.getAvailableStock() < quantity) {
                    logger.warn("Reservation rejected: item {} has only {} available, requested {}",
                            itemId, lockedItem.getAvailableStock(), quantity);
                    conn.rollback();
                    return false;
                }

                boolean updated = reliefItemDao.reserveStock(conn, itemId, quantity);
                conn.commit();
                logger.info("Reserved {} {} of item {}", quantity, lockedItem.getUnit(), itemId);
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction rolled back for reservation {}: {}", itemId, e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Database error in reserveStock: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Concurrency-safe cancellation/release of reserved stock.
     */
    public synchronized boolean releaseReservation(String itemId, int quantity) {
        if (quantity <= 0) return false;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ReliefItem locked = reliefItemDao.getByIdForUpdate(conn, itemId);
                if (locked == null) {
                    conn.rollback();
                    return false;
                }
                boolean updated = reliefItemDao.releaseReservation(conn, itemId, quantity);
                conn.commit();
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Database error in releaseReservation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Concurrency-safe Dispatch: Converts reserved stock into dispatched stock.
     */
    public synchronized boolean dispatchStock(String itemId, int quantity) {
        if (quantity <= 0) return false;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ReliefItem locked = reliefItemDao.getByIdForUpdate(conn, itemId);
                if (locked == null) {
                    conn.rollback();
                    return false;
                }
                boolean updated = reliefItemDao.dispatchStock(conn, itemId, quantity);
                conn.commit();
                logger.info("Dispatched {} {} of item {}", quantity, locked.getUnit(), itemId);
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Database error in dispatchStock: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Concurrency-safe damage adjustment.
     */
    public synchronized boolean markDamaged(String itemId, int quantity) {
        if (quantity <= 0) return false;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ReliefItem locked = reliefItemDao.getByIdForUpdate(conn, itemId);
                if (locked == null || locked.getAvailableStock() < quantity) {
                    conn.rollback();
                    return false;
                }
                boolean updated = reliefItemDao.recordDamaged(conn, itemId, quantity);
                conn.commit();
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Database error in markDamaged: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Concurrency-safe expired items deduction.
     */
    public synchronized boolean markExpired(String itemId, int quantity) {
        if (quantity <= 0) return false;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ReliefItem locked = reliefItemDao.getByIdForUpdate(conn, itemId);
                if (locked == null || locked.getAvailableStock() < quantity) {
                    conn.rollback();
                    return false;
                }
                boolean updated = reliefItemDao.recordExpired(conn, itemId, quantity);
                conn.commit();
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Database error in markExpired: {}", e.getMessage());
            return false;
        }
    }
}
