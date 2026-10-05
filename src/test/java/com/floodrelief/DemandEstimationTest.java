package com.floodrelief;

import com.floodrelief.model.Camp;
import com.floodrelief.service.DemandEstimationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DemandEstimationTest {

    @Test
    @DisplayName("Sphere Humanitarian standards calculates 15L water and food metrics correctly")
    public void testDemandCalculation() {
        DemandEstimationService service = new DemandEstimationService();
        Camp camp = new Camp("C-TEST", "Test Camp", "Sylhet", "Gowainghat", 1000, 1000, 5, 20, null);

        int days = 7;
        DemandEstimationService.CampDemandReport report = service.calculateDemand(camp, days);

        assertNotNull(report);
        assertEquals(1000, report.getPopulation());
        assertEquals(days, report.getDays());

        // 1000 people * 15 liters * 7 days = 105,000 liters
        assertEquals(105000.0, report.getTotalWaterLiters(), 0.01, "Water must match 15L/person/day standard");

        // 1000 people * 0.400 kg * 7 days = 2800 kg rice
        assertEquals(2800.0, report.getTotalRiceKg(), 0.01, "Rice must match 400g/person/day");

        // 1000 people * 3 tablets * 7 days = 21,000 purification tablets
        assertEquals(21000, report.getTotalWaterPurificationTablets());

        // ORS should be positive and include medical cases
        assertTrue(report.getTotalOrsSachets() > 0);
    }
}
