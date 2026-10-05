package com.floodrelief.service;

import com.floodrelief.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages database connections using HikariCP connection pool to the Neon PostgreSQL cloud database.
 * Located in com.floodrelief.service as per architectural design.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;
    private static boolean isConnected = false;
    private static String connectionMessage = "Not connected";

    static {
        initialize();
    }

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DatabaseConfig.getJdbcUrl());
            config.setUsername(DatabaseConfig.getUsername());
            config.setPassword(DatabaseConfig.getPassword());
            config.setMaximumPoolSize(DatabaseConfig.getPoolMaxSize());
            config.setMinimumIdle(DatabaseConfig.getPoolMinIdle());
            config.setConnectionTimeout(DatabaseConfig.getPoolTimeout());
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setPoolName("FloodReliefHikariPool");

            // PostgreSQL Driver optimization properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    isConnected = true;
                    connectionMessage = "Connected to Cloud PostgreSQL (" + conn.getCatalog() + ")";
                    logger.info("Successfully connected to database pool: {}", connectionMessage);
                }
            }
        } catch (Exception e) {
            isConnected = false;
            connectionMessage = "Connection failed: " + e.getMessage();
            logger.error("Failed to initialize database connection: {}", e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        if (dataSource == null) {
            throw new SQLException("Database connection pool is uninitialized.");
        }
        return dataSource.getConnection();
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static String getConnectionMessage() {
        return connectionMessage;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            isConnected = false;
            logger.info("Database connection pool closed.");
        }
    }
}
