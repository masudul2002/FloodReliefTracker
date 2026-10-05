package com.floodrelief;

import com.floodrelief.model.Camp;
import com.floodrelief.model.SeverityLabel;
import com.floodrelief.service.PriorityEngine;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PriorityEngineTest {

    @Test
    @DisplayName("Severity score calculation produces values between 0 and 100")
    public void testSeverityScoreBounds() {
        PriorityEngine engine = new PriorityEngine();

        // Low urgency camp: small population, 1 day waterlogged, 0 medical, relieved yesterday
        Camp lowCamp = new Camp("C-LOW", "Low Camp", "Sylhet", "Area 1",
                100, 500, 1, 0, LocalDateTime.now().minusDays(1));
        double lowScore = engine.calculateSeverityScore(lowCamp);
        assertTrue(lowScore >= 0.0 && lowScore < 35.0, "Score should be in Low range: " + lowScore);

        // Critical urgency camp: high population, 20 days waterlogged, acute medical cases, 10 days no relief
        Camp critCamp = new Camp("C-CRIT", "Critical Camp", "Sunamganj", "Area 2",
                2500, 1500, 20, 80, LocalDateTime.now().minusDays(10));
        double critScore = engine.calculateSeverityScore(critCamp);
        assertTrue(critScore >= 80.0 && critScore <= 100.0, "Score should be in Critical range: " + critScore);
        assertEquals(SeverityLabel.CRITICAL, SeverityLabel.fromScore(critScore));
    }

    @Test
    @DisplayName("PriorityQueue triages camps with highest urgency first")
    public void testPriorityQueueTriage() {
        PriorityEngine engine = new PriorityEngine();

        Camp c1 = new Camp("C1", "Moderate Camp", "Sylhet", "Area 1", 500, 500, 5, 5, LocalDateTime.now().minusDays(2));
        c1.setSeverityScore(engine.calculateSeverityScore(c1));

        Camp c2 = new Camp("C2", "Extremely Urgency Camp", "Sunamganj", "Area 2", 2400, 2000, 18, 70, LocalDateTime.now().minusDays(9));
        c2.setSeverityScore(engine.calculateSeverityScore(c2));

        Camp c3 = new Camp("C3", "Low Urgency Camp", "Sylhet", "Area 3", 150, 400, 1, 0, LocalDateTime.now().minusDays(1));
        c3.setSeverityScore(engine.calculateSeverityScore(c3));

        PriorityQueue<Camp> pq = new PriorityQueue<>(PriorityEngine.URGENCY_COMPARATOR);
        pq.offer(c1);
        pq.offer(c2);
        pq.offer(c3);

        Camp top = pq.poll();
        assertNotNull(top);
        assertEquals("C2", top.getCampId(), "Highest urgency camp C2 must be dequeued first");

        Camp second = pq.poll();
        assertNotNull(second);
        assertEquals("C1", second.getCampId(), "Second urgency camp C1 must be dequeued second");

        Camp third = pq.poll();
        assertNotNull(third);
        assertEquals("C3", third.getCampId(), "Lowest urgency camp C3 must be dequeued last");
    }
}
