# 🌊 Flood Relief and Disaster Supply Chain Tracker (`FloodReliefTracker`)

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![UI](https://img.shields.io/badge/UI-FlatLaf%203.5.4-blue?style=for-the-badge)](https://www.formdev.com/flatlaf/)
[![Database](https://img.shields.io/badge/Database-Neon%20Serverless%20PostgreSQL-00e599?style=for-the-badge&logo=postgresql&logoColor=white)](https://neon.tech/)
[![Connection Pool](https://img.shields.io/badge/Pool-HikariCP%205.1.0-orange?style=for-the-badge)](https://github.com/brettwooldridge/HikariCP)
[![Tests](https://img.shields.io/badge/Tests-JUnit%205%20Passing-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=for-the-badge)](LICENSE)

A cloud-connected, production-grade **Java Swing** desktop application engineered for emergency disaster response, humanitarian triage, and supply chain logistics in flood-ravaged regions—specifically tailored for the vulnerable **Haor Basin** across **Sylhet** and **Sunamganj**, Bangladesh.

---

## 📌 Table of Contents
- [Project Overview](#-project-overview)
- [System Architecture (3-Layer Model)](#-system-architecture-3-layer-model)
- [10-Module Technical Breakdown](#-10-module-technical-breakdown)
- [Role-Based Access Control (RBAC)](#-role-based-access-control-rbac)
- [Technology Stack & Dependencies](#-technology-stack--dependencies)
- [Cloud Database Setup (Neon PostgreSQL)](#-cloud-database-setup-neon-postgresql)
- [How to Run and Build](#-how-to-run-and-build)
- [Automated Testing Suite (JUnit 5)](#-automated-testing-suite-junit-5)
- [Git Tagging & Modular History](#-git-tagging--modular-history)
- [Interactive Features Showcase](#-interactive-features-showcase)
- [License](#-license)

---

## 📖 Project Overview

During flash floods in northeastern Bangladesh, the **Haor basin** (spanning Tahirpur, Companiganj, Gowainghat, Derai, Bishwamvarpur, Kanaighat, and Jagannathpur) is frequently submerged. Critical ground roads become impassable, cutting off villages and overwhelming temporary shelters. 

**`FloodReliefTracker`** solves key humanitarian bottlenecks:
- **Triage Under Scarcity:** Mathematically prioritizes shelters based on severity, waterlogged isolation, population, and time since last relief.
- **Logistics Under Road Inundation:** Computes multi-modal transit paths (Road Trucks vs. Engine Trawler Boats) with dynamic detour recalculations when roads submerge.
- **Fair Resource Allocation:** Blocks aid duplication with configurable cooldowns while maintaining an emergency audit-backed override trail.
- **Sphere Humanitarian Compliance:** Automatically estimates rations for clean water, kilocalories, ORS, and hygiene kits using Sphere International Standards.
- **Concurrency & High Availability:** Eliminates double-allocation race conditions using PostgreSQL row-level locks and thread synchronization over Neon cloud serverless database.

---

## 🏗️ System Architecture (3-Layer Model)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       PRESENTATION LAYER (UI)                               │
│  • FlatLaf Modern Theme (Light / Dark Modes)     • Real-Time Metric Cards   │
│  • Role-Restricted Admin / Volunteer Views       • 2D Route Canvas Map      │
│  • Custom Badge Renderers & Status Pills         • Instant CSV Data Export  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER (SERVICES)                          │
│  • PriorityEngine (4-Factor Normalization & PriorityQueue)                  │
│  • RouteOptimizationService (Dijkstra's Algorithm & Multi-Modal Routing)   │
│  • InventoryService (Synchronized Locking & Row Locks)                      │
│  • ShipmentService (5-State Supply Chain Lifecycle)                         │
│  • DistributionService (7-Day Duplicate Aid Prevention & Override Trail)    │
│  • DemandEstimationService (Sphere Humanitarian Standards Formulas)        │
│  • AlertMonitoringService (Background Daemon Thread - 30s Polling)          │
│  • AuthService & PasswordSecurity (PBKDF2 HMAC-SHA256, 12k Iterations)      │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                      DATA LAYER (PERSISTENCE & CLOUD)                       │
│  • Relational DAOs (UserDao, CampDao, ReliefItemDao, ShipmentDao, etc.)     │
│  • HikariCP Connection Pool (Low-latency connection pooling to Neon Cloud)  │
│  • PostgreSQL 16+ on Neon Serverless (12 Normalized Relational Tables)      │
│  • Row-Level Locks (`SELECT ... FOR UPDATE OF r`) in Atomic Transactions    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚙️ 10-Module Technical Breakdown

### Module 1: Cloud Database Connectivity & Connection Pooling
- Connected to **Neon Serverless PostgreSQL** hosted on AWS (`ap-southeast-1`).
- High-efficiency connection pooling via **HikariCP** (`HikariDataSource`) with automatic credential loading from `application.properties`.
- **`SchemaInitializer`**: Automatically detects table availability, executes `schema.sql`, verifies primary/foreign keys, and seeds Haor baseline hubs, warehouses, camps, items, and routing graphs upon first launch.

### Module 2: Authentication & Role-Based Access Control (RBAC)
- **PBKDF2 with HMAC-SHA256**: Generates salted, cryptographically secure 256-bit hashes using 12,000 iterations and constant-time byte comparisons to thwart timing attacks.
- Enforces session security through `AuthContext`, dynamically tailoring Swing components, navigation tabs, and actionable buttons based on user permissions.

### Module 3: Camp Registration & Haor Region Management
- Tracks critical shelter metrics: *Total Population*, *Shelter Capacity*, *Waterlogged Days*, and *Acute Medical Needs*.
- Automatically computes live occupancy percentages, warning operators when shelters exceed 100% capacity.
- Supports comprehensive multi-criteria filtering by Upazila/District (Sylhet vs. Sunamganj) and severity level.

### Module 4: Priority Engine & Dynamic Severity Scoring
- Normalizes disparate shelter metrics into a uniform 0–100 urgency score:
  $$\text{Score} = 100 \times \left( W_{\text{pop}} \cdot f_{\text{pop}} + W_{\text{water}} \cdot f_{\text{water}} + W_{\text{med}} \cdot f_{\text{med}} + W_{\text{relief}} \cdot f_{\text{relief}} \right)$$
- Default weights: **Population ($0.30$)**, **Waterlogged Days ($0.30$)**, **Medical Need ($0.30$)**, and **Days Since Last Relief ($0.10$)**.
- Urgency thresholds:
  - 🔴 **Critical:** $\text{Score} \ge 80.0$
  - 🟠 **High:** $60.0 \le \text{Score} < 80.0$
  - 🟡 **Medium:** $35.0 \le \text{Score} < 60.0$
  - 🟢 **Low:** $\text{Score} < 35.0$
- Implements a Java **`PriorityQueue<Camp>`** with a custom comparator (tie-breaking on acute medical needs and unserved durations) to deliver instantaneous triage ranking.

### Module 5: Concurrency-Safe Inventory Management
- Categorized relief tracking across warehouses: *Food & Nutrition*, *Medical & ORS*, *WASH & Hygiene*, *Shelter & Tarpaulins*, and *Clothing*.
- Lifecycle stock buckets: `Stock-In`, `Reserved`, `Dispatched`, `Damaged`, and `Expired`.
- **Concurrency Control:** Utilizes Java `synchronized` methods combined with cloud database row-level locking (**`SELECT ... FOR UPDATE OF r`**) in atomic transactions to guarantee zero over-allocation when concurrent field agents request stock simultaneously.

### Module 6: Shipment Lifecycle & Logistics Tracking
- Enforces strict five-state linear state progression:
  $$\text{Pending} \;\longrightarrow\; \text{Packed} \;\longrightarrow\; \text{Dispatched} \;\longrightarrow\; \text{In Transit} \;\longrightarrow\; \text{Delivered}$$
- Automatically moves inventory from `stock_in` $\rightarrow$ `reserved` upon creation, and $\rightarrow$ `dispatched` upon release.
- Automatically updates the destination camp's `last_relief_date` in the database upon marking the shipment `Delivered`.

### Module 7: Duplicate Relief Prevention & Beneficiary Registry
- Enrolls vulnerable households with family IDs, head of household, family size, and vulnerability tags.
- **Duplicate Prevention Algorithm:** Scans distribution history to prevent duplicate relief distributions of the same category to a family within the configurable cooldown period (Default: **7 days**).
- **Authorized Override Workflow:** Administrators and Relief Coordinators can override blocks for extreme crises, recording a mandatory justification in the database audit log.

### Module 8: Route Optimization & Multi-Modal Dijkstra Graph
- Models the Haor transportation network as a weighted graph of **RouteNodes** and **RouteEdges**.
- **Dijkstra's Algorithm** finds the shortest path, calculating travel times across transit modes:
  - 🚛 **Road Trucks:** Average speed $\sim 35\text{ km/h}$.
  - 🚤 **Haor Engine Trawler Boats:** Average speed $\sim 15\text{ km/h}$.
- **Flash Flood Road Inundation Simulation:** Operators can toggle road status (Passable / Flooded). Dijkstra dynamically detects submerged roads and recalculates bypass boat routes!
- **Interactive 2D Canvas Map:** Custom graphical panel (`HaorRouteMapPanel`) with zoom, pan, glowing path highlights, and visual waypoint indicators.

### Module 9: Sphere Demand Estimation & Automated Alert Daemon
- Implements international humanitarian rations based on **Sphere Standards**:
  - 💧 **Drinking Water:** $15\text{ Liters / person / day}$
  - 🍚 **Food & Nutrition:** $2,100\text{ kcal / person / day}$ (Rice, Lentils, Oil, High-Energy Biscuits)
  - 💊 **Medical:** ORS packets + Halazone water purification tablets
  - 🧼 **WASH Hygiene:** 1 hygiene kit / family / 14 days
  - ⛺ **Shelter:** Heavy-duty reinforced tarpaulins for displaced families
- **Background Alert Daemon (`AlertMonitoringService`):** Background thread scanning every 30 seconds for:
  - ⚠️ Low stock below safety threshold
  - ⏳ Expired or near-expiry medicine
  - 🚨 Shelters with severe overcrowding ($> 100\%$ capacity)
  - ⏱️ Neglected camps ($> 7$ days without aid)
  - 🚚 Delayed shipments overdue past ETA

### Module 10: Modern FlatLaf UI, Indicator Cards & CSV Reporting
- Styled with modern **FlatLaf** (Light & Dark theme switching via `ThemeManager`).
- Custom KPI summary indicator cards displaying active camps, displaced persons, critical shelters, and low-stock alerts.
- Data export engine (`ExportService`) to generate clean CSV reports for camps, families, inventory, shipments, distributions, and alerts.

---

## 👥 Role-Based Access Control (RBAC)

The system features 5 distinct operational roles to ensure data integrity and security:

| Role | Access Level & Permissions | Allowed Actions |
| :--- | :--- | :--- |
| 👑 **Administrator** | Full System & Configuration | Create users, modify priority formula weights, bypass cooldowns, export all logs |
| 🚚 **Relief Coordinator** | Supply Chain & Logistics | Manage warehouses, create shipments, allocate inventory, authorize duplicate overrides |
| ⛺ **Camp Manager** | Shelter Operations & Field Care | Register camps, enroll families, record distributions, view route recommendations |
| 🤝 **Volunteer** | Field Distribution & Verification | Record family distributions, view camp priority lists and route maps |
| 👁️ **Donor / Observer** | Transparency & Audit | Read-only view of camp priorities, inventory levels, shipments, and distribution audits |

### 🔑 Pre-Seeded Default Accounts (for Instant Testing)

| Username | Password | Assigned Role | Quick Login |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **Administrator** | Instant button on login dialog |
| `coord` | `coord123` | **Relief Coordinator** | Instant button on login dialog |
| `campmgr` | `camp123` | **Camp Manager** | Instant button on login dialog |
| `vol1` | `vol123` | **Volunteer** | Instant button on login dialog |
| `donor1` | `donor123` | **Donor / Observer** | Instant button on login dialog |

---

## 🛠️ Technology Stack & Dependencies

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 17 or 21 | Core programming language |
| **GUI Framework** | Java Swing + FlatLaf | 3.5.4 | Desktop UI & theme engine |
| **Database** | Neon Serverless PostgreSQL | 16+ | Cloud relational database persistence |
| **JDBC Driver** | PostgreSQL JDBC | 42.7.5 | Relational database connectivity |
| **Connection Pool** | HikariCP | 5.1.0 | High-performance JDBC connection pooling |
| **JSON Parser** | Google Gson / Org.JSON | 2.11.0 / 20240303 | Data serialization and configs |
| **Logging** | SLF4J + SimpleLogger | 2.0.16 | Diagnostic application logging |
| **Build & Package**| Apache Maven + Shade Plugin | 3.9+ | Dependency management & Fat JAR packaging |
| **Testing** | JUnit 5 (Jupiter) | 5.10.2 | Unit, integration & concurrency testing |

---

## 🗄️ Cloud Database Setup (Neon PostgreSQL)

The application communicates with a managed Neon Serverless PostgreSQL instance.

### Configuration (`application.properties`)
Located at `src/main/resources/application.properties`:

```properties
# Cloud Database Configuration (Neon PostgreSQL)
db.url=jdbc:postgresql://ep-old-sky-aocx9bsp-pooler.c-2.ap-southeast-1.aws.neon.tech/flood_relief_db?sslmode=require
db.username=neondb_owner
db.password=npg_RPz90yKHagls
db.pool.maxSize=10
db.pool.minIdle=2
db.pool.timeout=30000

# Application Defaults
app.name=Flood Relief & Disaster Supply Chain Tracker
app.region=Haor Basin (Sylhet & Sunamganj)
app.duplicate.cooldown.days=7

# Priority Engine Weights (Normalized sum = 1.0)
priority.weight.population=0.30
priority.weight.waterlogged=0.30
priority.weight.medical=0.30
priority.weight.relief=0.10
```

### Database Tables (Auto-Bootstrapped)
The `SchemaInitializer` verifies and auto-creates 12 normalized tables:
1. `users` — Authentication credentials (PBKDF2 hashes), full names, roles, active status.
2. `warehouses` — Regional disaster storage facilities with capacity and manager contacts.
3. `camps` — Shelters across Sylhet/Sunamganj with population, capacity, waterlogged days, and medical need.
4. `families` — Registered households, family head, size, vulnerability notes.
5. `relief_items` — Inventory items with SKU, unit, expiry, thresholds, and stock buckets.
6. `shipments` — Transport consignments with origin warehouse, destination camp, ETA, and status.
7. `shipment_items` — Itemized line quantities associated with shipments.
8. `distribution_records` — Granular aid distribution logs with family ID, timestamp, and override audit notes.
9. `alerts` — System notifications for stock depletion, medical expiry, overcrowding, and transit delays.
10. `system_configurations` — Dynamic key-value pairs for engine weights and duplicate cooldown intervals.
11. `route_nodes` — Graph waypoints representing regional hubs, boat ghats, and flood shelters.
12. `route_edges` — Weighted road and waterway routes with distances and passable flags.

---

## 🚀 How to Run and Build

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or 21 installed. Verify with `java -version`.
- **Internet Access:** Required on initial run to connect to the Neon cloud database pool.

---

### Option 1: Run Directly via Maven Wrapper

#### On Windows (PowerShell):
```powershell
.\mvnw.cmd compile exec:java -Dexec.mainClass="com.floodrelief.MainApp"
```

#### On Linux / macOS (Bash):
```bash
./mvnw compile exec:java -Dexec.mainClass="com.floodrelief.MainApp"
```

---

### Option 2: Build and Run Shaded Executable JAR

Generate the self-contained fat JAR containing all dependencies and resources:

#### Build the package:
```powershell
# Windows
.\mvnw.cmd clean package

# Linux / macOS
./mvnw clean package
```

#### Run the generated JAR:
```bash
java -jar target/flood-relief-tracker-1.0.0.jar
```

---

## 🧪 Automated Testing Suite (JUnit 5)

The test suite thoroughly verifies database connectivity, mathematical scoring, thread concurrency, duplicate aid prevention, and graph algorithms.

### Run All Tests:
```powershell
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### Test Classes Summary:
- **`BootstrapDatabaseTest`**: Validates the HikariCP pool, database connectivity, and runs schema verification against the live Neon cloud database.
- **`PasswordSecurityTest`**: Tests PBKDF2 HMAC-SHA256 hashing, unique salt generation, correct credential verification, and incorrect password rejection.
- **`PriorityEngineTest`**: Validates the 4-factor normalization formula, severity label thresholds, and `PriorityQueue` sorting order.
- **`DuplicateReliefAndConcurrencyTest`**:
  - Tests inventory reservation limits and atomic concurrency safeguards.
  - Verifies that a family receiving aid is blocked from receiving duplicate aid within the 7-day cooldown.
  - Verifies that an authorized override successfully records the aid distribution with audit justification.
- **`RouteOptimizationDijkstraTest`**: Tests Dijkstra's shortest path algorithm across the Haor transportation graph, validates flooded road detection, and verifies bypass boat rerouting.
- **`DemandEstimationTest`**: Tests Sphere humanitarian standard formulas for water, caloric requirements, ORS packets, and hygiene kits.

```text
Results :
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🏷️ Git Tagging & Modular History

Development of this project followed an incremental, module-by-module architecture. The Git commit history on branch `main` is structured with annotated tags marking each functional milestone:

| Tag | Target Module | Scope & Milestone |
| :--- | :--- | :--- |
| `v1.0-module1` | **Module 1** | Maven configuration, HikariCP connection pool, and Neon PostgreSQL setup |
| `v2.0-module2` | **Module 2** | PBKDF2 password security, role permissions, and user authentication |
| `v4.0-module4` | **Module 3 & 4** | Camp registration, Haor shelter tracking, 4-factor Priority Engine & PriorityQueue |
| `v6.0-module6` | **Module 5 & 6** | Concurrency-synchronized inventory locking and 5-stage shipment lifecycle |
| `v8.0-module8` | **Module 7 & 8** | Beneficiary duplicate relief prevention and Dijkstra route optimization |
| `v10.0-module10`| **Module 9 & 10**| Sphere demand formulas, background alert daemon, FlatLaf UI & CSV export |

### Inspecting Local Tags:
```powershell
# View annotated tags with descriptions
git tag -n

# View commit graph with tags
git log --oneline --decorate -n 6
```

### Pushing to GitHub with Tags:
When connecting this repository to your remote GitHub repository, push branch `main` and all annotated tags in a single command:
```powershell
git remote add origin <your-github-repository-url>
git push -u origin main --tags
```

---

## 🎨 Interactive Features Showcase

### 1. Haor Basin 2D Interactive Route Map
- **Visual Graph Rendering:** Renders road networks and river waterways connecting Sylhet, Companiganj, Gowainghat, Tahirpur, Derai, and Sunamganj.
- **Flash Flood Simulation:** Check the "Simulate Road Flooding" option to see Dijkstra immediately recalculate alternate water routes via engine trawler boats.
- **Route Summary Panel:** Displays total distance (km), estimated transit time (hours), and turn-by-turn waypoint directions.

### 2. Live Severity Triage Board
- Real-time sorted table driven by the **Priority Engine**.
- Critical shelters requiring immediate food or medical intervention are automatically floated to the top.
- Interactive slider controls allow emergency directors to adjust factor weights in real time.

### 3. Concurrency-Safe Stock Reservation
- Operators at different dispatch hubs can allocate supplies simultaneously without risk of overcommitting inventory.
- Backed by row-level locking (**`FOR UPDATE OF r`**) in PostgreSQL transactions.

---

## 📄 License
This project is licensed under the **MIT License** — feel free to use and adapt this system for academic, disaster relief, and humanitarian supply chain purposes.
