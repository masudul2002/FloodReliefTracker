package com.floodrelief.config;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages configuration settings for the Cloud PostgreSQL database and application defaults.
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final Properties props = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
                logger.info("Loaded configuration from application.properties");
            } else {
                try (InputStream inDb = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (inDb != null) {
                        props.load(inDb);
                        logger.info("Loaded configuration from db.properties");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load configuration properties: {}", e.getMessage());
        }
    }

    public static String getJdbcUrl() {
        // Environment variable override
        String envUrl = System.getenv("DATABASE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            if (envUrl.startsWith("postgres://")) {
                envUrl = envUrl.replace("postgres://", "jdbc:postgresql://");
            } else if (envUrl.startsWith("postgresql://")) {
                envUrl = envUrl.replace("postgresql://", "jdbc:postgresql://");
            }
            return envUrl;
        }
        return props.getProperty("db.url",
                "jdbc:postgresql://ep-old-sky-aocx9bsp-pooler.c-2.ap-southeast-1.aws.neon.tech/flood_relief_db?sslmode=require");
    }

    public static String getUsername() {
        String envUser = System.getenv("DB_USERNAME");
        if (envUser != null && !envUser.isBlank()) {
            return envUser;
        }
        return props.getProperty("db.username", "neondb_owner");
    }

    public static String getPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null && !envPass.isBlank()) {
            return envPass;
        }
        return props.getProperty("db.password", "npg_RPz90yKHagls");
    }

    public static int getPoolMaxSize() {
        try {
            return Integer.parseInt(props.getProperty("db.pool.maxSize", "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    public static int getPoolMinIdle() {
        try {
            return Integer.parseInt(props.getProperty("db.pool.minIdle", "2"));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public static int getPoolTimeout() {
        try {
            return Integer.parseInt(props.getProperty("db.pool.timeout", "30000"));
        } catch (NumberFormatException e) {
            return 30000;
        }
    }

    public static int getDefaultDuplicateCooldownDays() {
        try {
            return Integer.parseInt(props.getProperty("app.duplicate.cooldown.days", "7"));
        } catch (NumberFormatException e) {
            return 7;
        }
    }
}
