package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.provider.ConnectionProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BloodRequestController {

    public static class BloodRequest {
        private int id;
        private int requesterId;
        private String requesterName;
        private String bloodGroup;
        private String message;
        private String requestDate;
        private String status;
        private String requesterPhone;
        private String requesterEmail;

        public BloodRequest(int id, int requesterId, String requesterName, String bloodGroup,
                            String message, String requestDate, String status, String requesterPhone, String requesterEmail) {
            this.id = id;
            this.requesterId = requesterId;
            this.requesterName = requesterName;
            this.bloodGroup = bloodGroup;
            this.message = message;
            this.requestDate = requestDate;
            this.status = status;
            this.requesterPhone = requesterPhone;
            this.requesterEmail = requesterEmail;
        }

        // Getters
        public int getId() { return id; }
        public int getRequesterId() { return requesterId; }
        public String getRequesterName() { return requesterName; }
        public String getBloodGroup() { return bloodGroup; }
        public String getMessage() { return message; }
        public String getRequestDate() { return requestDate; }
        public String getStatus() { return status; }
        public String getRequesterPhone() { return requesterPhone; }
        public String getRequesterEmail() { return requesterEmail; }
    }

    public static boolean sendBloodRequest(int requesterId, int donorId, String message) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            // Insert blood request
            String requestQuery = "INSERT INTO blood_requests (requester_id, donor_id, message, request_date, status) VALUES (?, ?, ?, ?, 'PENDING')";
            PreparedStatement requestStmt = connection.prepareStatement(requestQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            requestStmt.setInt(1, requesterId);
            requestStmt.setInt(2, donorId);
            requestStmt.setString(3, message);
            requestStmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            int requestResult = requestStmt.executeUpdate();

            if (requestResult > 0) {
                // Get the generated blood request ID
                ResultSet generatedKeys = requestStmt.getGeneratedKeys();
                int bloodRequestId = 0;
                if (generatedKeys.next()) {
                    bloodRequestId = generatedKeys.getInt(1);
                }

                // Get requester's name for the notification
                String requesterName = getPersonName(requesterId, connection);

                // Create notification for the donor
                String notificationTitle = "New Blood Request from " + requesterName;
                String notificationMessage = message + "\n\nClick to view details and respond.";

                createNotification(donorId, "Blood_Request", notificationTitle, notificationMessage, bloodRequestId, connection);

                // Commit transaction
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    public static ObservableList<BloodRequest> getBloodRequestsForDonor(int donorId) throws SQLException, ClassNotFoundException {
        String query = "SELECT br.id, br.requester_id, p.first_name, p.last_name, p.blood_group, " +
                "br.message, br.request_date, br.status, p.phone_number, p.email " +
                "FROM blood_requests br JOIN person p ON br.requester_id = p.id " +
                "WHERE br.donor_id = ? ORDER BY br.request_date DESC";

        ObservableList<BloodRequest> requests = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, donorId);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String requesterName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
            requests.add(new BloodRequest(
                    resultSet.getInt("id"),
                    resultSet.getInt("requester_id"),
                    requesterName,
                    resultSet.getString("blood_group"),
                    resultSet.getString("message"),
                    resultSet.getString("request_date"),
                    resultSet.getString("status"),
                    resultSet.getString("phone_number"),
                    resultSet.getString("email")
            ));
        }

        return requests;
    }

    public static boolean respondToBloodRequest(int requestId, String response) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "UPDATE blood_requests SET status = ? WHERE id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, response);
        preparedStatement.setInt(2, requestId);

        return preparedStatement.executeUpdate() > 0;
    }

    public static ObservableList<BloodRequest> getMyBloodRequests(int requesterId) throws SQLException, ClassNotFoundException {
        String query = "SELECT br.id, br.donor_id as requester_id, p.first_name, p.last_name, p.blood_group, " +
                "br.message, br.request_date, br.status, p.phone_number, p.email " +
                "FROM blood_requests br JOIN person p ON br.donor_id = p.id " +
                "WHERE br.requester_id = ? ORDER BY br.request_date DESC";

        ObservableList<BloodRequest> requests = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, requesterId);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String donorName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
            requests.add(new BloodRequest(
                    resultSet.getInt("id"),
                    resultSet.getInt("requester_id"),
                    donorName,
                    resultSet.getString("blood_group"),
                    resultSet.getString("message"),
                    resultSet.getString("request_date"),
                    resultSet.getString("status"),
                    resultSet.getString("phone_number"),
                    resultSet.getString("email")
            ));
        }

        return requests;
    }

    public static void createBloodRequestsTable() throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS blood_requests (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "requester_id INT NOT NULL, " +
                "donor_id INT NOT NULL, " +
                "message TEXT, " +
                "request_date VARCHAR(50), " +
                "status VARCHAR(20) DEFAULT 'PENDING', " +
                "FOREIGN KEY (requester_id) REFERENCES person(id), " +
                "FOREIGN KEY (donor_id) REFERENCES person(id)" +
                ")";

        PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery);
        preparedStatement.execute();
    }

    private static String getPersonName(int personId, Connection connection) throws SQLException {
        String query = "SELECT first_name, last_name FROM person WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, personId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("first_name") + " " + rs.getString("last_name");
        }
        return "Unknown User";
    }

    private static void createNotification(int personId, String notificationType, String title, String message, int relatedId, Connection connection) throws SQLException {
        String query = "INSERT INTO notifications (person_id, notification_type, title, message, related_id, is_read, created_date) VALUES (?, ?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, personId);
        stmt.setString(2, notificationType);
        stmt.setString(3, title);
        stmt.setString(4, message);
        stmt.setInt(5, relatedId);
        stmt.executeUpdate();
    }
}
