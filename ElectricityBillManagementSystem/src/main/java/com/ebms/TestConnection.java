package com.ebms;

import com.ebms.util.DBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Connected to MySQL database successfully!");

                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("\nChecking database tables and views in 'ebms':");

                String[] types = {"TABLE", "VIEW"};
                try (ResultSet rs = meta.getTables("ebms", null, "%", types)) {
                    boolean foundAny = false;
                    while (rs.next()) {
                        foundAny = true;
                        String tableName = rs.getString("TABLE_NAME");
                        String tableType = rs.getString("TABLE_TYPE");
                        System.out.println(" - [" + tableType + "] " + tableName);
                    }
                    if (!foundAny) {
                        System.out.println(" (No tables or views found in 'ebms' database yet. schema.sql may need to be executed.)");
                    }
                }
            } else {
                System.out.println("FAILED: Could not connect to MySQL database.");
            }
        } catch (Exception e) {
            System.out.println("EXCEPTION during connection test:");
            System.out.println(e.getMessage());
        }
    }
}
