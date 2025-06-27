package com.hasib.bloodbank;

import com.hasib.bloodbank.server.provider.ConnectionProvider;
import com.hasib.bloodbank.singleton.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class NotificationController implements Initializable {

    // Simple Notification class inside this controller
    public static class Notification {
        private final int notificationId;
        private final String notificationType;
        private final String title;
        private final String message;
        private final String createdDate;
        private final boolean isRead;

        public Notification(int notificationId, String notificationType, String title, String message, String createdDate, boolean isRead) {
            this.notificationId = notificationId;
            this.notificationType = notificationType;
            this.title = title;
            this.message = message;
            this.createdDate = createdDate;
            this.isRead = isRead;
        }

        public int getNotificationId() { return notificationId; }
        public String getNotificationType() { return notificationType; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getCreatedDate() { return createdDate; }
        public boolean isRead() { return isRead; }
    }

    public TableView<Notification> requestsTable;
    public TableColumn<Notification, String> requesterNameColumn;
    public TableColumn<Notification, String> bloodGroupColumn;
    public TableColumn<Notification, String> messageColumn;
    public TableColumn<Notification, String> dateColumn;
    public TableColumn<Notification, String> statusColumn;
    public TableColumn<Notification, String> phoneColumn;

    public Button acceptButton;
    public Button declineButton;
    public Button refreshButton;
    public Label notificationCountLabel;
    public TabPane tabPane;

    // For "My Requests" tab
    public TableView<Notification> myRequestsTable;
    public TableColumn<Notification, String> donorNameColumn;
    public TableColumn<Notification, String> myBloodGroupColumn;
    public TableColumn<Notification, String> myMessageColumn;
    public TableColumn<Notification, String> myDateColumn;
    public TableColumn<Notification, String> myStatusColumn;
    public TableColumn<Notification, String> donorPhoneColumn;

    private final User user = User.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadNotifications();
    }

    private void setupTableColumns() {
        // Main notifications table
        requesterNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("notificationType"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean isRead = cellData.getValue().isRead();
            return new javafx.beans.property.SimpleStringProperty(isRead ? "Read" : "Unread");
        });

        // My requests table (can show same data or filter by type)
        donorNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        myBloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("notificationType"));
        myMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        myDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        myStatusColumn.setCellValueFactory(cellData -> {
            boolean isRead = cellData.getValue().isRead();
            return new javafx.beans.property.SimpleStringProperty(isRead ? "Read" : "Unread");
        });
    }

    public void loadNotifications() {
        try {
            ObservableList<Notification> notifications = getNotificationsFromDatabase();
            requestsTable.setItems(notifications);
            myRequestsTable.setItems(notifications);

            // Update notification count
            int unreadCount = getUnreadNotificationCount();
            notificationCountLabel.setText("Unread: " + unreadCount);

        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Database Error", "Failed to load notifications: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private ObservableList<Notification> getNotificationsFromDatabase() throws SQLException, ClassNotFoundException {
        ObservableList<Notification> notifications = FXCollections.observableArrayList();

        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT notification_id, notification_type, title, message, created_date, is_read " +
                "FROM notifications WHERE person_id = ? ORDER BY created_date DESC";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, user.getUserId());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            Notification notification = new Notification(
                    resultSet.getInt("notification_id"),
                    resultSet.getString("notification_type"),
                    resultSet.getString("title"),
                    resultSet.getString("message"),
                    resultSet.getString("created_date"),
                    resultSet.getBoolean("is_read")
            );
            notifications.add(notification);
        }

        connection.close();
        return notifications;
    }

    private int getUnreadNotificationCount() throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT COUNT(*) as count FROM notifications WHERE person_id = ? AND is_read = FALSE";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, user.getUserId());
        ResultSet resultSet = preparedStatement.executeQuery();

        int count = 0;
        if (resultSet.next()) {
            count = resultSet.getInt("count");
        }

        connection.close();
        return count;
    }

    private void markNotificationAsRead(int notificationId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "UPDATE notifications SET is_read = TRUE, read_date = CURRENT_TIMESTAMP WHERE notification_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, notificationId);
        preparedStatement.executeUpdate();
        connection.close();
    }

    public void onAcceptButtonClicked(ActionEvent actionEvent) {
        Notification selectedNotification = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedNotification != null) {
            try {
                markNotificationAsRead(selectedNotification.getNotificationId());

                if ("Blood_Request".equals(selectedNotification.getNotificationType())) {
                    // Update the blood request status to ACCEPTED
                    updateBloodRequestStatus(selectedNotification.getNotificationId(), "ACCEPTED");

                    // Create notification for the requester
                    int requesterId = getRequesterFromNotification(selectedNotification.getNotificationId());
                    if (requesterId > 0) {
                        createResponseNotification(requesterId, "Your blood request has been ACCEPTED! The donor will contact you soon.");
                    }

                    showAlert("Success", "Blood request accepted successfully! The requester has been notified.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Success", "Notification marked as read.", Alert.AlertType.INFORMATION);
                }

                loadNotifications(); // Refresh the list
            } catch (SQLException | ClassNotFoundException e) {
                showAlert("Error", "Failed to process request: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("No Selection", "Please select a notification to accept.", Alert.AlertType.WARNING);
        }
    }

    public void onDeclineButtonClicked(ActionEvent actionEvent) {
        Notification selectedNotification = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedNotification != null) {
            try {
                markNotificationAsRead(selectedNotification.getNotificationId());

                if ("Blood_Request".equals(selectedNotification.getNotificationType())) {
                    // Update the blood request status to DECLINED
                    updateBloodRequestStatus(selectedNotification.getNotificationId(), "DECLINED");

                    // Create notification for the requester
                    int requesterId = getRequesterFromNotification(selectedNotification.getNotificationId());
                    if (requesterId > 0) {
                        createResponseNotification(requesterId, "Your blood request has been declined. Please try contacting other donors.");
                    }

                    showAlert("Success", "Blood request declined. The requester has been notified.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Success", "Notification marked as read.", Alert.AlertType.INFORMATION);
                }

                loadNotifications(); // Refresh the list
            } catch (SQLException | ClassNotFoundException e) {
                showAlert("Error", "Failed to process request: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("No Selection", "Please select a notification to decline.", Alert.AlertType.WARNING);
        }
    }

    private void updateBloodRequestStatus(int notificationId, String status) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        // First get the blood request ID from the notification
        String getRequestQuery = "SELECT related_id FROM notifications WHERE notification_id = ?";
        PreparedStatement getStmt = connection.prepareStatement(getRequestQuery);
        getStmt.setInt(1, notificationId);
        ResultSet rs = getStmt.executeQuery();

        if (rs.next()) {
            int bloodRequestId = rs.getInt("related_id");

            // Update the blood request status
            String updateQuery = "UPDATE blood_requests SET status = ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, status);
            updateStmt.setInt(2, bloodRequestId);
            updateStmt.executeUpdate();
        }

        connection.close();
    }

    private int getRequesterFromNotification(int notificationId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        String query = "SELECT br.requester_id FROM notifications n " +
                "JOIN blood_requests br ON n.related_id = br.id " +
                "WHERE n.notification_id = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, notificationId);
        ResultSet rs = stmt.executeQuery();

        int requesterId = 0;
        if (rs.next()) {
            requesterId = rs.getInt("requester_id");
        }

        connection.close();
        return requesterId;
    }

    private void createResponseNotification(int personId, String message) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        String query = "INSERT INTO notifications (person_id, notification_type, title, message, is_read, created_date) " +
                "VALUES (?, 'Donation_Response', 'Blood Request Response', ?, FALSE, CURRENT_TIMESTAMP)";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, personId);
        stmt.setString(2, message);
        stmt.executeUpdate();

        connection.close();
    }

    public void onRefreshButtonClicked(ActionEvent actionEvent) {
        loadNotifications();
        showAlert("Refreshed", "Notifications have been refreshed.", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
