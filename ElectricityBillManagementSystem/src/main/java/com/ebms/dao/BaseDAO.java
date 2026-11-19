package com.ebms.dao;

import com.ebms.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Base DAO class with common database operations.
 * All DAO classes should extend this to enforce prepared statement usage.
 */
public abstract class BaseDAO {

    /**
     * Get a database connection.
     * Use with try-with-resources.
     */
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    /**
     * Close database resources safely.
     */
    protected void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Execute a non-query SQL statement (INSERT, UPDATE, DELETE).
     * Enforces prepared statement usage.
     *
     * @param sql The SQL query with ? placeholders
     * @param params Parameters to bind to the prepared statement
     * @return Number of rows affected
     * @throws SQLException if execution fails
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        }
    }

    /**
     * Set parameters on a prepared statement.
     * Prevents SQL injection by using typed setters.
     */
    protected void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
