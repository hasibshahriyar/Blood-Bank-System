package com.hasib.bloodbank.server.controller;

import com.hasib.bloodbank.server.provider.ConnectionProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Database-backed Chat Controller to replace socket-based chat system
 * Provides reliable messaging without dependency on socket server
 */
public class ChatController {

    // Chat Message Model
    public static class ChatMessage {
        private int id;
        private int conversationId;
        private int senderId;
        private String senderName;
        private String content;
        private String messageType;
        private String sentDate;
        private boolean isEdited;

        public ChatMessage(int id, int conversationId, int senderId, String senderName,
                          String content, String messageType, String sentDate, boolean isEdited) {
            this.id = id;
            this.conversationId = conversationId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.content = content;
            this.messageType = messageType;
            this.sentDate = sentDate;
            this.isEdited = isEdited;
        }

        // Getters
        public int getId() { return id; }
        public int getConversationId() { return conversationId; }
        public int getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
        public String getContent() { return content; }
        public String getMessageType() { return messageType; }
        public String getSentDate() { return sentDate; }
        public boolean isEdited() { return isEdited; }
    }

    // Conversation Model
    public static class Conversation {
        private int id;
        private String type;
        private String title;
        private String displayName;
        private String lastMessage;
        private String lastActivity;
        private int messageCount;

        public Conversation(int id, String type, String title, String displayName,
                           String lastMessage, String lastActivity, int messageCount) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.displayName = displayName;
            this.lastMessage = lastMessage;
            this.lastActivity = lastActivity;
            this.messageCount = messageCount;
        }

        // Getters
        public int getId() { return id; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDisplayName() { return displayName; }
        public String getLastMessage() { return lastMessage; }
        public String getLastActivity() { return lastActivity; }
        public int getMessageCount() { return messageCount; }
    }

    /**
     * Get or create a private conversation between two users
     */
    public static int getOrCreatePrivateConversation(int user1Id, int user2Id) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        try {
            CallableStatement stmt = connection.prepareCall("{CALL GetOrCreatePrivateConversation(?, ?, ?)}");
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.registerOutParameter(3, Types.INTEGER);

            stmt.execute();

            int conversationId = stmt.getInt(3);
            return conversationId;

        } catch (SQLException e) {
            System.err.println("Error creating conversation: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }
    }

    /**
     * Send a chat message
     */
    public static boolean sendMessage(int conversationId, int senderId, String content, String messageType)
            throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        try {
            CallableStatement stmt = connection.prepareCall("{CALL SendChatMessage(?, ?, ?, ?, ?, ?)}");
            stmt.setInt(1, conversationId);
            stmt.setInt(2, senderId);
            stmt.setString(3, content);
            stmt.setString(4, messageType != null ? messageType : "TEXT");
            stmt.registerOutParameter(5, Types.INTEGER); // message_id
            stmt.registerOutParameter(6, Types.BOOLEAN); // success

            stmt.execute();

            boolean success = stmt.getBoolean(6);

            if (success) {
                System.out.println("Message sent successfully. ID: " + stmt.getInt(5));
            }

            return success;

        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }
    }

    /**
     * Get messages for a conversation with pagination
     */
    public static ObservableList<ChatMessage> getMessages(int conversationId, int userId, int limit, int offset)
            throws SQLException, ClassNotFoundException {
        ObservableList<ChatMessage> messages = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();

        try {
            CallableStatement stmt = connection.prepareCall("{CALL GetChatMessages(?, ?, ?, ?)}");
            stmt.setInt(1, conversationId);
            stmt.setInt(2, userId);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ChatMessage message = new ChatMessage(
                    rs.getInt("id"),
                    conversationId,
                    rs.getInt("sender_id"),
                    rs.getString("sender_name"),
                    rs.getString("content"),
                    rs.getString("message_type"),
                    rs.getString("sent_date"),
                    rs.getBoolean("is_edited")
                );
                messages.add(message);
            }

        } catch (SQLException e) {
            System.err.println("Error getting messages: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }

        return messages;
    }

    /**
     * Get recent messages (last 50)
     */
    public static ObservableList<ChatMessage> getRecentMessages(int conversationId, int userId)
            throws SQLException, ClassNotFoundException {
        return getMessages(conversationId, userId, 50, 0);
    }

    /**
     * Get user's conversations
     */
    public static ObservableList<Conversation> getUserConversations(int userId)
            throws SQLException, ClassNotFoundException {
        ObservableList<Conversation> conversations = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();

        try {
            CallableStatement stmt = connection.prepareCall("{CALL GetUserConversations(?)}");
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Conversation conversation = new Conversation(
                    rs.getInt("conversation_id"),
                    rs.getString("conversation_type"),
                    rs.getString("title"),
                    rs.getString("display_name"),
                    rs.getString("last_message"),
                    rs.getString("last_activity"),
                    rs.getInt("message_count")
                );
                conversations.add(conversation);
            }

        } catch (SQLException e) {
            System.err.println("Error getting conversations: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }

        return conversations;
    }

    /**
     * Update user online status
     */
    public static void updateUserOnlineStatus(int userId, boolean isOnline) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        try {
            CallableStatement stmt = connection.prepareCall("{CALL UpdateUserOnlineStatus(?, ?)}");
            stmt.setInt(1, userId);
            stmt.setBoolean(2, isOnline);

            stmt.execute();

            System.out.println("Updated user " + userId + " online status to: " + isOnline);

        } catch (SQLException e) {
            System.err.println("Error updating online status: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }
    }

    /**
     * Get online users
     */
    public static ObservableList<String> getOnlineUsers() throws SQLException, ClassNotFoundException {
        ObservableList<String> onlineUsers = FXCollections.observableArrayList();
        Connection connection = ConnectionProvider.createConnection();

        try {
            String query = "SELECT full_name, blood_group FROM online_users ORDER BY full_name";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String userInfo = rs.getString("full_name") + " (" + rs.getString("blood_group") + ")";
                onlineUsers.add(userInfo);
            }

        } catch (SQLException e) {
            System.err.println("Error getting online users: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }

        return onlineUsers;
    }

    /**
     * Send a blood request message (special message type)
     */
    public static boolean sendBloodRequestMessage(int senderId, int recipientId, String hospitalInfo, String bloodGroup)
            throws SQLException, ClassNotFoundException {

        // Get or create conversation
        int conversationId = getOrCreatePrivateConversation(senderId, recipientId);

        // Create blood request message
        String message = String.format("🩸 BLOOD REQUEST ALERT 🩸\n\n" +
            "Blood Group Needed: %s\n" +
            "Hospital/Location: %s\n" +
            "Requested by: You\n\n" +
            "⏰ URGENT: Please respond as soon as possible!\n" +
            "💝 Your donation can save a life!", bloodGroup, hospitalInfo);

        return sendMessage(conversationId, senderId, message, "BLOOD_REQUEST");
    }

    /**
     * Send system notification as chat message
     */
    public static void sendSystemMessage(int userId, String title, String content) {
        try {
            Connection connection = ConnectionProvider.createConnection();

            // Insert directly into chat messages as system message
            String query = "INSERT INTO chat_messages (conversation_id, sender_id, content, message_type) " +
                          "SELECT 1, 0, ?, 'SYSTEM' WHERE EXISTS (SELECT 1 FROM person WHERE id = ?)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, "🔔 " + title + "\n\n" + content);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
            connection.close();

        } catch (Exception e) {
            System.err.println("Error sending system message: " + e.getMessage());
        }
    }

    /**
     * Format timestamp for display
     */
    public static String formatTimestamp(String timestamp) {
        try {
            // Parse the timestamp and format it nicely
            LocalDateTime dateTime = LocalDateTime.parse(timestamp.replace(" ", "T"));
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } catch (Exception e) {
            return timestamp; // Return original if parsing fails
        }
    }

    /**
     * Check if user is online
     */
    public static boolean isUserOnline(int userId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        try {
            String query = "SELECT is_online FROM person WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_online");
            }

            return false;

        } finally {
            connection.close();
        }
    }

    /**
     * Get unread message count for user
     */
    public static int getUnreadMessageCount(int userId) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionProvider.createConnection();

        try {
            String query = "SELECT COUNT(*) as unread_count FROM chat_messages cm " +
                          "JOIN chat_participants cp ON cm.conversation_id = cp.conversation_id " +
                          "WHERE cp.person_id = ? AND cm.sender_id != ? " +
                          "AND cm.id > COALESCE(cp.last_read_message_id, 0)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("unread_count");
            }

            return 0;

        } finally {
            connection.close();
        }
    }
}
