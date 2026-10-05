package com.floodrelief.model;

/**
 * Node in the Haor disaster logistics graph (Warehouse, Camp, or Waypoint/Hub).
 */
public class RouteNode {
    private String nodeId;
    private String name;
    private String type; // WAREHOUSE, CAMP, WAYPOINT
    private String district;
    private int posX; // X coordinate for visual diagram rendering
    private int posY; // Y coordinate for visual diagram rendering

    public RouteNode() {}

    public RouteNode(String nodeId, String name, String type, String district, int posX, int posY) {
        this.nodeId = nodeId;
        this.name = name;
        this.type = type;
        this.district = district;
        this.posX = posX;
        this.posY = posY;
    }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public int getPosX() { return posX; }
    public void setPosX(int posX) { this.posX = posX; }

    public int getPosY() { return posY; }
    public void setPosY(int posY) { this.posY = posY; }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteNode other)) return false;
        return nodeId != null && nodeId.equals(other.nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId != null ? nodeId.hashCode() : 0;
    }
}
