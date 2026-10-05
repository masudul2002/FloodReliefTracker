package com.floodrelief.service;

import com.floodrelief.dao.RouteDao;
import com.floodrelief.model.PathResult;
import com.floodrelief.model.RouteEdge;
import com.floodrelief.model.RouteNode;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Route Optimization using Dijkstra's Algorithm on a graph representing
 * Haor disaster road networks, river crossings, and relief camps.
 */
public class RouteOptimizationService {
    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizationService.class);
    private final RouteDao routeDao = new RouteDao();

    public static class NodeDistance implements Comparable<NodeDistance> {
        final String nodeId;
        final double distance;

        public NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public List<RouteNode> getAllNodes() {
        return routeDao.getAllNodes();
    }

    public List<RouteEdge> getAllEdges() {
        return routeDao.getAllEdges();
    }

    public boolean toggleEdgePassability(int edgeId, boolean passable) {
        return routeDao.toggleEdgePassability(edgeId, passable);
    }

    /**
     * Executes Dijkstra's Algorithm to find the shortest passable route from source to destination.
     */
    public PathResult findShortestPath(String startNodeId, String endNodeId) {
        List<RouteNode> allNodes = routeDao.getAllNodes();
        List<RouteEdge> allEdges = routeDao.getAllEdges();

        Map<String, RouteNode> nodeMap = new HashMap<>();
        for (RouteNode n : allNodes) {
            nodeMap.put(n.getNodeId(), n);
        }

        if (!nodeMap.containsKey(startNodeId) || !nodeMap.containsKey(endNodeId)) {
            logger.warn("Invalid start or end node: {} -> {}", startNodeId, endNodeId);
            return new PathResult(false);
        }

        if (startNodeId.equals(endNodeId)) {
            PathResult result = new PathResult(true);
            result.getPathNodes().add(nodeMap.get(startNodeId));
            result.setTotalDistanceKm(0.0);
            result.setEstimatedHours(0.0);
            result.setRecommendedTransport("TRUCK");
            return result;
        }

        // Build adjacency list (bi-directional edges)
        Map<String, List<RouteEdge>> adj = new HashMap<>();
        for (RouteNode n : allNodes) {
            adj.put(n.getNodeId(), new ArrayList<>());
        }

        for (RouteEdge e : allEdges) {
            if (!e.isPassable()) continue; // Skip flooded / impassable roads

            // Forward
            adj.computeIfAbsent(e.getFromNode(), k -> new ArrayList<>()).add(e);
            // Reverse (since routes are bi-directional in emergency response)
            RouteEdge rev = new RouteEdge(e.getToNode(), e.getFromNode(), e.getDistanceKm(), e.getRouteType(), e.isPassable());
            rev.setEdgeId(e.getEdgeId());
            adj.computeIfAbsent(e.getToNode(), k -> new ArrayList<>()).add(rev);
        }

        // Dijkstra's Algorithm with PriorityQueue
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prevNode = new HashMap<>();
        Map<String, RouteEdge> prevEdge = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();

        for (String nid : nodeMap.keySet()) {
            dist.put(nid, Double.POSITIVE_INFINITY);
        }

        dist.put(startNodeId, 0.0);
        pq.offer(new NodeDistance(startNodeId, 0.0));

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance curr = pq.poll();
            String u = curr.nodeId;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u.equals(endNodeId)) break; // Found shortest path to target

            List<RouteEdge> neighbors = adj.getOrDefault(u, Collections.emptyList());
            for (RouteEdge edge : neighbors) {
                String v = edge.getToNode();
                if (visited.contains(v)) continue;

                double newDist = dist.get(u) + edge.getDistanceKm();
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prevNode.put(v, u);
                    prevEdge.put(v, edge);
                    pq.offer(new NodeDistance(v, newDist));
                }
            }
        }

        if (dist.get(endNodeId) == Double.POSITIVE_INFINITY) {
            // Unreachable
            PathResult result = new PathResult(false);
            logger.info("Destination {} unreachable from {} due to impassable edges.", endNodeId, startNodeId);
            return result;
        }

        // Reconstruct path
        LinkedList<RouteNode> pathNodes = new LinkedList<>();
        LinkedList<RouteEdge> pathEdges = new LinkedList<>();
        String curr = endNodeId;
        boolean hasWaterway = false;

        while (curr != null) {
            pathNodes.addFirst(nodeMap.get(curr));
            RouteEdge pe = prevEdge.get(curr);
            if (pe != null) {
                pathEdges.addFirst(pe);
                if ("WATERWAY".equalsIgnoreCase(pe.getRouteType())) {
                    hasWaterway = true;
                }
            }
            curr = prevNode.get(curr);
        }

        PathResult result = new PathResult(true);
        result.setPathNodes(pathNodes);
        result.setPathEdges(pathEdges);
        result.setTotalDistanceKm(Math.round(dist.get(endNodeId) * 10.0) / 10.0);

        // Estimate time: Road average 32 km/h, Waterway average 16 km/h
        double roadKm = 0.0;
        double waterKm = 0.0;
        for (RouteEdge edge : pathEdges) {
            if ("WATERWAY".equalsIgnoreCase(edge.getRouteType())) {
                waterKm += edge.getDistanceKm();
            } else {
                roadKm += edge.getDistanceKm();
            }
        }
        double estHours = (roadKm / 32.0) + (waterKm / 16.0);
        result.setEstimatedHours(Math.round(estHours * 10.0) / 10.0);

        if (hasWaterway && roadKm > 0) {
            result.setRecommendedTransport("MULTI-MODAL (TRUCK + TRAWLER BOAT)");
        } else if (hasWaterway) {
            result.setRecommendedTransport("TRAWLER BOAT / SPEEDBOAT");
        } else {
            result.setRecommendedTransport("RELIEF TRUCK");
        }

        return result;
    }
}
