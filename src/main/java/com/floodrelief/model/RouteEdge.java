package com.floodrelief.model;

/**
 * Weighted edge between route nodes in Haor region disaster road/waterway graph.
 */
public class RouteEdge {
    private int edgeId;
    private String fromNode;
    private String toNode;
    private double distanceKm;
    private String routeType; // ROAD, WATERWAY
    private boolean isPassable;

    public RouteEdge() {
        this.routeType = "ROAD";
        this.isPassable = true;
    }

    public RouteEdge(String fromNode, String toNode, double distanceKm, String routeType, boolean isPassable) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.distanceKm = distanceKm;
        this.routeType = routeType;
        this.isPassable = isPassable;
    }

    public int getEdgeId() { return edgeId; }
    public void setEdgeId(int edgeId) { this.edgeId = edgeId; }

    public String getFromNode() { return fromNode; }
    public void setFromNode(String fromNode) { this.fromNode = fromNode; }

    public String getToNode() { return toNode; }
    public void setToNode(String toNode) { this.toNode = toNode; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getRouteType() { return routeType; }
    public void setRouteType(String routeType) { this.routeType = routeType; }

    public boolean isPassable() { return isPassable; }
    public void setPassable(boolean passable) { isPassable = passable; }

    @Override
    public String toString() {
        return fromNode + " <-> " + toNode + " (" + distanceKm + " km, " + routeType + (isPassable ? "" : " [BLOCKED]") + ")";
    }
}
