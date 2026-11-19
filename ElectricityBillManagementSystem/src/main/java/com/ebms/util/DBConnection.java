package com.ebms.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility class.
 * Manages MySQL connections using credentials from db.properties file.
 * Uses try-with-resources for automatic connection cleanup.
 */
public class DBConnection {

    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static boolean initialized = false;

    // Private constructor to prevent instantiation
    private DBConnection() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Load database configuration from properties file.
     * Falls back to environment variables if file is not found.
     */
    private static void initialize() {
        if (initialized) {
            return;
        }

        Properties props = new Properties();

        // Try to load from db.properties file first
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input != null) {
                props.load(input);
                DB_URL = props.getProperty("db.url");
                DB_USERNAME = props.getProperty("db.username");
                DB_PASSWORD = props.getProperty("db.password");
            } else {
                // Fallback to environment variables
                System.err.println("db.properties not found. Using environment variables.");
                DB_URL = System.getenv("EBMS_DB_URL");
                DB_USERNAME = System.getenv("EBMS_DB_USERNAME");
                DB_PASSWORD = System.getenv("EBMS_DB_PASSWORD");
            }

            // Validate configuration
            if (DB_URL == null || DB_USERNAME == null || DB_PASSWORD == null) {
                throw new IllegalStateException(
                    "Database configuration is incomplete. Check db.properties or environment variables."
                );
            }

            initialized = true;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Get a new database connection.
     * Use with try-with-resources for automatic cleanup.
     *
     * Example:
     * try (Connection conn = DBConnection.getConnection()) {
     *     // Use connection
     * } catch (SQLException e) {
     *     // Handle exception
     * }
     *
     * @return A new Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        initialize();

        try {
            // Load MySQL JDBC driver (optional in newer JDBC versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }

        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    /**
     * Test database connectivity.
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
