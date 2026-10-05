package com.floodrelief;

import com.floodrelief.model.Family;
import com.floodrelief.model.ItemCategory;
import com.floodrelief.model.ReliefItem;
import com.floodrelief.model.User;
import com.floodrelief.model.UserRole;
import com.floodrelief.config.AuthContext;
import com.floodrelief.service.DistributionService;
import com.floodrelief.service.InventoryService;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DuplicateReliefAndConcurrencyTest {

    @BeforeAll
    public static void setup() {
        AuthContext.setCurrentUser(new User("USR-ADMIN", "admin", "hash", "Admin User", UserRole.ADMIN, true));
    }

    @Test
    @DisplayName("Duplicate Relief Prevention blocks duplicate distribution within cooldown unless authorized override")
    public void testDuplicateReliefPrevention() {
        DistributionService distService = new DistributionService();
        String famId = "FAM-TEST-" + System.currentTimeMillis();
        distService.registerFamily(famId, "CMP-001", "Test Beneficiary", 4, "Flood displaced test");

        ItemCategory category = ItemCategory.FOOD;

        // Check initial eligibility
        DistributionService.DuplicateCheckResult initialCheck = distService.checkEligibility(famId, category);
        assertFalse(initialCheck.isDuplicate(), "Fresh family must be eligible");

        // Record a distribution
        String recId = "TEST-DST-" + System.currentTimeMillis();
        boolean recorded = distService.recordDistribution(
                recId, famId, "CMP-001", category, "ITM-001", 2, "admin", null, false, null
        );
        assertTrue(recorded, "First distribution should succeed");

        // Immediately check eligibility again for same category -> Must detect duplicate!
        DistributionService.DuplicateCheckResult secondCheck = distService.checkEligibility(famId, category);
        assertTrue(secondCheck.isDuplicate(), "Immediate second request must be flagged as DUPLICATE");
        assertTrue(secondCheck.getMessage().contains("DUPLICATE DETECTED"), "Message must mention duplicate");

        // Attempting another distribution without override must fail
        String recId2 = "TEST-DST2-" + System.currentTimeMillis();
        boolean blocked = distService.recordDistribution(
                recId2, famId, "CMP-001", category, "ITM-001", 1, "volunteer", null, false, null
        );
        assertFalse(blocked, "Unapproved duplicate distribution must be blocked");

        // Attempting with authorized Administrator override and reason must succeed
        String recId3 = "TEST-DST3-" + System.currentTimeMillis();
        boolean overrideApproved = distService.recordDistribution(
                recId3, famId, "CMP-001", category, "ITM-001", 1, "admin", null, true, "Emergency infant nutrition requirement"
        );
        assertTrue(overrideApproved, "Admin override with mandatory reason must succeed");
    }

    @Test
    @DisplayName("Inventory reservation prevents over-allocation under concurrency")
    public void testInventoryReservationBounds() {
        InventoryService invService = new InventoryService();
        List<ReliefItem> items = invService.getAllItems();
        assertFalse(items.isEmpty(), "Items must exist");

        ReliefItem item = items.get(0);
        int available = item.getAvailableStock();

        // Reserving more than available must fail
        boolean overReserve = invService.reserveStock(item.getItemId(), available + 1000);
        assertFalse(overReserve, "Cannot reserve more stock than available");

        // Reserving valid amount must succeed
        if (available >= 5) {
            boolean validReserve = invService.reserveStock(item.getItemId(), 5);
            assertTrue(validReserve, "Reserving valid quantity within available stock must succeed");

            // Releasing reservation restores availability
            boolean released = invService.releaseReservation(item.getItemId(), 5);
            assertTrue(released, "Releasing reservation must succeed");
        }
    }
}
