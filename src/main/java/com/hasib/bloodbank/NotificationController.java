package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.ChatController;
import com.hasib.bloodbank.server.provider.ConnectionProvider;
import com.hasib.bloodbank.singleton.User;
import com.hasib.bloodbank.utils.ThreadPoolManager;
import com.hasib.bloodbank.utils.NotificationManager;
import javafx.application.Platform;
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
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();
    private final NotificationManager notificationManager = NotificationManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadNotifications();

        // Schedule periodic notification refresh
        threadPool.schedulePeriodicTask(() -> {
            Platform.runLater(this::loadNotifications);
        }, 30, 30, java.util.concurrent.TimeUnit.SECONDS);
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
        threadPool.executeDatabaseTask(() -> {
            try {
                ObservableList<Notification> notifications = getNotificationsFromDatabase();
                int unreadCount = getUnreadNotificationCount();

                Platform.runLater(() -> {
                    requestsTable.setItems(notifications);
                    myRequestsTable.setItems(notifications);
                    notificationCountLabel.setText("Unread: " + unreadCount);

                    // Debug information
                    System.out.println("Loaded " + notifications.size() + " notifications for user ID: " + user.getUserId());
                    System.out.println("Unread notifications: " + unreadCount);
                });

            } catch (SQLException | ClassNotFoundException e) {
                System.err.println("Error loading notifications: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Database Error", "Failed to load notifications: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
    }

    private ObservableList<Notification> getNotificationsFromDatabase() throws SQLException, ClassNotFoundException {
        ObservableList<Notification> notifications = FXCollections.observableArrayList();

        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT notification_id, notification_type, title, message, created_date, is_read " +
                "FROM notifications WHERE person_id = ? ORDER BY created_date DESC";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, user.getUserId());

        System.out.println("Querying notifications for user ID: " + user.getUserId());

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
            System.out.println("Found notification: " + notification.getTitle());
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
            threadPool.executeDatabaseTask(() -> {
                try {
                    // Mark notification as read
                    markNotificationAsRead(selectedNotification.getNotificationId());

                    if ("Blood_Request".equals(selectedNotification.getNotificationType())) {
                        // Update the blood request status to ACCEPTED
                        updateBloodRequestStatus(selectedNotification.getNotificationId(), "ACCEPTED");

                        // Get requester information for detailed notification
                        int requesterId = getRequesterFromNotification(selectedNotification.getNotificationId());
                        String donorName = getDonorNameFromUserId(user.getUserId());
                        String donorPhone = getDonorPhoneFromUserId(user.getUserId());

                        if (requesterId > 0) {
                            // Create detailed acceptance notification for the requester
                            String acceptanceMessage = String.format(
                                "🎉 Great News! Your blood request has been ACCEPTED!\n\n" +
                                "✅ Donor: %s\n" +
                                "📞 Contact: %s\n" +
                                "⏰ Please coordinate the donation time and location.\n\n" +
                                "💝 Thank you for using our Blood Bank system!",
                                donorName, donorPhone
                            );

                            createResponseNotification(requesterId, "Blood Request Accepted", acceptanceMessage, "ACCEPTED");

                            // Also create a chat message for real-time communication
                            try {
                                ChatController.sendBloodRequestMessage(
                                    user.getUserId(),
                                    requesterId,
                                    "I have accepted your blood request. Let's coordinate!",
                                    "ACCEPTED"
                                );
                            } catch (Exception chatError) {
                                System.err.println("Chat notification failed: " + chatError.getMessage());
                            }
                        }

                        Platform.runLater(() -> {
                            showAlert("Success", "Blood request accepted successfully! The requester has been notified with your contact details.", Alert.AlertType.INFORMATION);
                            loadNotifications(); // Refresh the list
                        });
                    } else {
                        Platform.runLater(() -> {
                            showAlert("Success", "Notification marked as read.", Alert.AlertType.INFORMATION);
                            loadNotifications(); // Refresh the list
                        });
                    }

                } catch (SQLException | ClassNotFoundException e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to process acceptance: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                }
            });
        } else {
            showAlert("No Selection", "Please select a blood request notification to accept.", Alert.AlertType.WARNING);
        }
    }

    public void onDeclineButtonClicked(ActionEvent actionEvent) {
        Notification selectedNotification = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedNotification != null) {
            threadPool.executeDatabaseTask(() -> {
                try {
                    // Mark notification as read
                    markNotificationAsRead(selectedNotification.getNotificationId());

                    if ("Blood_Request".equals(selectedNotification.getNotificationType())) {
                        // Update the blood request status to DECLINED
                        updateBloodRequestStatus(selectedNotification.getNotificationId(), "DECLINED");

                        // Get requester information for detailed notification
                        int requesterId = getRequesterFromNotification(selectedNotification.getNotificationId());
                        String donorName = getDonorNameFromUserId(user.getUserId());

                        if (requesterId > 0) {
                            // Create detailed decline notification for the requester
                            String declineMessage = String.format(
                                "😔 Blood Request Update\n\n" +
                                "❌ Unfortunately, %s cannot fulfill your blood request at this time.\n\n" +
                                "💡 Suggestions:\n" +
                                "• Try contacting other available donors\n" +
                                "• Check nearby blood banks\n" +
                                "• Post your request in emergency groups\n\n" +
                                "🤝 Don't give up - there are many willing donors in our community!",
                                donorName
                            );

                            createResponseNotification(requesterId, "Blood Request Declined", declineMessage, "DECLINED");

                            // Send chat message for real-time communication
                            try {
                                ChatController.sendMessage(
                                    ChatController.getOrCreatePrivateConversation(user.getUserId(), requesterId),
                                    user.getUserId(),
                                    "I'm sorry, but I cannot fulfill your blood request right now. Please try other donors.",
                                    "TEXT"
                                );
                            } catch (Exception chatError) {
                                System.err.println("Chat notification failed: " + chatError.getMessage());
                            }
                        }

                        Platform.runLater(() -> {
                            showAlert("Request Declined", "Blood request declined. The requester has been notified to seek alternative donors.", Alert.AlertType.INFORMATION);
                            loadNotifications(); // Refresh the list
                        });
                    } else {
                        Platform.runLater(() -> {
                            showAlert("Success", "Notification marked as read.", Alert.AlertType.INFORMATION);
                            loadNotifications(); // Refresh the list
                        });
                    }

                } catch (SQLException | ClassNotFoundException e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to process decline: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                }
            });
        } else {
            showAlert("No Selection", "Please select a blood request notification to decline.", Alert.AlertType.WARNING);
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

    private void createResponseNotification(int personId, String title, String message, String status) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        String query = "INSERT INTO notifications (person_id, notification_type, title, message, is_read, created_date) " +
                "VALUES (?, 'Donation_Response', ?, ?, FALSE, CURRENT_TIMESTAMP)";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, personId);
        stmt.setString(2, title);
        stmt.setString(3, message);
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

    private String getDonorNameFromUserId(int userId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT CONCAT(first_name, ' ', last_name) as full_name FROM person WHERE id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);
        ResultSet resultSet = preparedStatement.executeQuery();

        String name = "Unknown User";
        if (resultSet.next()) {
            name = resultSet.getString("full_name");
        }

        connection.close();
        return name;
    }

    private String getDonorPhoneFromUserId(int userId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT phone_number FROM person WHERE id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, userId);
        ResultSet resultSet = preparedStatement.executeQuery();

        String phone = "Contact via app";
        if (resultSet.next()) {
            phone = resultSet.getString("phone_number");
        }

        connection.close();
        return phone;
    }
}
