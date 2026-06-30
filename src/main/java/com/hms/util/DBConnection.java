package com.hms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;"+
            "databaseName=HospitalDB;"+"encrypt=true;"+"trustServerCertificate=true;";

    private static final String USER = "sa";
    private static final String PASSWORD = "sap";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try(Connection conn = getConnection()) {
            System.out.println("SUCCESS: Connected to HospitalDB!");
        } catch (SQLException e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}
