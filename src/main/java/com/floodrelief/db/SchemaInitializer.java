package com.floodrelief.db;

import com.floodrelief.model.UserRole;
import com.floodrelief.service.DatabaseManager;
import com.floodrelief.service.PasswordSecurity;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes database schema and seeds realistic baseline data for Haor basin flood relief.
 */
public class SchemaInitializer {
    private static final Logger logger = LoggerFactory.getLogger(SchemaInitializer.class);

    public static void initializeSchemaAndSeed() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            logger.info("Verifying and initializing database schema on Cloud PostgreSQL...");

            // 1. Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(50) PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    full_name VARCHAR(150),
                    role VARCHAR(50) NOT NULL,
                    active_status BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 2. Warehouses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS warehouses (
                    warehouse_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    location VARCHAR(200) NOT NULL,
                    capacity_sqft INT DEFAULT 5000,
                    contact_person VARCHAR(100)
                );
            """);

            // 3. Camps table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS camps (
                    camp_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    district VARCHAR(100) NOT NULL,
                    area VARCHAR(150) NOT NULL,
                    population INT NOT NULL DEFAULT 0,
                    capacity INT NOT NULL DEFAULT 500,
                    waterlogged_days INT NOT NULL DEFAULT 0,
                    medical_need INT NOT NULL DEFAULT 0,
                    last_relief_date TIMESTAMP,
                    severity_score DOUBLE PRECISION DEFAULT 0.0,
                    severity_label VARCHAR(30) DEFAULT 'LOW',
                    latitude DOUBLE PRECISION,
                    longitude DOUBLE PRECISION,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 4. Families table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS families (
                    family_id VARCHAR(50) PRIMARY KEY,
                    camp_id VARCHAR(50) REFERENCES camps(camp_id) ON DELETE SET NULL,
                    head_name VARCHAR(150) NOT NULL,
                    member_count INT NOT NULL DEFAULT 1,
                    vulnerability_notes TEXT,
                    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 5. Relief items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS relief_items (
                    item_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    unit VARCHAR(30) NOT NULL,
                    expiry_date DATE,
                    low_stock_threshold INT NOT NULL DEFAULT 100,
                    stock_in INT NOT NULL DEFAULT 0,
                    reserved INT NOT NULL DEFAULT 0,
                    dispatched INT NOT NULL DEFAULT 0,
                    damaged INT NOT NULL DEFAULT 0,
                    expired INT NOT NULL DEFAULT 0,
                    warehouse_id VARCHAR(50) REFERENCES warehouses(warehouse_id) ON DELETE SET NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 6. Shipments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS shipments (
                    shipment_id VARCHAR(50) PRIMARY KEY,
                    warehouse_id VARCHAR(50) REFERENCES warehouses(warehouse_id),
                    camp_id VARCHAR(50) REFERENCES camps(camp_id),
                    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                    dispatch_time TIMESTAMP,
                    expected_delivery_time TIMESTAMP,
                    actual_delivery_time TIMESTAMP,
                    responsible_user VARCHAR(100),
                    driver_contact VARCHAR(100),
                    transport_mode VARCHAR(50) DEFAULT 'TRUCK',
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 7. Shipment items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS shipment_items (
                    id SERIAL PRIMARY KEY,
                    shipment_id VARCHAR(50) REFERENCES shipments(shipment_id) ON DELETE CASCADE,
                    item_id VARCHAR(50) REFERENCES relief_items(item_id),
                    quantity INT NOT NULL
                );
            """);

            // 8. Distribution records table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS distribution_records (
                    record_id VARCHAR(50) PRIMARY KEY,
                    family_id VARCHAR(50) REFERENCES families(family_id) ON DELETE SET NULL,
                    camp_id VARCHAR(50) REFERENCES camps(camp_id) ON DELETE SET NULL,
                    item_category VARCHAR(50) NOT NULL,
                    item_id VARCHAR(50) REFERENCES relief_items(item_id),
                    quantity INT NOT NULL,
                    date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    recorded_by VARCHAR(100) NOT NULL,
                    shipment_id VARCHAR(50),
                    is_override BOOLEAN DEFAULT FALSE,
                    override_reason TEXT
                );
            """);

            // 9. Alerts table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS alerts (
                    alert_id SERIAL PRIMARY KEY,
                    type VARCHAR(50) NOT NULL,
                    message TEXT NOT NULL,
                    severity VARCHAR(30) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(30) DEFAULT 'ACTIVE'
                );
            """);

            // 10. System configurations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS system_configurations (
                    config_key VARCHAR(100) PRIMARY KEY,
                    config_value TEXT NOT NULL
                );
            """);

            // 11. Route Nodes table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS route_nodes (
                    node_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    type VARCHAR(30) NOT NULL,
                    district VARCHAR(100),
                    pos_x INT DEFAULT 0,
                    pos_y INT DEFAULT 0
                );
            """);

            // 12. Route Edges table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS route_edges (
                    edge_id SERIAL PRIMARY KEY,
                    from_node VARCHAR(50) REFERENCES route_nodes(node_id) ON DELETE CASCADE,
                    to_node VARCHAR(50) REFERENCES route_nodes(node_id) ON DELETE CASCADE,
                    distance_km DOUBLE PRECISION NOT NULL,
                    route_type VARCHAR(50) DEFAULT 'ROAD',
                    is_passable BOOLEAN DEFAULT TRUE
                );
            """);

            logger.info("Database tables initialized successfully. Checking seed data...");
            seedDataIfEmpty(conn);

        } catch (Exception e) {
            logger.error("Error during database schema initialization: {}", e.getMessage(), e);
        }
    }

    private static void seedDataIfEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Seed Users if table empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                logger.info("Seeding initial RBAC user accounts...");
                String adminHash = PasswordSecurity.hashPassword("admin123");
                String coordHash = PasswordSecurity.hashPassword("coord123");
                String campHash = PasswordSecurity.hashPassword("camp123");
                String volHash = PasswordSecurity.hashPassword("vol123");
                String donorHash = PasswordSecurity.hashPassword("donor123");

                stmt.execute(String.format("INSERT INTO users (user_id, username, password_hash, full_name, role) VALUES " +
                        "('USR-001', 'admin', '%s', 'System Administrator', '%s')," +
                        "('USR-002', 'coord', '%s', 'Shahidul Alam (Coordinator)', '%s')," +
                        "('USR-003', 'campmgr', '%s', 'Fatima Begum (Camp Manager)', '%s')," +
                        "('USR-004', 'vol1', '%s', 'Tanvir Hasan (Volunteer)', '%s')," +
                        "('USR-005', 'donor1', '%s', 'Red Crescent Observer (Donor)', '%s')",
                        adminHash, UserRole.ADMIN.name(),
                        coordHash, UserRole.RELIEF_COORDINATOR.name(),
                        campHash, UserRole.CAMP_MANAGER.name(),
                        volHash, UserRole.VOLUNTEER.name(),
                        donorHash, UserRole.DONOR.name()
                ));
            }

            // Seed System Configurations
            rs = stmt.executeQuery("SELECT COUNT(*) FROM system_configurations");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO system_configurations (config_key, config_value) VALUES
                    ('weight.population', '0.25'),
                    ('weight.waterlogged', '0.30'),
                    ('weight.medical', '0.25'),
                    ('weight.last_relief', '0.20'),
                    ('cooldown.days', '7'),
                    ('sphere.water_per_person_day', '15'),
                    ('sphere.calories_per_person_day', '2100');
                """);
            }

            // Seed Warehouses
            rs = stmt.executeQuery("SELECT COUNT(*) FROM warehouses");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO warehouses (warehouse_id, name, location, capacity_sqft, contact_person) VALUES
                    ('WH-SYL-01', 'Sylhet Central Relief Hub', 'Subidbazar, Sylhet Sadar', 15000, 'Engr. Mahfuzur Rahman (01711-123456)'),
                    ('WH-SUN-01', 'Sunamganj Disaster Depot', 'Hason Raja Road, Sunamganj Town', 10000, 'Ziaur Rahman (01819-234567)'),
                    ('WH-COM-01', 'Companiganj Forward Depot', 'Bholaganj Highway Point, Companiganj', 6000, 'Nayeem Islam (01912-345678)');
                """);
            }

            // Seed Camps (Realistic Haor region flood shelters)
            rs = stmt.executeQuery("SELECT COUNT(*) FROM camps");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO camps (camp_id, name, district, area, population, capacity, waterlogged_days, medical_need, last_relief_date, latitude, longitude) VALUES
                    ('CMP-001', 'Bhangnamari Govt Primary School Shelter', 'Sunamganj', 'Tahirpur', 1250, 1000, 14, 45, NOW() - INTERVAL '6 days', 25.089, 91.176),
                    ('CMP-002', 'Bholaganj High School Flood Center', 'Sylhet', 'Companiganj', 850, 800, 11, 28, NOW() - INTERVAL '4 days', 25.132, 91.758),
                    ('CMP-003', 'Derai Model Degree College Shelter', 'Sunamganj', 'Derai', 1600, 1400, 18, 62, NOW() - INTERVAL '8 days', 24.789, 91.354),
                    ('CMP-004', 'Gowainghat Union Parishad Complex', 'Sylhet', 'Gowainghat', 920, 900, 9, 19, NOW() - INTERVAL '2 days', 25.074, 91.973),
                    ('CMP-005', 'Bishwamvarpur Pilot High School', 'Sunamganj', 'Bishwamvarpur', 740, 700, 7, 12, NOW() - INTERVAL '1 days', 25.056, 91.312),
                    ('CMP-006', 'Kanaighat Govt College Shelter', 'Sylhet', 'Kanaighat', 1100, 1000, 12, 34, NOW() - INTERVAL '7 days', 24.981, 92.261),
                    ('CMP-007', 'Jamalganj Community Center', 'Sunamganj', 'Jamalganj', 620, 600, 6, 8, NOW() - INTERVAL '3 days', 24.996, 91.240),
                    ('CMP-008', 'Jagannathpur Poura Kalyan Shelter', 'Sunamganj', 'Jagannathpur', 480, 500, 4, 5, NOW() - INTERVAL '1 days', 24.767, 91.545);
                """);
            }

            // Seed Relief Items across categories
            rs = stmt.executeQuery("SELECT COUNT(*) FROM relief_items");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO relief_items (item_id, name, category, unit, expiry_date, low_stock_threshold, stock_in, reserved, dispatched, damaged, expired, warehouse_id) VALUES
                    ('ITM-001', 'Emergency Dry Food Ration Pack (10kg)', 'FOOD', 'packs', '2027-06-30', 200, 2500, 250, 500, 15, 0, 'WH-SYL-01'),
                    ('ITM-002', 'High-Protein Fortified Biscuits', 'FOOD', 'cartons', '2027-03-31', 100, 1200, 100, 300, 5, 0, 'WH-SYL-01'),
                    ('ITM-003', 'ORS Oral Rehydration Salt Sachet (Box of 50)', 'MEDICAL', 'boxes', '2027-12-31', 150, 1800, 200, 450, 0, 0, 'WH-SYL-01'),
                    ('ITM-004', 'Water Purification Tablets (Halazone 1000s)', 'MEDICAL', 'bottles', '2028-01-01', 100, 950, 100, 200, 2, 0, 'WH-SYL-01'),
                    ('ITM-005', 'WASH Family Hygiene Kit (Soap/Sanitary/Towel)', 'WASH', 'kits', '2028-12-31', 150, 1400, 150, 350, 8, 0, 'WH-SUN-01'),
                    ('ITM-006', 'Collapsible Clean Water Jerry Cans (10L)', 'WASH', 'cans', NULL, 120, 1600, 180, 400, 12, 0, 'WH-SUN-01'),
                    ('ITM-007', 'Reinforced Heavy-Duty Tarpaulin (12x15 ft)', 'SHELTER', 'sheets', NULL, 80, 850, 90, 210, 4, 0, 'WH-SUN-01'),
                    ('ITM-008', 'Emergency Dry Cloth Kits (Adult/Child)', 'CLOTHING', 'sets', NULL, 100, 1100, 80, 220, 6, 0, 'WH-COM-01'),
                    ('ITM-009', 'First Aid Trauma Emergency Kits', 'MEDICAL', 'kits', '2026-11-01', 50, 300, 30, 80, 1, 0, 'WH-SYL-01');
                """);
            }

            // Seed Beneficiary Families
            rs = stmt.executeQuery("SELECT COUNT(*) FROM families");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO families (family_id, camp_id, head_name, member_count, vulnerability_notes) VALUES
                    ('FAM-1001', 'CMP-001', 'Abdul Mannan', 5, 'Includes 2 elderly grandparents, infant child'),
                    ('FAM-1002', 'CMP-001', 'Renu Begum', 4, 'Female-headed household, infant child'),
                    ('FAM-1003', 'CMP-001', 'Monirul Islam', 6, 'Disabled youth member, lost home to river erosion'),
                    ('FAM-1004', 'CMP-002', 'Jalal Uddin', 5, 'Pregnant woman in household, acute food shortage'),
                    ('FAM-1005', 'CMP-002', 'Suleman Mia', 4, 'Elderly parent with chronic asthma'),
                    ('FAM-1006', 'CMP-003', 'Hajera Khatun', 7, 'Elderly widow, 4 young children'),
                    ('FAM-1007', 'CMP-003', 'Kabir Hossain', 5, 'Patient recovering from waterborne diarrhea'),
                    ('FAM-1008', 'CMP-004', 'Anwar Ali', 3, 'Fisherman family, entire boat damaged'),
                    ('FAM-1009', 'CMP-006', 'Shamsun Nahar', 6, 'Female-headed, lactating mother with twins');
                """);
            }

            // Seed Route Nodes for Haor Logistics Graph
            rs = stmt.executeQuery("SELECT COUNT(*) FROM route_nodes");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO route_nodes (node_id, name, type, district, pos_x, pos_y) VALUES
                    ('N_SYL_WH', 'Sylhet Central Hub', 'WAREHOUSE', 'Sylhet', 380, 260),
                    ('N_SUN_WH', 'Sunamganj Town Hub', 'WAREHOUSE', 'Sunamganj', 220, 180),
                    ('N_COM_WH', 'Companiganj Depot', 'WAREHOUSE', 'Sylhet', 320, 100),
                    ('N_TAHIR', 'Tahirpur Camp (CMP-001)', 'CAMP', 'Sunamganj', 120, 80),
                    ('N_BHOLA', 'Bholaganj Camp (CMP-002)', 'CAMP', 'Sylhet', 340, 50),
                    ('N_DERAI', 'Derai Camp (CMP-003)', 'CAMP', 'Sunamganj', 180, 310),
                    ('N_GOWAIN', 'Gowainghat Camp (CMP-004)', 'CAMP', 'Sylhet', 430, 90),
                    ('N_BISHWA', 'Bishwamvarpur Camp (CMP-005)', 'CAMP', 'Sunamganj', 160, 130),
                    ('N_KANAI', 'Kanaighat Camp (CMP-006)', 'CAMP', 'Sylhet', 510, 190),
                    ('N_JAGAN', 'Jagannathpur Camp (CMP-008)', 'CAMP', 'Sunamganj', 260, 330),
                    ('N_CHHATAK', 'Chhatak Transit Junction', 'WAYPOINT', 'Sunamganj', 290, 200),
                    ('N_SARIGHAT', 'Sarighat River Pier', 'WAYPOINT', 'Sylhet', 420, 150);
                """);

                stmt.execute("""
                    INSERT INTO route_edges (from_node, to_node, distance_km, route_type, is_passable) VALUES
                    ('N_SYL_WH', 'N_CHHATAK', 34.5, 'ROAD', TRUE),
                    ('N_CHHATAK', 'N_SUN_WH', 31.0, 'ROAD', TRUE),
                    ('N_SYL_WH', 'N_COM_WH', 38.0, 'ROAD', TRUE),
                    ('N_COM_WH', 'N_BHOLA', 12.0, 'ROAD', TRUE),
                    ('N_SYL_WH', 'N_SARIGHAT', 32.0, 'ROAD', TRUE),
                    ('N_SARIGHAT', 'N_GOWAIN', 16.5, 'ROAD', TRUE),
                    ('N_SARIGHAT', 'N_KANAI', 26.0, 'ROAD', TRUE),
                    ('N_SUN_WH', 'N_BISHWA', 18.0, 'ROAD', TRUE),
                    ('N_BISHWA', 'N_TAHIR', 22.0, 'WATERWAY', TRUE),
                    ('N_SUN_WH', 'N_TAHIR', 38.0, 'WATERWAY', TRUE),
                    ('N_SUN_WH', 'N_DERAI', 36.0, 'ROAD', TRUE),
                    ('N_DERAI', 'N_JAGAN', 24.0, 'ROAD', TRUE),
                    ('N_SYL_WH', 'N_JAGAN', 42.0, 'ROAD', TRUE),
                    ('N_COM_WH', 'N_GOWAIN', 28.0, 'WATERWAY', TRUE);
                """);
            }

            logger.info("Database schema and baseline seed verification completed successfully.");
        } catch (Exception e) {
            logger.error("Error seeding initial data: {}", e.getMessage(), e);
        }
    }
}
