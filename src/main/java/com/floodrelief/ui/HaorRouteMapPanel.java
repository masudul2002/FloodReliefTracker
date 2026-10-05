package com.floodrelief.ui;

import com.floodrelief.model.PathResult;
import com.floodrelief.model.RouteEdge;
import com.floodrelief.model.RouteNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Interactive 2D graphical map visualization for the Haor logistics network.
 * Located in com.floodrelief.ui as per architectural structure.
 */
public class HaorRouteMapPanel extends JPanel {
    private List<RouteNode> nodes = new ArrayList<>();
    private List<RouteEdge> edges = new ArrayList<>();
    private PathResult highlightedPath = null;
    private RouteNode hoveredNode = null;

    public HaorRouteMapPanel() {
        setBackground(new Color(245, 248, 252));
        setPreferredSize(new Dimension(650, 420));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 230), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                RouteNode found = null;
                for (RouteNode n : nodes) {
                    int dx = n.getPosX() - e.getX();
                    int dy = n.getPosY() - e.getY();
                    if (dx * dx + dy * dy <= 225) { // Radius 15
                        found = n;
                        break;
                    }
                }
                if (found != hoveredNode) {
                    hoveredNode = found;
                    setToolTipText(found != null ? found.getName() + " (" + found.getType() + " - " + found.getDistrict() + ")" : null);
                    repaint();
                }
            }
        });
    }

    public void setGraphData(List<RouteNode> nodes, List<RouteEdge> edges) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        repaint();
    }

    public void setHighlightedPath(PathResult path) {
        this.highlightedPath = path;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw Water body background decor (Haor wetlands hint)
        g2.setColor(new Color(225, 238, 248));
        g2.fillRoundRect(50, 40, 280, 160, 40, 40); // Sunamganj Haor Basin
        g2.drawString("🌊 Tanguar & Hakaluki Haor Basin (Monsoon Inundation Zone)", 60, 60);

        Map<String, RouteNode> nodeMap = new HashMap<>();
        for (RouteNode n : nodes) {
            nodeMap.put(n.getNodeId(), n);
        }

        // 1. Draw Normal Edges
        for (RouteEdge edge : edges) {
            RouteNode from = nodeMap.get(edge.getFromNode());
            RouteNode to = nodeMap.get(edge.getToNode());
            if (from == null || to == null) continue;

            boolean isPathEdge = isEdgeOnHighlightedPath(edge);

            if (!isPathEdge) {
                if (!edge.isPassable()) {
                    // Flooded / Blocked edge
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f));
                    g2.setColor(new Color(220, 53, 69, 180));
                    g2.draw(new Line2D.Double(from.getPosX(), from.getPosY(), to.getPosX(), to.getPosY()));

                    int midX = (from.getPosX() + to.getPosX()) / 2;
                    int midY = (from.getPosY() + to.getPosY()) / 2;
                    g2.setColor(new Color(220, 53, 69));
                    g2.drawString("✖ FLOODED", midX - 25, midY);
                } else if ("WATERWAY".equalsIgnoreCase(edge.getRouteType())) {
                    // Waterway / Boat edge
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 4.0f}, 0.0f));
                    g2.setColor(new Color(0, 150, 200, 160));
                    g2.draw(new Line2D.Double(from.getPosX(), from.getPosY(), to.getPosX(), to.getPosY()));
                } else {
                    // Standard Road edge
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.setColor(new Color(160, 175, 190));
                    g2.draw(new Line2D.Double(from.getPosX(), from.getPosY(), to.getPosX(), to.getPosY()));
                }

                // Distance label
                if (edge.isPassable()) {
                    int midX = (from.getPosX() + to.getPosX()) / 2;
                    int midY = (from.getPosY() + to.getPosY()) / 2;
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.setColor(new Color(100, 115, 130));
                    g2.drawString(String.format("%.0fkm", edge.getDistanceKm()), midX + 3, midY - 3);
                }
            }
        }

        // 2. Draw Highlighted Path Edges (Glowing green Dijkstra route)
        if (highlightedPath != null && highlightedPath.isReachable() && highlightedPath.getPathNodes().size() > 1) {
            List<RouteNode> pNodes = highlightedPath.getPathNodes();
            for (int i = 0; i < pNodes.size() - 1; i++) {
                RouteNode u = pNodes.get(i);
                RouteNode v = pNodes.get(i + 1);

                // Outer glow
                g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(40, 167, 69, 70));
                g2.draw(new Line2D.Double(u.getPosX(), u.getPosY(), v.getPosX(), v.getPosY()));

                // Core vibrant path line
                g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(40, 167, 69));
                g2.draw(new Line2D.Double(u.getPosX(), u.getPosY(), v.getPosX(), v.getPosY()));
            }
        }

        // 3. Draw Nodes
        for (RouteNode n : nodes) {
            boolean isStart = highlightedPath != null && !highlightedPath.getPathNodes().isEmpty() &&
                    highlightedPath.getPathNodes().get(0).getNodeId().equals(n.getNodeId());
            boolean isEnd = highlightedPath != null && !highlightedPath.getPathNodes().isEmpty() &&
                    highlightedPath.getPathNodes().get(highlightedPath.getPathNodes().size() - 1).getNodeId().equals(n.getNodeId());
            boolean onPath = isNodeOnHighlightedPath(n);

            int x = n.getPosX();
            int y = n.getPosY();

            if ("WAREHOUSE".equalsIgnoreCase(n.getType())) {
                // Warehouse: Square
                g2.setColor(isStart ? new Color(13, 110, 253) : new Color(30, 144, 255));
                g2.fillRoundRect(x - 12, y - 12, 24, 24, 6, 6);
                g2.setColor(Color.WHITE);
                g2.drawString("🏬", x - 7, y + 5);
            } else if ("CAMP".equalsIgnoreCase(n.getType())) {
                // Camp: Circle
                g2.setColor(isEnd ? new Color(220, 53, 69) : onPath ? new Color(255, 150, 0) : new Color(240, 80, 80));
                g2.fillOval(x - 12, y - 12, 24, 24);
                g2.setColor(Color.WHITE);
                g2.drawString("⛺", x - 7, y + 5);
            } else {
                // Waypoint: Diamond
                g2.setColor(new Color(108, 117, 125));
                int[] px = {x, x + 8, x, x - 8};
                int[] py = {y - 8, y, y + 8, y};
                g2.fillPolygon(px, py, 4);
            }

            // Node ring if hovered or on path
            if (n == hoveredNode || isStart || isEnd) {
                g2.setColor(isStart ? new Color(13, 110, 253) : isEnd ? new Color(220, 53, 69) : new Color(40, 167, 69));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x - 16, y - 16, 32, 32);
            }

            // Node Title Label
            g2.setFont(new Font("Segoe UI", (isStart || isEnd) ? Font.BOLD : Font.PLAIN, 11));
            g2.setColor(isStart || isEnd ? Color.BLACK : new Color(60, 70, 80));
            g2.drawString(n.getName(), x + 15, y + 4);
        }

        // Map Legend
        drawLegend(g2);

        g2.dispose();
    }

    private void drawLegend(Graphics2D g2) {
        int lx = 15;
        int ly = getHeight() - 55;

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(lx, ly, 380, 45, 8, 8);
        g2.setColor(new Color(200, 210, 220));
        g2.drawRoundRect(lx, ly, 380, 45, 8, 8);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(Color.DARK_GRAY);

        g2.drawString("🏬 Warehouse   ⛺ Camp   ◆ Waypoint", lx + 10, ly + 18);
        g2.drawString("━ Road   ┄ Boat Waterway   ━ Optimal Dijkstra Path", lx + 10, ly + 36);
    }

    private boolean isNodeOnHighlightedPath(RouteNode node) {
        if (highlightedPath == null || !highlightedPath.isReachable()) return false;
        for (RouteNode n : highlightedPath.getPathNodes()) {
            if (n.getNodeId().equals(node.getNodeId())) return true;
        }
        return false;
    }

    private boolean isEdgeOnHighlightedPath(RouteEdge edge) {
        if (highlightedPath == null || !highlightedPath.isReachable()) return false;
        List<RouteNode> pNodes = highlightedPath.getPathNodes();
        for (int i = 0; i < pNodes.size() - 1; i++) {
            String u = pNodes.get(i).getNodeId();
            String v = pNodes.get(i + 1).getNodeId();
            if ((edge.getFromNode().equals(u) && edge.getToNode().equals(v)) ||
                (edge.getFromNode().equals(v) && edge.getToNode().equals(u))) {
                return true;
            }
        }
        return false;
    }
}
