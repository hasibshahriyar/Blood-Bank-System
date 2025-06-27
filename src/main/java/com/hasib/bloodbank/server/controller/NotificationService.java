package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.provider.ConnectionProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationService {

    public static class Notification {
        private final int notificationId;
        private final int personId;
        private final String notificationType;
        private final String title;
        private final String message;
        private final Integer relatedId;
        private boolean isRead;
        private final String createdDate;
        private String readDate;

        public Notification(int notificationId, int personId, String notificationType, String title,
                            String message, Integer relatedId, boolean isRead, String createdDate, String readDate) {
            this.notificationId = notificationId;
            this.personId = personId;
            this.notificationType = notificationType;
            this.title = title;
            this.message = message;
            this.relatedId = relatedId;
            this.isRead = isRead;
            this.createdDate = createdDate;
            this.readDate = readDate;
        }

        // Getters
        public int getNotificationId() { return notificationId; }
        public int getPersonId() { return personId; }
        public String getNotificationType() { return notificationType; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public Integer getRelatedId() { return relatedId; }
        public boolean isRead() { return isRead; }
        public String getCreatedDate() { return createdDate; }
        public String getReadDate() { return readDate; }

        // Setters
        public void setRead(boolean read) { isRead = read; }
        public void setReadDate(String readDate) { this.readDate = readDate; }
    }

    /**
     * Get all notifications for a specific person
     */
    public static ObservableList<Notification> getNotificationsForPerson(int personId) throws SQLException, ClassNotFoundException {
        ObservableList<Notification> notifications = FXCollections.observableArrayList();

        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT notification_id, person_id, notification_type, title, message, " +
                "related_id, is_read, created_date, read_date " +
                "FROM notifications WHERE person_id = ? ORDER BY created_date DESC";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, personId);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            Notification notification = new Notification(
                    resultSet.getInt("notification_id"),
                    resultSet.getInt("person_id"),
                    resultSet.getString("notification_type"),
                    resultSet.getString("title"),
                    resultSet.getString("message"),
                    resultSet.getObject("related_id", Integer.class),
                    resultSet.getBoolean("is_read"),
                    resultSet.getString("created_date"),
                    resultSet.getString("read_date")
            );
            notifications.add(notification);
        }

        connection.close();
        return notifications;
    }

    /**
     * Get unread notifications count for a person
     */
    public static int getUnreadNotificationCount(int personId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "SELECT COUNT(*) as count FROM notifications WHERE person_id = ? AND is_read = FALSE";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, personId);
        ResultSet resultSet = preparedStatement.executeQuery();

        int count = 0;
        if (resultSet.next()) {
            count = resultSet.getInt("count");
        }

        connection.close();
        return count;
    }

    /**
     * Mark a notification as read
     */
    public static boolean markNotificationAsRead(int notificationId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "UPDATE notifications SET is_read = TRUE, read_date = CURRENT_TIMESTAMP WHERE notification_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, notificationId);
        int rowsAffected = preparedStatement.executeUpdate();

        connection.close();
        return rowsAffected > 0;
    }

    /**
     * Mark all notifications as read for a person
     */
    public static boolean markAllNotificationsAsRead(int personId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "UPDATE notifications SET is_read = TRUE, read_date = CURRENT_TIMESTAMP WHERE person_id = ? AND is_read = FALSE";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, personId);
        int rowsAffected = preparedStatement.executeUpdate();

        connection.close();
        return rowsAffected > 0;
    }

    /**
     * Create a new notification
     */
    public static boolean createNotification(int personId, String notificationType, String title,
                                             String message, Integer relatedId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();
        String query = "INSERT INTO notifications (person_id, notification_type, title, message, related_id) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, personId);
        preparedStatement.setString(2, notificationType);
        preparedStatement.setString(3, title);
        preparedStatement.setString(4, message);
        if (relatedId != null) {
            preparedStatement.setInt(5, relatedId);
        } else {
            preparedStatement.setNull(5, java.sql.Types.INTEGER);
        }

        int rowsAffected = preparedStatement.executeUpdate();
        connection.close();

        return rowsAffected > 0;
    }
}
