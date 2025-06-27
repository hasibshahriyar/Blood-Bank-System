package com.hasib.bloodbank.utils;

import com.hasib.bloodbank.server.controller.BloodRequestController;
import javafx.application.Platform;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe notification manager for handling real-time notifications
 */
public class NotificationManager {
    private static NotificationManager instance;
    private final ConcurrentLinkedQueue<NotificationEvent> notificationQueue;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final ThreadPoolManager threadPool;

    public static class NotificationEvent {
        private final int recipientId;
        private final String type;
        private final String title;
        private final String message;
        private final int relatedId;
        private final long timestamp;

        public NotificationEvent(int recipientId, String type, String title, String message, int relatedId) {
            this.recipientId = recipientId;
            this.type = type;
            this.title = title;
            this.message = message;
            this.relatedId = relatedId;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public int getRecipientId() { return recipientId; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public int getRelatedId() { return relatedId; }
        public long getTimestamp() { return timestamp; }
    }

    private NotificationManager() {
        this.notificationQueue = new ConcurrentLinkedQueue<>();
        this.threadPool = ThreadPoolManager.getInstance();
        startNotificationProcessor();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addNotification(int recipientId, String type, String title, String message, int relatedId) {
        NotificationEvent event = new NotificationEvent(recipientId, type, title, message, relatedId);
        notificationQueue.offer(event);

        System.out.println("Notification queued for user " + recipientId + ": " + title);

        // Trigger processing if not already running
        if (!isProcessing.get()) {
            processNotifications();
        }
    }

    private void startNotificationProcessor() {
        // Schedule periodic notification processing
        threadPool.schedulePeriodicTask(() -> {
            if (!notificationQueue.isEmpty()) {
                processNotifications();
            }
        }, 1, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void processNotifications() {
        if (isProcessing.compareAndSet(false, true)) {
            threadPool.executeNotificationTask(() -> {
                try {
                    processNotificationQueue();
                } finally {
                    isProcessing.set(false);
                }
            });
        }
    }

    private void processNotificationQueue() {
        lock.lock();
        try {
            while (!notificationQueue.isEmpty()) {
                NotificationEvent event = notificationQueue.poll();
                if (event != null) {
                    processNotificationEvent(event);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void processNotificationEvent(NotificationEvent event) {
        // Store notification in database
        threadPool.executeDatabaseTask(() -> {
            try {
                BloodRequestController.createNotificationsTable();

                // Create notification in database
                java.sql.Connection connection = com.hasib.bloodbank.server.provider.ConnectionProvider.createConnection();
                String query = "INSERT INTO notifications (person_id, notification_type, title, message, related_id, is_read, created_date) " +
                        "VALUES (?, ?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

                java.sql.PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, event.getRecipientId());
                stmt.setString(2, event.getType());
                stmt.setString(3, event.getTitle());
                stmt.setString(4, event.getMessage());
                stmt.setInt(5, event.getRelatedId());

                int result = stmt.executeUpdate();
                connection.close();

                System.out.println("Notification stored in database for user " + event.getRecipientId() +
                        ": " + (result > 0 ? "SUCCESS" : "FAILED"));

                // Update UI on JavaFX Application Thread if needed
                Platform.runLater(() -> {
                    // Notify UI components that new notification is available
                    notifyUIComponents(event);
                });

            } catch (Exception e) {
                System.err.println("Error processing notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void notifyUIComponents(NotificationEvent event) {
        // This method can be used to update UI components when new notifications arrive
        // For example, updating notification badges, showing toast messages, etc.
        System.out.println("UI notification: " + event.getTitle() + " for user " + event.getRecipientId());
    }

    public void refreshNotificationsForUser(int userId) {
        threadPool.executeNotificationTask(() -> {
            Platform.runLater(() -> {
                // Trigger UI refresh for specific user
                System.out.println("Refreshing notifications for user: " + userId);
            });
        });
    }

    public void shutdown() {
        System.out.println("Shutting down NotificationManager...");
        // Process remaining notifications before shutdown
        processNotificationQueue();
    }
}
