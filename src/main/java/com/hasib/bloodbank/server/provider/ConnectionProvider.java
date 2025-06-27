package com.hasib.bloodbank.server.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {
    public static Connection connection;

    public static Connection createConnection() throws ClassNotFoundException, SQLException {

        // Try different common MySQL configurations
        String[] passwords = {"", "root", "password", "admin", "123456"};
        String user = "root";
        String url = "jdbc:mysql://localhost:3306/blood_donation_system?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        Class.forName("com.mysql.cj.jdbc.Driver");

        // Try connecting with different passwords
        SQLException lastException = null;
        for (String password : passwords) {
            try {
                System.out.println("Trying to connect with password: " + (password.isEmpty() ? "[empty]" : "[hidden]"));
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("✅ Successfully connected to database!");
                return connection;
            } catch (SQLException e) {
                lastException = e;
                System.out.println("❌ Failed with password: " + (password.isEmpty() ? "[empty]" : "[hidden]"));
            }
        }

        // If all attempts failed, throw the last exception
        System.err.println("Failed to connect with any common passwords. Please check your MySQL configuration.");
        throw lastException;
    }
}
