# Flood Relief and Disaster Supply Chain Tracker (`FloodReliefTracker`)

A production-grade, cloud-connected Java Swing desktop application designed for emergency disaster response, humanitarian triage, and supply chain logistics in flood-affected regions (focusing on the **Haor Basin** across **Sylhet** and **Sunamganj**, Bangladesh).

---

## 🌟 Key Features & 10-Module Implementation

1. **Module 1: Cloud Database Connectivity (Neon PostgreSQL)**
   - Persistent relational data persistence hosted on **Neon Serverless PostgreSQL**.
   - Robust connection pooling via **HikariCP** (`HikariDataSource`) with real-time status indicators.
   - Self-bootstrapping database schema initializer with seed data for Haor basin hubs, shelters, and relief items.

2. **Module 2: User Authentication & Role-Based Access Control (RBAC)**
   - Secure password hashing using **PBKDF2 with HMAC-SHA256**, 12,000 iterations, unique salts, and constant-time verification.
   - 5 Granular RBAC roles:
     - 👑 **Administrator:** Full administrative rights, user creation, weight adjustments.
     - 🚚 **Relief Coordinator:** Logistics, supply shipments, inventory management, override approvals.
     - ⛺ **Camp Manager:** Camp registration, family enrollment, aid distributions.
     - 🤝 **Volunteer:** Aid distribution logging and field verification.
     - 👁️ **Donor / Observer:** Read-only transparency dashboard and relief stock reporting.
   - Quick one-click demo login buttons in the login dialog for instant testing.

3. **Module 3: Camp Registration & Haor Region Management**
   - Dynamic management of shelters across Sylhet and Sunamganj (Companiganj, Gowainghat, Tahirpur, Derai, Bishwamvarpur, Kanaighat, Jagannathpur, etc.).
   - Tracks population, shelter capacity, waterlogged days, and acute medical need counts.
   - Live occupancy percentage calculation with over-capacity detection.

4. **Module 4: Priority Engine & Dynamic Severity Scoring Model**
   - Normalized 4-Factor Urgency Formula:
     $$\text{Score} = 100 \times \left( W_{\text{pop}} \cdot f_{\text{pop}} + W_{\text{water}} \cdot f_{\text{water}} + W_{\text{med}} \cdot f_{\text{med}} + W_{\text{relief}} \cdot f_{\text{relief}} \right)$$
   - Categorized triage labels:
     - 🔴 **Critical:** Score $\ge 80.0$
     - 🟠 **High:** Score $60.0 - 79.9$
     - 🟡 **Medium:** Score $35.0 - 59.9$
     - 🟢 **Low:** Score $< 35.0$
   - High-performance Java **`PriorityQueue<Camp>`** with a custom tie-breaking comparator for urgent medical and relief triage.

5. **Module 5: Concurrency-Safe Inventory Management**
   - Multi-warehouse inventory tracking (Food, Medical/ORS, WASH, Shelter, Clothing).
   - Prevents stock over-allocation and race conditions using:
     - Java **`synchronized`** monitor locks.
     - Cloud PostgreSQL row-level locks (**`SELECT ... FOR UPDATE OF r`** inside atomic transactions).
   - Tracks: `Stock-In`, `Reserved`, `Dispatched`, `Damaged`, and `Expired` quantities.

6. **Module 6: Shipment Lifecycle & Supply Chain Logistics**
   - End-to-end shipment status progression:
     $$\text{Pending} \;\longrightarrow\; \text{Packed} \;\longrightarrow\; \text{Dispatched} \;\longrightarrow\; \text{In Transit} \;\longrightarrow\; \text{Delivered}$$
   - Auto-reserves inventory upon shipment creation and transitions stock to dispatched.
   - Automatically updates destination camp's `last_relief_date` when marked `Delivered`.

7. **Module 7: Duplicate Relief Prevention & Beneficiary Registry**
   - Registers beneficiary households with family IDs, head of household, member counts, and vulnerability tags.
   - **Duplicate Relief Prevention Algorithm:** Scans aid history to prevent repeated distribution of the same category within the configured eligibility cooldown period (default: 7 days).
   - Authorized override workflow: Administrators and Relief Coordinators can authorize duplicate aid with mandatory justification audit logging.

8. **Module 8: Route Optimization (Dijkstra's Algorithm)**
   - Graph network representing regional hubs, transit waypoints, and flood shelters.
   - **Dijkstra's Algorithm** with priority queue calculates optimal shortest route.
   - Computes multi-modal transit estimates (Road trucks vs. Haor engine trawler boats).
   - **Flash Flood Simulation:** Toggle roads as flooded/blocked to observe dynamic real-time bypass rerouting!
   - Interactive 2D graphical network map (`HaorRouteMapPanel`) with glowing path renderers.

9. **Module 9: Sphere Demand Estimation & Automated Alert Monitor**
   - Calculates humanitarian rations based on **Sphere Humanitarian Standards**:
     - Clean drinking water: 15 Liters / person / day.
     - Nutrition: 2,100 kcal / person / day (rice, lentils, fortified oil, high-energy biscuits).
     - ORS sachets and water purification tablets (Halazone).
     - Family WASH hygiene packs (1 kit / family / 14 days).
     - Reinforced shelter tarpaulins.
   - Background daemon thread (`AlertMonitoringService`) running automated scans every 30 seconds for:
     - Depleted or low stock below threshold.
     - Expired or near-expiry medicines.
     - Delayed shipments past ETA.
     - Overcrowded shelters ($> 100\%$ capacity).
     - Neglected shelters ($> 7$ days without relief aid).

10. **Module 10: Modern FlatLaf UI, Indicator Cards & CSV Export**
    - Styled with modern **FlatLaf** Look and Feel (Light / Dark mode toggle).
    - Summary metric indicator cards.
    - Status pills and color-coded badges for table cells.
    - Comprehensive CSV export for camps, families, inventory, shipments, distributions, and alerts.

---

## 🗄️ Database Architecture (Neon Cloud PostgreSQL)

Connected to a dedicated cloud database instance:
- **Host:** `ep-old-sky-aocx9bsp-pooler.c-2.ap-southeast-1.aws.neon.tech`
- **Database:** `flood_relief_db`
- **Driver:** PostgreSQL JDBC Driver 42.7.5 + HikariCP Connection Pool

### Schema Tables:
1. `users` (userId, username, passwordHash, fullName, role, activeStatus, createdAt)
2. `warehouses` (warehouseId, name, location, capacitySqft, contactPerson)
3. `camps` (campId, name, district, area, population, capacity, waterloggedDays, medicalNeed, lastReliefDate, severityScore, severityLabel, latitude, longitude)
4. `families` (familyId, campId, headName, memberCount, vulnerabilityNotes, registrationDate)
5. `relief_items` (itemId, name, category, unit, expiryDate, lowStockThreshold, stockIn, reserved, dispatched, damaged, expired, warehouseId)
6. `shipments` (shipmentId, warehouseId, campId, status, dispatchTime, expectedDeliveryTime, actualDeliveryTime, responsibleUser, driverContact, transportMode, notes)
7. `shipment_items` (id, shipmentId, itemId, quantity)
8. `distribution_records` (recordId, familyId, campId, itemCategory, itemId, quantity, dateTime, recordedBy, shipmentId, isOverride, overrideReason)
9. `alerts` (alertId, type, message, severity, createdAt, status)
10. `system_configurations` (configKey, configValue)
11. `route_nodes` (nodeId, name, type, district, posX, posY)
12. `route_edges` (edgeId, fromNode, toNode, distanceKm, routeType, isPassable)

---

## 🔑 Default User Profiles (Pre-Seeded)

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `admin123` | **Administrator** | Full access, user accounts, system configuration |
| `coord` | `coord123` | **Relief Coordinator** | Shipments, inventory management, duplicate overrides |
| `campmgr` | `camp123` | **Camp Manager** | Camp registration, family registry, distribution |
| `vol1` | `vol123` | **Volunteer** | Aid distribution logging, field view |
| `donor1` | `donor123` | **Donor / Observer** | Read-only transparency dashboard and reports |

*(You can also click the quick demo buttons on the login dialog for instant access).*

---

## 🚀 How to Run

### Option 1: Run with Maven Wrapper
```powershell
.\mvnw.cmd compile exec:java -Dexec.mainClass="com.floodrelief.MainApp"
```

### Option 2: Run Executable Shaded JAR
```powershell
java -jar target\flood-relief-tracker-1.0.0.jar
```

### Option 3: Run Unit Tests
```powershell
.\mvnw.cmd test
```
