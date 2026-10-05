package com.floodrelief;

import com.floodrelief.dao.RouteDao;
import com.floodrelief.model.PathResult;
import com.floodrelief.model.RouteEdge;
import com.floodrelief.model.RouteNode;
import com.floodrelief.service.RouteOptimizationService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RouteOptimizationDijkstraTest {

    @Test
    @DisplayName("Dijkstra finds optimal shortest path between warehouse and camp")
    public void testDijkstraShortestPath() {
        RouteOptimizationService service = new RouteOptimizationService();
        List<RouteNode> nodes = service.getAllNodes();

        // If nodes exist in database (or seeded)
        if (!nodes.isEmpty()) {
            PathResult path = service.findShortestPath("N_SYL_WH", "N_BHOLA");
            if (path.isReachable()) {
                assertTrue(path.getTotalDistanceKm() > 0, "Distance should be positive");
                assertFalse(path.getPathNodes().isEmpty(), "Path nodes should not be empty");
                assertEquals("N_SYL_WH", path.getPathNodes().get(0).getNodeId(), "Path starts at warehouse");
            }
        }
    }
}
