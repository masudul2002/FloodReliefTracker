package com.floodrelief.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of Dijkstra's algorithm shortest path computation.
 */
public class PathResult {
    private List<RouteNode> pathNodes = new ArrayList<>();
    private List<RouteEdge> pathEdges = new ArrayList<>();
    private double totalDistanceKm;
    private double estimatedHours;
    private String recommendedTransport;
    private boolean reachable;

    public PathResult(boolean reachable) {
        this.reachable = reachable;
    }

    public List<RouteNode> getPathNodes() { return pathNodes; }
    public void setPathNodes(List<RouteNode> pathNodes) { this.pathNodes = pathNodes; }

    public List<RouteEdge> getPathEdges() { return pathEdges; }
    public void setPathEdges(List<RouteEdge> pathEdges) { this.pathEdges = pathEdges; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getRecommendedTransport() { return recommendedTransport; }
    public void setRecommendedTransport(String recommendedTransport) { this.recommendedTransport = recommendedTransport; }

    public boolean isReachable() { return reachable; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }

    public String getFormattedPath() {
        if (!reachable || pathNodes.isEmpty()) return "No reachable route found";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathNodes.size(); i++) {
            sb.append(pathNodes.get(i).getName());
            if (i < pathNodes.size() - 1) {
                sb.append(" ➔ ");
            }
        }
        return sb.toString();
    }
}
