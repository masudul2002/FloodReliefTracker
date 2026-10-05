-- ====================================================================
-- FloodReliefTracker - Database Schema (Neon PostgreSQL)
-- ====================================================================

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    role VARCHAR(50) NOT NULL,
    active_status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS warehouses (
    warehouse_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(200) NOT NULL,
    capacity_sqft INT DEFAULT 5000,
    contact_person VARCHAR(100)
);

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

CREATE TABLE IF NOT EXISTS families (
    family_id VARCHAR(50) PRIMARY KEY,
    camp_id VARCHAR(50) REFERENCES camps(camp_id) ON DELETE SET NULL,
    head_name VARCHAR(150) NOT NULL,
    member_count INT NOT NULL DEFAULT 1,
    vulnerability_notes TEXT,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE IF NOT EXISTS shipment_items (
    id SERIAL PRIMARY KEY,
    shipment_id VARCHAR(50) REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    item_id VARCHAR(50) REFERENCES relief_items(item_id),
    quantity INT NOT NULL
);

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

CREATE TABLE IF NOT EXISTS alerts (
    alert_id SERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS system_configurations (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS route_nodes (
    node_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL,
    district VARCHAR(100),
    pos_x INT DEFAULT 0,
    pos_y INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS route_edges (
    edge_id SERIAL PRIMARY KEY,
    from_node VARCHAR(50) REFERENCES route_nodes(node_id) ON DELETE CASCADE,
    to_node VARCHAR(50) REFERENCES route_nodes(node_id) ON DELETE CASCADE,
    distance_km DOUBLE PRECISION NOT NULL,
    route_type VARCHAR(50) DEFAULT 'ROAD',
    is_passable BOOLEAN DEFAULT TRUE
);
