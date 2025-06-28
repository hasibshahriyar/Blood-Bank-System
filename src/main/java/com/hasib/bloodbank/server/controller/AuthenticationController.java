package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.singleton.User;
import com.hasib.bloodbank.server.provider.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationController {

    // Hash password for security
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to plain text if hashing fails (not recommended for production)
            return password;
        }
    }

    public static boolean authenticateWithPhoneNo(String phone, String password) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionProvider.createConnection();

            // Use parameterized query to prevent SQL injection
            String query = "SELECT p.id, p.first_name, p.last_name, p.phone_number, p.email, p.blood_group, pw.password " +
                          "FROM person p " +
                          "JOIN password pw ON p.password_id = pw.id " +
                          "WHERE p.phone_number = ? AND p.is_active = TRUE";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, phone);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int personId = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String phoneNoFromDb = resultSet.getString("phone_number");
                String emailFromDb = resultSet.getString("email");
                String bloodGroup = resultSet.getString("blood_group");
                String passwordFromDb = resultSet.getString("password");

                // Verify password (try both hashed and plain text for compatibility)
                boolean passwordMatches = password.equals(passwordFromDb) ||
                                        hashPassword(password).equals(passwordFromDb);

                if (passwordMatches && phone.equals(phoneNoFromDb)) {
                    // Update last login time
                    updateLastLogin(personId);

                    // Set user session
                    User user = User.getInstance();
                    user.setUserId(personId);
                    user.setUserPhoneNo(phoneNoFromDb);
                    user.setUserEmail(emailFromDb);
                    user.setName(firstName + " " + lastName);
                    user.setBloodGroup(bloodGroup);

                    return true;
                }
            }

            return false;

        } finally {
            // Properly close resources
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
    }

    public static boolean authenticateWithEmail(String email, String password) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionProvider.createConnection();

            // Use parameterized query to prevent SQL injection
            String query = "SELECT p.id, p.first_name, p.last_name, p.phone_number, p.email, p.blood_group, pw.password " +
                          "FROM person p " +
                          "JOIN password pw ON p.password_id = pw.id " +
                          "WHERE p.email = ? AND p.is_active = TRUE";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int personId = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String phoneNoFromDb = resultSet.getString("phone_number");
                String emailFromDb = resultSet.getString("email");
                String bloodGroup = resultSet.getString("blood_group");
                String passwordFromDb = resultSet.getString("password");

                // Verify password (try both hashed and plain text for compatibility)
                boolean passwordMatches = password.equals(passwordFromDb) ||
                                        hashPassword(password).equals(passwordFromDb);

                if (passwordMatches && email.equals(emailFromDb)) {
                    // Update last login time
                    updateLastLogin(personId);

                    // Set user session
                    User user = User.getInstance();
                    user.setUserId(personId);
                    user.setUserPhoneNo(phoneNoFromDb);
                    user.setUserEmail(emailFromDb);
                    user.setName(firstName + " " + lastName);
                    user.setBloodGroup(bloodGroup);

                    return true;
                }
            }

            return false;

        } finally {
            // Properly close resources
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
    }

    // Update last login timestamp
    private static void updateLastLogin(int personId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = ConnectionProvider.createConnection();
            String updateQuery = "UPDATE person SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setInt(1, personId);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error updating last login: " + e.getMessage());
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Method to register new user with proper password hashing
    public static boolean registerUser(String firstName, String lastName, String email, String phone,
                                     String bloodGroup, String gender, String dateOfBirth, String password) {
        Connection connection = null;
        PreparedStatement passwordStmt = null;
        PreparedStatement personStmt = null;

        try {
            connection = ConnectionProvider.createConnection();
            connection.setAutoCommit(false); // Start transaction

            // Insert password first
            String passwordQuery = "INSERT INTO password (password) VALUES (?)";
            passwordStmt = connection.prepareStatement(passwordQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            passwordStmt.setString(1, hashPassword(password));
            passwordStmt.executeUpdate();

            // Get generated password ID
            ResultSet passwordKeys = passwordStmt.getGeneratedKeys();
            int passwordId = 0;
            if (passwordKeys.next()) {
                passwordId = passwordKeys.getInt(1);
            }

            // Insert person
            String personQuery = "INSERT INTO person (first_name, last_name, email, phone_number, blood_group, gender, date_of_birth, password_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            personStmt = connection.prepareStatement(personQuery);
            personStmt.setString(1, firstName);
            personStmt.setString(2, lastName);
            personStmt.setString(3, email);
            personStmt.setString(4, phone);
            personStmt.setString(5, bloodGroup);
            personStmt.setString(6, gender);
            personStmt.setString(7, dateOfBirth);
            personStmt.setInt(8, passwordId);

            int rowsAffected = personStmt.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }

        } catch (Exception e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        } finally {
            try {
                if (personStmt != null) personStmt.close();
                if (passwordStmt != null) passwordStmt.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Method to change user password
    public static boolean changePassword(String phoneNumber, String newPassword) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = ConnectionProvider.createConnection();

            // Hash the new password
            String hashedPassword = hashPassword(newPassword);

            // Update password in database
            String updateQuery = "UPDATE password p " +
                               "JOIN person pe ON pe.password_id = p.id " +
                               "SET p.password = ? " +
                               "WHERE pe.phone_number = ? AND pe.is_active = TRUE";

            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setString(2, phoneNumber);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Method to check if email exists and return password ID
    public static int isExistEmail(String email) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionProvider.createConnection();

            String query = "SELECT pw.id FROM person p " +
                          "JOIN password pw ON p.password_id = pw.id " +
                          "WHERE p.email = ? AND p.is_active = TRUE";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }

            return 0; // Email not found

        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
    }

    // Method to check if phone exists and return password ID
    public static int isExistPhone(String phoneNumber) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionProvider.createConnection();

            String query = "SELECT pw.id FROM person p " +
                          "JOIN password pw ON p.password_id = pw.id " +
                          "WHERE p.phone_number = ? AND p.is_active = TRUE";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, phoneNumber);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }

            return 0; // Phone not found

        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
    }

    // Method to reset password using password ID
    public static boolean resetPassword(int passwordId, String newPassword) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = ConnectionProvider.createConnection();

            String hashedPassword = hashPassword(newPassword);
            String updateQuery = "UPDATE password SET password = ? WHERE id = ?";

            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setInt(2, passwordId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

}
