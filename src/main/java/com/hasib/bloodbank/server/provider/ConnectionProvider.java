package com.hasib.bloodbank.server.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {
    public static Connection connection;

    public static Connection createConnection() throws ClassNotFoundException, SQLException {

        // Updated connection parameters to use correct MySQL authentication
        String user = "root";
        String password = ""; // Empty password or change to your actual MySQL root password
        String url = "jdbc:mysql://localhost:3306/blood_donation_system?createDatabaseIfNotExist=true";

        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(url, user, password);

        return connection;
    }
}
