package com.floodrelief.ui;

import com.floodrelief.MainApp;
import com.floodrelief.config.AuthContext;
import com.floodrelief.dao.SystemConfigDao;
import com.floodrelief.model.*;
import com.floodrelief.service.*;
import com.floodrelief.ui.HaorRouteMapPanel;
import com.floodrelief.ui.components.BadgeRenderer;
import com.floodrelief.ui.components.CardPanel;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application Dashboard Frame for FloodReliefTracker.
 * Integrates all 10 modules:
 * 1. Cloud Database Connectivity (Neon PostgreSQL)
 * 2. Role-Based Access Control (RBAC)
 * 3. Camp Registration & Haor Region Management
 * 4. Priority Engine & Severity Scoring
 * 5. Concurrency-Safe Inventory Management
 * 6. Shipment Lifecycle & Logistics Tracking
 * 7. Duplicate Relief Prevention & Family Registry
 * 8. Route Optimization (Dijkstra's Algorithm)
 * 9. Sphere Demand Estimation & Automated Alert Monitoring
 * 10. Modern FlatLaf UI, Indicator Cards & CSV Export
 */
public class MainDashboardFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainDashboardFrame.class);

    private final CampService campService = new CampService();
    private final PriorityEngine priorityEngine = new PriorityEngine();
    private final InventoryService inventoryService = new InventoryService();
    private final ShipmentService shipmentService = new ShipmentService();
    private final DistributionService distributionService = new DistributionService();
    private final RouteOptimizationService routeService = new RouteOptimizationService();
    private final DemandEstimationService demandService = new DemandEstimationService();
    private final AlertMonitoringService alertService = AlertMonitoringService.getInstance();
    private final AuthService authService = new AuthService();
    private final SystemConfigDao configDao = new SystemConfigDao();

    // Top Header Components
    private JLabel lblCurrentUser;
    private JLabel lblUserRole;
    private JLabel lblDbStatus;

    // Navigation & Tabs
    private JTabbedPane tabbedPane;

    // Overview Components
    private CardPanel cardCamps;
    private CardPanel cardFamilies;
    private CardPanel cardInventory;
    private CardPanel cardShipments;
    private CardPanel cardCritical;
    private CardPanel cardAlerts;
    private JTable tblTriageOverview;
    private DefaultTableModel modelTriageOverview;

    // Camps Tab Components
    private JTable tblCamps;
    private DefaultTableModel modelCamps;
    private JTextField txtSearchCamp;
    private JComboBox<String> cmbDistrictFilter;
    private JComboBox<String> cmbSeverityFilter;

    // Families Tab Components
    private JTable tblFamilies;
    private DefaultTableModel modelFamilies;
    private JTextField txtSearchFamily;

    // Inventory Tab Components
    private JTable tblInventory;
    private DefaultTableModel modelInventory;

    // Shipments Tab Components
    private JTable tblShipments;
    private DefaultTableModel modelShipments;

    // Distribution Tab Components
    private JComboBox<Family> cmbDistFamily;
    private JComboBox<ItemCategory> cmbDistCategory;
    private JComboBox<ReliefItem> cmbDistItem;
    private JSpinner spinDistQty;
    private JLabel lblEligibilityResult;
    private JCheckBox chkOverride;
    private JTextField txtOverrideReason;
    private JTable tblDistributions;
    private DefaultTableModel modelDistributions;

    // Route Tab Components
    private JComboBox<RouteNode> cmbRouteSource;
    private JComboBox<RouteNode> cmbRouteTarget;
    private JLabel lblRouteDetails;
    private HaorRouteMapPanel mapPanel;
    private JTable tblEdges;
    private DefaultTableModel modelEdges;

    // Demand Estimation Tab Components
    private JComboBox<Camp> cmbDemandCamp;
    private JComboBox<String> cmbDemandDays;
    private JTable tblDemandMetrics;
    private DefaultTableModel modelDemandMetrics;

    // Alerts Tab Components
    private JTable tblAlerts;
    private DefaultTableModel modelAlerts;

    // Settings Tab Components
    private JTable tblUsers;
    private DefaultTableModel modelUsers;
    private JSpinner spinWeightPop;
    private JSpinner spinWeightWater;
    private JSpinner spinWeightMed;
    private JSpinner spinWeightRelief;
    private JSpinner spinCooldownDays;

    public MainDashboardFrame() {
        super("FloodReliefTracker - Haor Basin Disaster Supply Chain [Neon Cloud Connected]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        initComponents();
        setupAlertListener();
        refreshAllData();
        applyRbacPermissions();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 0));

        // 1. Top Header Bar
        JPanel header = createHeaderBar();
        root.add(header, BorderLayout.NORTH);

        // 2. Main Tabbed Content Area
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabbedPane.addTab("📊 Overview", createOverviewPanel());
        tabbedPane.addTab("⛺ Camps & Haor", createCampsPanel());
        tabbedPane.addTab("👨‍👩‍👧 Beneficiaries", createFamiliesPanel());
        tabbedPane.addTab("📦 Inventory", createInventoryPanel());
        tabbedPane.addTab("🚚 Shipments", createShipmentsPanel());
        tabbedPane.addTab("🤝 Relief Aid", createDistributionPanel());
        tabbedPane.addTab("🗺️ Route Planner", createRoutePanel());
        tabbedPane.addTab("📈 Demand Estimate", createDemandPanel());
        tabbedPane.addTab("⚠️ System Alerts", createAlertsPanel());
        tabbedPane.addTab("⚙️ Settings & RBAC", createSettingsPanel());

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 225)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel lblLogo = new JLabel("🌊 FloodReliefTracker");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblLogo.setForeground(new Color(13, 110, 253));

        JLabel lblTag = new JLabel("Haor Basin Response (Sylhet & Sunamganj)");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTag.setForeground(Color.GRAY);

        brandPanel.add(lblLogo);
        brandPanel.add(new JSeparator(SwingConstants.VERTICAL));
        brandPanel.add(lblTag);

        // Right User & Status Area
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        lblDbStatus = new JLabel(DatabaseManager.isConnected() ? "🟢 Neon Cloud DB" : "🔴 DB Offline");
        lblDbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDbStatus.setForeground(DatabaseManager.isConnected() ? new Color(40, 167, 69) : Color.RED);

        User cur = AuthContext.getCurrentUser();
        lblCurrentUser = new JLabel(cur != null ? "👤 " + cur.getFullName() : "Guest");
        lblCurrentUser.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblUserRole = new JLabel(cur != null ? "[" + cur.getRole().getDisplayName() + "]" : "");
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUserRole.setForeground(new Color(13, 110, 253));

        JButton btnTheme = new JButton(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        btnTheme.setFocusPainted(false);
        btnTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(this);
            btnTheme.setText(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        });

        JButton btnLogout = new JButton("🚪 Sign Out");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> performLogout());

        rightPanel.add(lblDbStatus);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightPanel.add(lblCurrentUser);
        rightPanel.add(lblUserRole);
        rightPanel.add(btnTheme);
        rightPanel.add(btnLogout);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 1. OVERVIEW DASHBOARD PANEL
    // ==========================================
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Metric Indicator Cards
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        cardCamps = new CardPanel("⛺", "TOTAL CAMPS", "0", "Monsoon relief centers", new Color(13, 110, 253));
        cardFamilies = new CardPanel("👨‍👩‍👧", "REGISTERED FAMILIES", "0", "Vulnerable beneficiaries", new Color(40, 167, 69));
        cardInventory = new CardPanel("📦", "AVAILABLE SUPPLIES", "0", "Food, WASH, & medical packs", new Color(23, 162, 184));
        cardShipments = new CardPanel("🚚", "ACTIVE SHIPMENTS", "0", "En-route supply convoys", new Color(255, 193, 7));
        cardCritical = new CardPanel("🚨", "CRITICAL URGENCY CAMPS", "0", "Immediate triage priority", new Color(220, 53, 69));
        cardAlerts = new CardPanel("⚠️", "ACTIVE SYSTEM ALERTS", "0", "Stock, ETA & overflow alerts", new Color(253, 126, 20));

        cardsGrid.add(cardCamps);
        cardsGrid.add(cardFamilies);
        cardsGrid.add(cardInventory);
        cardsGrid.add(cardShipments);
        cardsGrid.add(cardCritical);
        cardsGrid.add(cardAlerts);

        // Priority Triage Camps Table
        JPanel triagePanel = new JPanel(new BorderLayout(0, 8));
        JLabel lblTriageTitle = new JLabel("🎯 Dynamic Priority Queue - Camps Sorted by Urgency (Priority Engine Model)");
        lblTriageTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] cols = {"Camp ID", "Camp Name", "District", "Area", "Population", "Waterlogged Days", "Medical Need", "Days No Relief", "Severity Score", "Urgency Status"};
        modelTriageOverview = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTriageOverview = new JTable(modelTriageOverview);
        tblTriageOverview.getColumnModel().getColumn(9).setCellRenderer(new BadgeRenderer());
        tblTriageOverview.setRowHeight(28);

        triagePanel.add(lblTriageTitle, BorderLayout.NORTH);
        triagePanel.add(new JScrollPane(tblTriageOverview), BorderLayout.CENTER);

        // Bottom Controls
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnRefresh = new JButton("🔄 Refresh All Data");
        btnRefresh.addActionListener(e -> refreshAllData());
        btnPanel.add(btnRefresh);

        panel.add(cardsGrid, BorderLayout.NORTH);
        panel.add(triagePanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 2. CAMPS & HAOR REGIONS PANEL
    // ==========================================
    private JPanel createCampsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        txtSearchCamp = new JTextField(14);
        cmbDistrictFilter = new JComboBox<>(new String[]{"All", "Sylhet", "Sunamganj"});
        cmbSeverityFilter = new JComboBox<>(new String[]{"All", "CRITICAL", "HIGH", "MEDIUM", "LOW"});

        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.addActionListener(e -> searchCamps());

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearchCamp.setText("");
            cmbDistrictFilter.setSelectedIndex(0);
            cmbSeverityFilter.setSelectedIndex(0);
            loadCampsTable();
        });

        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearchCamp);
        filterBar.add(new JLabel("District:"));
        filterBar.add(cmbDistrictFilter);
        filterBar.add(new JLabel("Severity:"));
        filterBar.add(cmbSeverityFilter);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);

        // Camps Table
        String[] cols = {"Camp ID", "Camp Name", "District", "Area", "Pop.", "Cap.", "Occ. %", "Waterlogged", "Medical", "Last Relief", "Score", "Status"};
        modelCamps = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCamps = new JTable(modelCamps);
        tblCamps.getColumnModel().getColumn(11).setCellRenderer(new BadgeRenderer());
        tblCamps.setRowHeight(28);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnAddCamp = new JButton("➕ Register New Camp");
        btnAddCamp.addActionListener(e -> showRegisterCampDialog());

        JButton btnReliefArrived = new JButton("📦 Record Relief Arrival");
        btnReliefArrived.addActionListener(e -> recordCampReliefArrival());

        JButton btnRecalc = new JButton("⚡ Recalculate Priorities");
        btnRecalc.addActionListener(e -> {
            loadCampsTable();
            JOptionPane.showMessageDialog(this, "Priorities recalculated using 4-factor normalization formula.", "Priority Engine", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnExport = new JButton("📥 Export CSV");
        btnExport.addActionListener(e -> exportCampsCsv());

        actionPanel.add(btnAddCamp);
        actionPanel.add(btnReliefArrived);
        actionPanel.add(btnRecalc);
        actionPanel.add(btnExport);

        panel.add(filterBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblCamps), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 3. FAMILIES / BENEFICIARIES PANEL
    // ==========================================
    private JPanel createFamiliesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        txtSearchFamily = new JTextField(16);
        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.addActionListener(e -> loadFamiliesTable());

        topBar.add(new JLabel("Search Household Head / ID:"));
        topBar.add(txtSearchFamily);
        topBar.add(btnSearch);

        String[] cols = {"Family ID", "Camp ID", "Camp Name", "Household Head", "Member Count", "Vulnerability Details", "Registered At"};
        modelFamilies = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblFamilies = new JTable(modelFamilies);
        tblFamilies.setRowHeight(28);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnAddFamily = new JButton("➕ Register Family");
        btnAddFamily.addActionListener(e -> showRegisterFamilyDialog());

        JButton btnExport = new JButton("📥 Export CSV");
        btnExport.addActionListener(e -> exportFamiliesCsv());

        btnPanel.add(btnAddFamily);
        btnPanel.add(btnExport);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblFamilies), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 4. INVENTORY MANAGEMENT PANEL
    // ==========================================
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel("📦 Warehouse Relief Inventory (Concurrency Synchronized & Row-Locked)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] cols = {"Item ID", "Item Name", "Category", "Unit", "Warehouse", "Stock-In", "Reserved", "Dispatched", "Damaged", "Expired", "Available", "Low Stock?"};
        modelInventory = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblInventory = new JTable(modelInventory);
        tblInventory.getColumnModel().getColumn(11).setCellRenderer(new BadgeRenderer());
        tblInventory.setRowHeight(28);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnStockIn = new JButton("➕ Stock-In");
        btnStockIn.addActionListener(e -> showStockInDialog());

        JButton btnDamage = new JButton("⚠️ Record Damage");
        btnDamage.addActionListener(e -> showDamageDialog());

        JButton btnExpire = new JButton("⌛ Record Expired");
        btnExpire.addActionListener(e -> showExpiredDialog());

        JButton btnNewItem = new JButton("✨ New Item Type");
        btnNewItem.addActionListener(e -> showNewItemDialog());

        JButton btnExport = new JButton("📥 Export CSV");
        btnExport.addActionListener(e -> exportInventoryCsv());

        btnPanel.add(btnStockIn);
        btnPanel.add(btnDamage);
        btnPanel.add(btnExpire);
        btnPanel.add(btnNewItem);
        btnPanel.add(btnExport);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblInventory), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 5. SHIPMENTS & SUPPLY CHAIN PANEL
    // ==========================================
    private JPanel createShipmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel("🚚 Emergency Relief Shipments Lifecycle (Pending ➔ Packed ➔ Dispatched ➔ In Transit ➔ Delivered)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] cols = {"Shipment ID", "Origin Warehouse", "Destination Camp", "Status", "Transport Mode", "Driver Contact", "Expected ETA", "Delivered At", "Manager"};
        modelShipments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblShipments = new JTable(modelShipments);
        tblShipments.getColumnModel().getColumn(3).setCellRenderer(new BadgeRenderer());
        tblShipments.setRowHeight(28);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnNewShipment = new JButton("➕ New Shipment");
        btnNewShipment.addActionListener(e -> showNewShipmentDialog());

        JButton btnAdvanceStatus = new JButton("⏩ Advance Status");
        btnAdvanceStatus.addActionListener(e -> advanceShipmentStatus());

        JButton btnCancel = new JButton("❌ Cancel Shipment");
        btnCancel.addActionListener(e -> cancelShipment());

        JButton btnExport = new JButton("📥 Export CSV");
        btnExport.addActionListener(e -> exportShipmentsCsv());

        btnPanel.add(btnNewShipment);
        btnPanel.add(btnAdvanceStatus);
        btnPanel.add(btnCancel);
        btnPanel.add(btnExport);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblShipments), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 6. RELIEF DISTRIBUTION & DUPLICATE PREVENTION
    // ==========================================
    private JPanel createDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top: Distribution Form with Duplicate Check
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBorder(BorderFactory.createTitledBorder("Log Relief Assistance (Automated Duplicate Check & Cooldown Window)"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        cmbDistFamily = new JComboBox<>();
        cmbDistCategory = new JComboBox<>(ItemCategory.values());
        cmbDistItem = new JComboBox<>();
        spinDistQty = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        lblEligibilityResult = new JLabel("Select family and category to check eligibility.");
        lblEligibilityResult.setFont(new Font("Segoe UI", Font.BOLD, 12));

        chkOverride = new JCheckBox("Coordinator / Admin Override");
        txtOverrideReason = new JTextField(20);
        txtOverrideReason.setEnabled(false);

        chkOverride.addActionListener(e -> txtOverrideReason.setEnabled(chkOverride.isSelected()));

        JButton btnCheck = new JButton("🔍 Check Eligibility");
        btnCheck.addActionListener(e -> runDuplicateCheck());

        JButton btnDistribute = new JButton("✅ Confirm Aid Distribution");
        btnDistribute.setBackground(new Color(40, 167, 69));
        btnDistribute.setForeground(Color.WHITE);
        btnDistribute.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDistribute.addActionListener(e -> executeDistribution());

        // Fill grid
        g.gridx = 0; g.gridy = 0; fields.add(new JLabel("Beneficiary Family:"), g);
        g.gridx = 1; fields.add(cmbDistFamily, g);
        g.gridx = 2; fields.add(new JLabel("Aid Category:"), g);
        g.gridx = 3; fields.add(cmbDistCategory, g);

        g.gridx = 0; g.gridy = 1; fields.add(new JLabel("Relief Item:"), g);
        g.gridx = 1; fields.add(cmbDistItem, g);
        g.gridx = 2; fields.add(new JLabel("Quantity:"), g);
        g.gridx = 3; fields.add(spinDistQty, g);

        g.gridx = 0; g.gridy = 2; fields.add(btnCheck, g);
        g.gridx = 1; g.gridwidth = 3; fields.add(lblEligibilityResult, g);
        g.gridwidth = 1;

        g.gridx = 0; g.gridy = 3; fields.add(chkOverride, g);
        g.gridx = 1; g.gridwidth = 2; fields.add(txtOverrideReason, g);
        g.gridwidth = 1;
        g.gridx = 3; fields.add(btnDistribute, g);

        formCard.add(fields, BorderLayout.CENTER);

        // Bottom: Distribution History Table
        JPanel historyPanel = new JPanel(new BorderLayout(0, 8));
        JLabel lblHistory = new JLabel("📜 Distribution History Records");
        lblHistory.setFont(new Font("Segoe UI", Font.BOLD, 13));

        String[] cols = {"Record ID", "Family ID", "Head Name", "Camp", "Category", "Item", "Qty", "Date & Time", "Recorded By", "Override?", "Override Reason"};
        modelDistributions = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDistributions = new JTable(modelDistributions);
        tblDistributions.setRowHeight(26);

        JButton btnExport = new JButton("📥 Export Distributions CSV");
        btnExport.addActionListener(e -> exportDistributionsCsv());
        JPanel histBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        histBottom.add(btnExport);

        historyPanel.add(lblHistory, BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(tblDistributions), BorderLayout.CENTER);
        historyPanel.add(histBottom, BorderLayout.SOUTH);

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(historyPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 7. ROUTE OPTIMIZATION (DIJKSTRA)
    // ==========================================
    private JPanel createRoutePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top Control
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        cmbRouteSource = new JComboBox<>();
        cmbRouteTarget = new JComboBox<>();
        JButton btnDijkstra = new JButton("🚀 Compute Shortest Route (Dijkstra)");
        btnDijkstra.setBackground(new Color(13, 110, 253));
        btnDijkstra.setForeground(Color.WHITE);
        btnDijkstra.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDijkstra.addActionListener(e -> calculateShortestRoute());

        top.add(new JLabel("Origin Hub:"));
        top.add(cmbRouteSource);
        top.add(new JLabel("Destination Shelter:"));
        top.add(cmbRouteTarget);
        top.add(btnDijkstra);

        // Center split: Map on left, Edges / Flood Simulation on right
        mapPanel = new HaorRouteMapPanel();

        JPanel rightSide = new JPanel(new BorderLayout(0, 8));
        lblRouteDetails = new JLabel("<html><b>Route Plan:</b> Select origin and destination, then click compute.</html>");
        lblRouteDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRouteDetails.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Edge Passability table (Simulate monsoon road flooding)
        String[] cols = {"Edge ID", "From", "To", "Dist (km)", "Mode", "Passable?"};
        modelEdges = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEdges = new JTable(modelEdges);
        tblEdges.setRowHeight(24);

        JButton btnToggleBlocked = new JButton("🌊 Toggle Road Flooded / Open");
        btnToggleBlocked.addActionListener(e -> toggleSelectedEdgePassability());

        JPanel edgeBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        edgeBtnPanel.add(btnToggleBlocked);

        rightSide.add(lblRouteDetails, BorderLayout.NORTH);
        rightSide.add(new JScrollPane(tblEdges), BorderLayout.CENTER);
        rightSide.add(edgeBtnPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, rightSide);
        split.setResizeWeight(0.65);

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 8. SPHERE DEMAND ESTIMATION PANEL
    // ==========================================
    private JPanel createDemandPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        cmbDemandCamp = new JComboBox<>();
        cmbDemandDays = new JComboBox<>(new String[]{"3 Days (Immediate Emergency)", "7 Days (Standard Rations)", "14 Days (Extended Recovery)"});

        JButton btnCalc = new JButton("📊 Compute Rations (Sphere Standards)");
        btnCalc.setBackground(new Color(13, 110, 253));
        btnCalc.setForeground(Color.WHITE);
        btnCalc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCalc.addActionListener(e -> computeDemandMetrics());

        top.add(new JLabel("Select Camp:"));
        top.add(cmbDemandCamp);
        top.add(new JLabel("Relief Horizon:"));
        top.add(cmbDemandDays);
        top.add(btnCalc);

        String[] cols = {"Relief Metric (Sphere Humanitarian Standard)", "Calculated Supply Requirement"};
        modelDemandMetrics = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDemandMetrics = new JTable(modelDemandMetrics);
        tblDemandMetrics.setRowHeight(32);
        tblDemandMetrics.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblDemandMetrics), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 9. ALERTS & INCIDENT MONITORING PANEL
    // ==========================================
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel("⚠️ Automated Incident Alerts (Low Stock, Medical Expiry, Delayed ETAs, Shelter Overflow)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] cols = {"Alert ID", "Severity", "Incident Type", "Message Details", "Timestamp", "Status"};
        modelAlerts = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAlerts = new JTable(modelAlerts);
        tblAlerts.getColumnModel().getColumn(1).setCellRenderer(new BadgeRenderer());
        tblAlerts.setRowHeight(28);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnScanNow = new JButton("🔍 Run Alert Scan Now");
        btnScanNow.addActionListener(e -> {
            alertService.runInspectionNow();
            JOptionPane.showMessageDialog(this, "Alert inspection executed.", "Monitor", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnResolve = new JButton("✅ Resolve Selected");
        btnResolve.addActionListener(e -> resolveSelectedAlert());

        JButton btnResolveAll = new JButton("🧹 Resolve All Active");
        btnResolveAll.addActionListener(e -> {
            alertService.resolveAllAlerts();
            loadAlertsTable();
        });

        btnPanel.add(btnScanNow);
        btnPanel.add(btnResolve);
        btnPanel.add(btnResolveAll);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblAlerts), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 10. SYSTEM SETTINGS & RBAC PANEL
    // ==========================================
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Split: Left = User Accounts (Admin only), Right = Config Parameters
        JPanel userPanel = new JPanel(new BorderLayout(0, 8));
        userPanel.setBorder(BorderFactory.createTitledBorder("User Accounts & Roles (RBAC Management)"));

        String[] cols = {"User ID", "Username", "Full Name", "Role", "Active Status"};
        modelUsers = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUsers = new JTable(modelUsers);
        tblUsers.getColumnModel().getColumn(3).setCellRenderer(new BadgeRenderer());
        tblUsers.setRowHeight(26);

        JButton btnAddUser = new JButton("➕ Create User");
        btnAddUser.addActionListener(e -> showAddUserDialog());
        JPanel userBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        userBtns.add(btnAddUser);

        userPanel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);
        userPanel.add(userBtns, BorderLayout.SOUTH);

        // Config Panel
        JPanel configCard = new JPanel(new GridBagLayout());
        configCard.setBorder(BorderFactory.createTitledBorder("Priority Engine Weights & Duplicate Cooldown Parameters"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        spinWeightPop = new JSpinner(new SpinnerNumberModel(0.25, 0.05, 1.0, 0.05));
        spinWeightWater = new JSpinner(new SpinnerNumberModel(0.30, 0.05, 1.0, 0.05));
        spinWeightMed = new JSpinner(new SpinnerNumberModel(0.25, 0.05, 1.0, 0.05));
        spinWeightRelief = new JSpinner(new SpinnerNumberModel(0.20, 0.05, 1.0, 0.05));
        spinCooldownDays = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));

        JButton btnSaveWeights = new JButton("💾 Save Parameters");
        btnSaveWeights.setBackground(new Color(13, 110, 253));
        btnSaveWeights.setForeground(Color.WHITE);
        btnSaveWeights.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSaveWeights.addActionListener(e -> saveConfigurationParameters());

        g.gridx = 0; g.gridy = 0; configCard.add(new JLabel("Population Weight (W1):"), g);
        g.gridx = 1; configCard.add(spinWeightPop, g);

        g.gridx = 0; g.gridy = 1; configCard.add(new JLabel("Waterlogged Days Weight (W2):"), g);
        g.gridx = 1; configCard.add(spinWeightWater, g);

        g.gridx = 0; g.gridy = 2; configCard.add(new JLabel("Medical Need Weight (W3):"), g);
        g.gridx = 1; configCard.add(spinWeightMed, g);

        g.gridx = 0; g.gridy = 3; configCard.add(new JLabel("Days Since Relief Weight (W4):"), g);
        g.gridx = 1; configCard.add(spinWeightRelief, g);

        g.gridx = 0; g.gridy = 4; configCard.add(new JLabel("Duplicate Relief Cooldown (Days):"), g);
        g.gridx = 1; configCard.add(spinCooldownDays, g);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        configCard.add(btnSaveWeights, g);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, configCard);
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // REFRESH & LOAD METHODS
    // ==========================================
    public void refreshAllData() {
        loadOverviewCards();
        loadCampsTable();
        loadFamiliesTable();
        loadInventoryTable();
        loadShipmentsTable();
        loadDistributionsTable();
        loadRouteData();
        loadAlertsTable();
        loadUsersTable();
        loadConfigValues();
    }

    private void loadOverviewCards() {
        List<Camp> camps = campService.getAllCamps();
        List<Family> families = distributionService.getAllFamilies();
        List<ReliefItem> items = inventoryService.getAllItems();
        List<Shipment> shipments = shipmentService.getAllShipments();
        List<Alert> alerts = alertService.getActiveAlerts();

        int totalSupplies = items.stream().mapToInt(ReliefItem::getAvailableStock).sum();
        long activeShipments = shipments.stream().filter(s -> s.getStatus() != ShipmentStatus.DELIVERED && s.getStatus() != ShipmentStatus.CANCELLED).count();
        long criticalCamps = camps.stream().filter(c -> c.getSeverityLabel() == SeverityLabel.CRITICAL).count();

        cardCamps.updateValue(String.valueOf(camps.size()), camps.size() + " active shelters");
        cardFamilies.updateValue(String.valueOf(families.size()), families.size() + " registered households");
        cardInventory.updateValue(String.format("%,d", totalSupplies), "Items ready across hubs");
        cardShipments.updateValue(String.valueOf(activeShipments), "Convoys in transit");
        cardCritical.updateValue(String.valueOf(criticalCamps), "Requires emergency triage");
        cardAlerts.updateValue(String.valueOf(alerts.size()), "Pending attention");

        // Populate Overview Triage Table
        modelTriageOverview.setRowCount(0);
        List<Camp> prioritized = priorityEngine.getCampsSortedByUrgency(camps);
        for (Camp c : prioritized) {
            double days = PriorityEngine.getDaysSinceLastRelief(c);
            modelTriageOverview.addRow(new Object[]{
                    c.getCampId(), c.getName(), c.getDistrict(), c.getArea(),
                    String.format("%,d", c.getPopulation()), c.getWaterloggedDays(),
                    c.getMedicalNeed(), String.format("%.1f days", days),
                    String.format("%.1f", c.getSeverityScore()), c.getSeverityLabel()
            });
        }
    }

    private void loadCampsTable() {
        modelCamps.setRowCount(0);
        List<Camp> camps = campService.getAllCamps();

        // Update dropdowns
        if (cmbDemandCamp != null) {
            cmbDemandCamp.removeAllItems();
            for (Camp c : camps) cmbDemandCamp.addItem(c);
        }

        for (Camp c : camps) {
            priorityEngine.evaluateAndUpdateCamp(c);
            modelCamps.addRow(new Object[]{
                    c.getCampId(), c.getName(), c.getDistrict(), c.getArea(),
                    c.getPopulation(), c.getCapacity(),
                    String.format("%.0f%%", c.getOccupancyRate()),
                    c.getWaterloggedDays(), c.getMedicalNeed(),
                    c.getLastReliefDate() != null ? c.getLastReliefDate().toString().replace("T", " ") : "Never",
                    String.format("%.1f", c.getSeverityScore()), c.getSeverityLabel()
            });
        }
    }

    private void searchCamps() {
        String kw = txtSearchCamp.getText().trim();
        String dist = (String) cmbDistrictFilter.getSelectedItem();
        String sev = (String) cmbSeverityFilter.getSelectedItem();
        SeverityLabel label = (sev != null && !sev.equalsIgnoreCase("All")) ? SeverityLabel.valueOf(sev) : null;

        List<Camp> list = campService.searchCamps(kw, dist, label);
        modelCamps.setRowCount(0);
        for (Camp c : list) {
            modelCamps.addRow(new Object[]{
                    c.getCampId(), c.getName(), c.getDistrict(), c.getArea(),
                    c.getPopulation(), c.getCapacity(),
                    String.format("%.0f%%", c.getOccupancyRate()),
                    c.getWaterloggedDays(), c.getMedicalNeed(),
                    c.getLastReliefDate() != null ? c.getLastReliefDate().toString().replace("T", " ") : "Never",
                    String.format("%.1f", c.getSeverityScore()), c.getSeverityLabel()
            });
        }
    }

    private void loadFamiliesTable() {
        modelFamilies.setRowCount(0);
        String q = txtSearchFamily != null ? txtSearchFamily.getText().trim() : "";
        List<Family> families = distributionService.searchFamilies(q, "All");

        if (cmbDistFamily != null) {
            cmbDistFamily.removeAllItems();
            for (Family f : families) cmbDistFamily.addItem(f);
        }

        for (Family f : families) {
            modelFamilies.addRow(new Object[]{
                    f.getFamilyId(), f.getCampId(), f.getCampName(),
                    f.getHeadName(), f.getMemberCount(), f.getVulnerabilityNotes(),
                    f.getRegistrationDate() != null ? f.getRegistrationDate().toString().replace("T", " ") : ""
            });
        }
    }

    private void loadInventoryTable() {
        modelInventory.setRowCount(0);
        List<ReliefItem> items = inventoryService.getAllItems();

        if (cmbDistItem != null) {
            cmbDistItem.removeAllItems();
            for (ReliefItem item : items) {
                if (item.getAvailableStock() > 0) cmbDistItem.addItem(item);
            }
        }

        for (ReliefItem i : items) {
            String status = i.getAvailableStock() == 0 ? "DEPLETED" : i.isLowStock() ? "LOW STOCK" : "OK";
            modelInventory.addRow(new Object[]{
                    i.getItemId(), i.getName(), i.getCategory(), i.getUnit(),
                    i.getWarehouseName(), i.getStockIn(), i.getReserved(),
                    i.getDispatched(), i.getDamaged(), i.getExpired(),
                    i.getAvailableStock(), status
            });
        }
    }

    private void loadShipmentsTable() {
        modelShipments.setRowCount(0);
        List<Shipment> shipments = shipmentService.getAllShipments();
        for (Shipment s : shipments) {
            modelShipments.addRow(new Object[]{
                    s.getShipmentId(), s.getWarehouseName(), s.getCampName(),
                    s.getStatus(), s.getTransportMode(), s.getDriverContact(),
                    s.getExpectedDeliveryTime() != null ? s.getExpectedDeliveryTime().toString().replace("T", " ") : "",
                    s.getActualDeliveryTime() != null ? s.getActualDeliveryTime().toString().replace("T", " ") : "Pending",
                    s.getResponsibleUser()
            });
        }
    }

    private void loadDistributionsTable() {
        modelDistributions.setRowCount(0);
        List<DistributionRecord> records = distributionService.getAllRecords();
        for (DistributionRecord d : records) {
            modelDistributions.addRow(new Object[]{
                    d.getRecordId(), d.getFamilyId(), d.getHeadName(),
                    d.getCampName(), d.getItemCategory(), d.getItemName(),
                    d.getQuantity(),
                    d.getDateTime() != null ? d.getDateTime().toString().replace("T", " ") : "",
                    d.getRecordedBy(), d.isOverride() ? "YES" : "NO",
                    d.getOverrideReason() != null ? d.getOverrideReason() : ""
            });
        }
    }

    private void loadRouteData() {
        List<RouteNode> nodes = routeService.getAllNodes();
        List<RouteEdge> edges = routeService.getAllEdges();

        if (cmbRouteSource != null) {
            cmbRouteSource.removeAllItems();
            cmbRouteTarget.removeAllItems();
            for (RouteNode n : nodes) {
                if ("WAREHOUSE".equalsIgnoreCase(n.getType())) cmbRouteSource.addItem(n);
                if ("CAMP".equalsIgnoreCase(n.getType())) cmbRouteTarget.addItem(n);
            }
        }

        mapPanel.setGraphData(nodes, edges);

        modelEdges.setRowCount(0);
        for (RouteEdge e : edges) {
            modelEdges.addRow(new Object[]{
                    e.getEdgeId(), e.getFromNode(), e.getToNode(),
                    e.getDistanceKm(), e.getRouteType(), e.isPassable() ? "OPEN" : "BLOCKED"
            });
        }
    }

    private void loadAlertsTable() {
        modelAlerts.setRowCount(0);
        List<Alert> alerts = alertService.getActiveAlerts();
        for (Alert a : alerts) {
            modelAlerts.addRow(new Object[]{
                    a.getAlertId(), a.getSeverity(), a.getType(),
                    a.getMessage(),
                    a.getCreatedAt() != null ? a.getCreatedAt().toString().replace("T", " ") : "",
                    a.getStatus()
            });
        }
    }

    private void loadUsersTable() {
        modelUsers.setRowCount(0);
        List<User> users = authService.getAllUsers();
        for (User u : users) {
            modelUsers.addRow(new Object[]{
                    u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getRole(), u.isActiveStatus() ? "ACTIVE" : "INACTIVE"
            });
        }
    }

    private void loadConfigValues() {
        spinWeightPop.setValue(configDao.getDouble("weight.population", 0.25));
        spinWeightWater.setValue(configDao.getDouble("weight.waterlogged", 0.30));
        spinWeightMed.setValue(configDao.getDouble("weight.medical", 0.25));
        spinWeightRelief.setValue(configDao.getDouble("weight.last_relief", 0.20));
        spinCooldownDays.setValue(configDao.getInt("cooldown.days", 7));
    }

    private void setupAlertListener() {
        alertService.addListener(activeAlerts -> {
            SwingUtilities.invokeLater(() -> {
                cardAlerts.updateValue(String.valueOf(activeAlerts.size()), "Pending attention");
                loadAlertsTable();
            });
        });
    }

    private void applyRbacPermissions() {
        User cur = AuthContext.getCurrentUser();
        if (cur == null) return;
        boolean canManageUsers = AuthContext.canManageUsers();
        boolean canManageInventory = AuthContext.canManageInventory();
        boolean canManageShipments = AuthContext.canManageShipments();
        boolean isReadOnly = AuthContext.isReadOnly();

        // Restrict Settings tab for non-admins
        if (!canManageUsers) {
            tabbedPane.setEnabledAt(9, false); // Settings & RBAC disabled
        }
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================
    private void showRegisterCampDialog() {
        if (!AuthContext.canRegisterCamps()) {
            JOptionPane.showMessageDialog(this, "Permission Denied: Your role cannot register camps.", "RBAC Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtId = new JTextField("CMP-" + (int)(Math.random() * 900 + 100));
        JTextField txtName = new JTextField();
        JComboBox<String> cmbDist = new JComboBox<>(new String[]{"Sylhet", "Sunamganj", "Moulvibazar", "Habiganj"});
        JTextField txtArea = new JTextField();
        JSpinner spinPop = new JSpinner(new SpinnerNumberModel(500, 10, 10000, 50));
        JSpinner spinCap = new JSpinner(new SpinnerNumberModel(600, 10, 10000, 50));
        JSpinner spinWater = new JSpinner(new SpinnerNumberModel(5, 0, 60, 1));
        JSpinner spinMed = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 5));

        JPanel form = new JPanel(new GridLayout(8, 2, 6, 6));
        form.add(new JLabel("Camp ID:")); form.add(txtId);
        form.add(new JLabel("Camp Name:")); form.add(txtName);
        form.add(new JLabel("District:")); form.add(cmbDist);
        form.add(new JLabel("Area/Upazila:")); form.add(txtArea);
        form.add(new JLabel("Population:")); form.add(spinPop);
        form.add(new JLabel("Shelter Capacity:")); form.add(spinCap);
        form.add(new JLabel("Waterlogged Days:")); form.add(spinWater);
        form.add(new JLabel("Medical Need Count:")); form.add(spinMed);

        int res = JOptionPane.showConfirmDialog(this, form, "Register Flood Relief Camp", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            if (txtName.getText().trim().isEmpty() || txtArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Camp Name and Area.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            campService.registerCamp(
                    txtId.getText().trim(), txtName.getText().trim(),
                    (String) cmbDist.getSelectedItem(), txtArea.getText().trim(),
                    (int) spinPop.getValue(), (int) spinCap.getValue(),
                    (int) spinWater.getValue(), (int) spinMed.getValue(),
                    24.89, 91.86
            );
            refreshAllData();
        }
    }

    private void recordCampReliefArrival() {
        int row = tblCamps.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a camp from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String campId = (String) modelCamps.getValueAt(row, 0);
        campService.recordReliefArrival(campId);
        refreshAllData();
        JOptionPane.showMessageDialog(this, "Relief arrival recorded! Camp priority score updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRegisterFamilyDialog() {
        if (!AuthContext.canRegisterFamilies()) {
            JOptionPane.showMessageDialog(this, "Permission Denied: Your role cannot register families.", "RBAC Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtId = new JTextField("FAM-" + (int)(Math.random() * 9000 + 1000));
        JComboBox<Camp> cmbCamp = new JComboBox<>();
        for (Camp c : campService.getAllCamps()) cmbCamp.addItem(c);
        JTextField txtHead = new JTextField();
        JSpinner spinMembers = new JSpinner(new SpinnerNumberModel(5, 1, 25, 1));
        JTextField txtNotes = new JTextField("Elderly, infant children");

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Family ID:")); form.add(txtId);
        form.add(new JLabel("Camp:")); form.add(cmbCamp);
        form.add(new JLabel("Household Head:")); form.add(txtHead);
        form.add(new JLabel("Member Count:")); form.add(spinMembers);
        form.add(new JLabel("Vulnerability Notes:")); form.add(txtNotes);

        int res = JOptionPane.showConfirmDialog(this, form, "Register Beneficiary Family", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            if (txtHead.getText().trim().isEmpty() || cmbCamp.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please provide household head name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Camp selectedCamp = (Camp) cmbCamp.getSelectedItem();
            distributionService.registerFamily(
                    txtId.getText().trim(), selectedCamp.getCampId(),
                    txtHead.getText().trim(), (int) spinMembers.getValue(),
                    txtNotes.getText().trim()
            );
            refreshAllData();
        }
    }

    private void showStockInDialog() {
        if (!AuthContext.canManageInventory()) {
            JOptionPane.showMessageDialog(this, "Permission Denied: Only Administrators and Relief Coordinators can manage stock.", "RBAC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = tblInventory.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item from the table to Stock-In.", "Select Item", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String itemId = (String) modelInventory.getValueAt(row, 0);
        String name = (String) modelInventory.getValueAt(row, 1);

        String val = JOptionPane.showInputDialog(this, "Enter stock quantity to add for:\n" + name, "Stock-In Operation", JOptionPane.PLAIN_MESSAGE);
        if (val != null && !val.trim().isEmpty()) {
            try {
                int qty = Integer.parseInt(val.trim());
                inventoryService.stockIn(itemId, qty);
                refreshAllData();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDamageDialog() {
        if (!AuthContext.canManageInventory()) return;
        int row = tblInventory.getSelectedRow();
        if (row < 0) return;
        String itemId = (String) modelInventory.getValueAt(row, 0);
        String val = JOptionPane.showInputDialog(this, "Enter damaged quantity to deduct:", "Record Damaged", JOptionPane.WARNING_MESSAGE);
        if (val != null && !val.trim().isEmpty()) {
            try {
                int qty = Integer.parseInt(val.trim());
                inventoryService.markDamaged(itemId, qty);
                refreshAllData();
            } catch (Exception ignored) {}
        }
    }

    private void showExpiredDialog() {
        if (!AuthContext.canManageInventory()) return;
        int row = tblInventory.getSelectedRow();
        if (row < 0) return;
        String itemId = (String) modelInventory.getValueAt(row, 0);
        String val = JOptionPane.showInputDialog(this, "Enter expired quantity to quarantine:", "Record Expired", JOptionPane.WARNING_MESSAGE);
        if (val != null && !val.trim().isEmpty()) {
            try {
                int qty = Integer.parseInt(val.trim());
                inventoryService.markExpired(itemId, qty);
                refreshAllData();
            } catch (Exception ignored) {}
        }
    }

    private void showNewItemDialog() {
        if (!AuthContext.canManageInventory()) return;
        JTextField txtId = new JTextField("ITM-" + (int)(Math.random() * 900 + 100));
        JTextField txtName = new JTextField();
        JComboBox<ItemCategory> cmbCat = new JComboBox<>(ItemCategory.values());
        JTextField txtUnit = new JTextField("packs");
        JSpinner spinThreshold = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        JSpinner spinStock = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        JComboBox<String> cmbWh = new JComboBox<>(new String[]{"WH-SYL-01", "WH-SUN-01", "WH-COM-01"});

        JPanel form = new JPanel(new GridLayout(7, 2, 6, 6));
        form.add(new JLabel("Item ID:")); form.add(txtId);
        form.add(new JLabel("Name:")); form.add(txtName);
        form.add(new JLabel("Category:")); form.add(cmbCat);
        form.add(new JLabel("Unit:")); form.add(txtUnit);
        form.add(new JLabel("Low Stock Threshold:")); form.add(spinThreshold);
        form.add(new JLabel("Initial Stock:")); form.add(spinStock);
        form.add(new JLabel("Warehouse:")); form.add(cmbWh);

        int res = JOptionPane.showConfirmDialog(this, form, "Create Relief Item Type", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            inventoryService.createItem(
                    txtId.getText().trim(), txtName.getText().trim(),
                    (ItemCategory) cmbCat.getSelectedItem(), txtUnit.getText().trim(),
                    null, (int) spinThreshold.getValue(), (int) spinStock.getValue(),
                    (String) cmbWh.getSelectedItem()
            );
            refreshAllData();
        }
    }

    private void showNewShipmentDialog() {
        if (!AuthContext.canManageShipments()) {
            JOptionPane.showMessageDialog(this, "Permission Denied: Only Coordinators and Admins can create shipments.", "RBAC", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtId = new JTextField("SHP-" + (int)(Math.random() * 9000 + 1000));
        JComboBox<String> cmbWh = new JComboBox<>(new String[]{"WH-SYL-01 (Sylhet Central)", "WH-SUN-01 (Sunamganj Depot)", "WH-COM-01 (Companiganj Hub)"});
        JComboBox<Camp> cmbCamp = new JComboBox<>();
        for (Camp c : campService.getAllCamps()) cmbCamp.addItem(c);
        JComboBox<String> cmbTransport = new JComboBox<>(new String[]{"TRUCK", "TRAWLER_BOAT", "SPEEDBOAT", "TRACTOR"});
        JTextField txtDriver = new JTextField("Driver Jamal (01712-345678)");
        JComboBox<ReliefItem> cmbItem = new JComboBox<>();
        for (ReliefItem ri : inventoryService.getAllItems()) {
            if (ri.getAvailableStock() > 0) cmbItem.addItem(ri);
        }
        JSpinner spinQty = new JSpinner(new SpinnerNumberModel(50, 5, 2000, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 6, 6));
        form.add(new JLabel("Shipment ID:")); form.add(txtId);
        form.add(new JLabel("Origin Warehouse:")); form.add(cmbWh);
        form.add(new JLabel("Destination Camp:")); form.add(cmbCamp);
        form.add(new JLabel("Transport Mode:")); form.add(cmbTransport);
        form.add(new JLabel("Driver Contact:")); form.add(txtDriver);
        form.add(new JLabel("Manifest Item:")); form.add(cmbItem);
        form.add(new JLabel("Quantity:")); form.add(spinQty);

        int res = JOptionPane.showConfirmDialog(this, form, "Create Emergency Relief Shipment", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String whId = ((String) cmbWh.getSelectedItem()).split(" ")[0];
            Camp c = (Camp) cmbCamp.getSelectedItem();
            ReliefItem selectedItem = (ReliefItem) cmbItem.getSelectedItem();

            if (c == null || selectedItem == null) return;

            ShipmentItem item = new ShipmentItem(txtId.getText().trim(), selectedItem.getItemId(), (int) spinQty.getValue());
            boolean ok = shipmentService.createShipment(
                    txtId.getText().trim(), whId, c.getCampId(),
                    AuthContext.getCurrentUser() != null ? AuthContext.getCurrentUser().getUsername() : "admin",
                    txtDriver.getText().trim(),
                    (String) cmbTransport.getSelectedItem(),
                    LocalDateTime.now().plusHours(4),
                    "High priority flood relief",
                    List.of(item)
            );
            if (ok) {
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Shipment created and inventory reserved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create shipment. Check available inventory.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void advanceShipmentStatus() {
        if (!AuthContext.canManageShipments()) return;
        int row = tblShipments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a shipment to advance.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String shpId = (String) modelShipments.getValueAt(row, 0);
        Shipment s = shipmentService.getShipmentById(shpId);
        if (s == null) return;

        ShipmentStatus next = switch (s.getStatus()) {
            case PENDING -> ShipmentStatus.PACKED;
            case PACKED -> ShipmentStatus.DISPATCHED;
            case DISPATCHED -> ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> ShipmentStatus.DELIVERED;
            default -> null;
        };

        if (next == null) {
            JOptionPane.showMessageDialog(this, "Shipment already at final status (" + s.getStatus() + ").", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        shipmentService.updateStatus(shpId, next);
        refreshAllData();
    }

    private void cancelShipment() {
        if (!AuthContext.canManageShipments()) return;
        int row = tblShipments.getSelectedRow();
        if (row < 0) return;
        String shpId = (String) modelShipments.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel shipment " + shpId + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            shipmentService.updateStatus(shpId, ShipmentStatus.CANCELLED);
            refreshAllData();
        }
    }

    private void runDuplicateCheck() {
        Family fam = (Family) cmbDistFamily.getSelectedItem();
        ItemCategory cat = (ItemCategory) cmbDistCategory.getSelectedItem();
        if (fam == null || cat == null) return;

        DistributionService.DuplicateCheckResult res = distributionService.checkEligibility(fam.getFamilyId(), cat);
        if (res.isDuplicate()) {
            lblEligibilityResult.setText("<html><font color='red'><b>" + res.getMessage() + "</b></font></html>");
        } else {
            lblEligibilityResult.setText("<html><font color='green'><b>" + res.getMessage() + "</b></font></html>");
        }
    }

    private void executeDistribution() {
        if (!AuthContext.canRecordDistributions()) {
            JOptionPane.showMessageDialog(this, "Permission Denied: Your role cannot record distributions.", "RBAC", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Family fam = (Family) cmbDistFamily.getSelectedItem();
        ItemCategory cat = (ItemCategory) cmbDistCategory.getSelectedItem();
        ReliefItem item = (ReliefItem) cmbDistItem.getSelectedItem();
        int qty = (int) spinDistQty.getValue();

        if (fam == null || cat == null || item == null) {
            JOptionPane.showMessageDialog(this, "Select all distribution fields.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isOverride = chkOverride.isSelected();
        String reason = txtOverrideReason.getText().trim();

        if (isOverride && !AuthContext.canOverrideDuplicateDistributions()) {
            JOptionPane.showMessageDialog(this, "Unauthorized: Only Administrators and Relief Coordinators can override duplicate limits.", "RBAC Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String recId = "DST-" + (int)(Math.random() * 90000 + 10000);
        try {
            boolean ok = distributionService.recordDistribution(
                    recId, fam.getFamilyId(), fam.getCampId(),
                    cat, item.getItemId(), qty,
                    AuthContext.getCurrentUser() != null ? AuthContext.getCurrentUser().getUsername() : "volunteer",
                    null, isOverride, reason
            );

            if (ok) {
                // Deduct stock in real-time
                inventoryService.dispatchStock(item.getItemId(), qty);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Distribution recorded successfully! (Record ID: " + recId + ")", "Distribution Confirmed", JOptionPane.INFORMATION_MESSAGE);
                chkOverride.setSelected(false);
                txtOverrideReason.setText("");
                txtOverrideReason.setEnabled(false);
                runDuplicateCheck();
            } else {
                JOptionPane.showMessageDialog(this, "Distribution blocked: Duplicate aid detected! Check eligibility or provide coordinator override.", "Duplicate Prevention", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Distribution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateShortestRoute() {
        RouteNode src = (RouteNode) cmbRouteSource.getSelectedItem();
        RouteNode tgt = (RouteNode) cmbRouteTarget.getSelectedItem();
        if (src == null || tgt == null) return;

        PathResult res = routeService.findShortestPath(src.getNodeId(), tgt.getNodeId());
        mapPanel.setHighlightedPath(res);

        if (!res.isReachable()) {
            lblRouteDetails.setText("<html><font color='red'><b>NO PASSABLE ROUTE FOUND!</b><br>All connecting roads and waterways are currently flooded or impassable.</font></html>");
        } else {
            lblRouteDetails.setText(String.format("<html>" +
                    "<b>Optimal Route (Dijkstra):</b> %s<br>" +
                    "<b>Total Distance:</b> <font color='#0d6efd'>%.1f km</font> | " +
                    "<b>Estimated Travel Time:</b> <font color='#198754'>%.1f hours</font><br>" +
                    "<b>Recommended Transport Mode:</b> <font color='#d63384'><b>%s</b></font>" +
                    "</html>",
                    res.getFormattedPath(), res.getTotalDistanceKm(), res.getEstimatedHours(), res.getRecommendedTransport()));
        }
    }

    private void toggleSelectedEdgePassability() {
        int row = tblEdges.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a route edge from the table to toggle.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int edgeId = (int) modelEdges.getValueAt(row, 0);
        String status = (String) modelEdges.getValueAt(row, 5);
        boolean newStatus = !"OPEN".equalsIgnoreCase(status);

        routeService.toggleEdgePassability(edgeId, newStatus);
        loadRouteData();
        calculateShortestRoute(); // Auto re-run Dijkstra on updated terrain
    }

    private void computeDemandMetrics() {
        Camp c = (Camp) cmbDemandCamp.getSelectedItem();
        int days = switch (cmbDemandDays.getSelectedIndex()) {
            case 0 -> 3;
            case 1 -> 7;
            case 2 -> 14;
            default -> 7;
        };

        if (c == null) return;
        DemandEstimationService.CampDemandReport rep = demandService.calculateDemand(c, days);
        modelDemandMetrics.setRowCount(0);
        for (var entry : rep.getFormattedMetrics().entrySet()) {
            modelDemandMetrics.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void resolveSelectedAlert() {
        int row = tblAlerts.getSelectedRow();
        if (row < 0) return;
        int id = (int) modelAlerts.getValueAt(row, 0);
        alertService.resolveAlert(id);
        loadAlertsTable();
    }

    private void showAddUserDialog() {
        if (!AuthContext.canManageUsers()) return;
        JTextField txtU = new JTextField();
        JPasswordField txtP = new JPasswordField();
        JTextField txtFull = new JTextField();
        JComboBox<UserRole> cmbRole = new JComboBox<>(UserRole.values());

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Username:")); form.add(txtU);
        form.add(new JLabel("Password:")); form.add(txtP);
        form.add(new JLabel("Full Name:")); form.add(txtFull);
        form.add(new JLabel("Role:")); form.add(cmbRole);

        int res = JOptionPane.showConfirmDialog(this, form, "Create User Account", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            authService.createUser(txtU.getText().trim(), new String(txtP.getPassword()), txtFull.getText().trim(), (UserRole) cmbRole.getSelectedItem());
            loadUsersTable();
        }
    }

    private void saveConfigurationParameters() {
        if (!AuthContext.canModifySystemConfig()) {
            JOptionPane.showMessageDialog(this, "Permission Denied: Only administrators can modify system parameters.", "RBAC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        configDao.set("weight.population", String.valueOf(spinWeightPop.getValue()));
        configDao.set("weight.waterlogged", String.valueOf(spinWeightWater.getValue()));
        configDao.set("weight.medical", String.valueOf(spinWeightMed.getValue()));
        configDao.set("weight.last_relief", String.valueOf(spinWeightRelief.getValue()));
        configDao.set("cooldown.days", String.valueOf(spinCooldownDays.getValue()));

        JOptionPane.showMessageDialog(this, "System configurations updated successfully in Cloud Database!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshAllData();
    }

    // CSV Exports
    private void exportCampsCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("camps_report_" + System.currentTimeMillis() + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ExportService.exportCampsToCsv(campService.getAllCamps(), fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Camps exported to " + fc.getSelectedFile().getName(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportFamiliesCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("beneficiaries_report_" + System.currentTimeMillis() + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ExportService.exportFamiliesToCsv(distributionService.getAllFamilies(), fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Beneficiaries exported to " + fc.getSelectedFile().getName(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportInventoryCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("inventory_report_" + System.currentTimeMillis() + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ExportService.exportInventoryToCsv(inventoryService.getAllItems(), fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Inventory exported to " + fc.getSelectedFile().getName(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportShipmentsCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("shipments_report_" + System.currentTimeMillis() + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ExportService.exportShipmentsToCsv(shipmentService.getAllShipments(), fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Shipments exported to " + fc.getSelectedFile().getName(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportDistributionsCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("distributions_report_" + System.currentTimeMillis() + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ExportService.exportDistributionsToCsv(distributionService.getAllRecords(), fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Distributions exported to " + fc.getSelectedFile().getName(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void performLogout() {
        authService.logout();
        dispose();
        MainApp.launchLoginAndMain();
    }
}
