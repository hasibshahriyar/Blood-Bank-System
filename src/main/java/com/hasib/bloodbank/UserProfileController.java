package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.AddressController;
import com.hasib.bloodbank.server.controller.BloodRequestController;
import com.hasib.bloodbank.server.controller.ChatController;
import com.hasib.bloodbank.server.controller.DonationController;
import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.Address;
import com.hasib.bloodbank.server.entity.BloodGroup;
import com.hasib.bloodbank.server.entity.Donation;
import com.hasib.bloodbank.server.entity.Person;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.singleton.User;
import com.hasib.bloodbank.utils.ThreadPoolManager;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UserProfileController implements Initializable {
    // Database-backed chat system (replacing socket-based communication)
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();
    private int currentConversationId = 0;
    private boolean isOnline = false;

    // FXML Components
    public JFXTextArea textArea;
    public Label addressLabel;
    public Label email;
    public Label dateOfBirth;
    public Label mobileNum;
    public Label gender;
    public Label bloodGroup;
    public Label name;
    public TextField hospitalNameTExtFild;
    public Text warning;
    public TextField textField;

    // Data members
    User user = User.getInstance();
    ShowPerson showPerson;
    Person person;
    Address personAddress;

    // Enhanced helper methods for better UI feedback
    private void updateWarning(String text, String style) {
        Platform.runLater(() -> {
            if (warning != null) {
                warning.setText(text);
                if (style != null) {
                    warning.setStyle(style);
                }
            } else {
                System.out.println("Warning field is null. Message would be: " + text);
            }
        });
    }

    private void updateWarning(String text) {
        updateWarning(text, "-fx-text-fill: #6c757d; -fx-font-weight: normal;");
    }

    private void updateChatArea(String message) {
        Platform.runLater(() -> {
            if (textArea != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                textArea.appendText("\n[" + timestamp + "] " + message);
                textArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    // ...existing getter/setter methods...
    public ShowPerson getShowPerson() {
        return showPerson;
    }

    public void setShowPerson(ShowPerson showPerson) {
        this.showPerson = showPerson;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.showPerson = User.getInstance().getShowPerson();

        // Initialize chat area with welcome message
        if (textArea != null) {
            textArea.setText("💬 Database Chat System Initialized\n🔄 Setting up communication...");
        }

        // Load person data in background thread
        threadPool.executeDatabaseTask(() -> {
            try {
                getPerson();
                initializeDatabaseChat();

                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    updateUI();
                });
            } catch (Exception e) {
                updateWarning("Error loading profile: " + e.getMessage(), "-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
        });

        // Setup text field for Enter key press
        if (textField != null) {
            textField.setOnAction(this::send);
        }
    }

    private void updateUI() {
        if (showPerson != null) {
            name.setText(showPerson.getName());
            email.setText(showPerson.getEmail());
            mobileNum.setText(showPerson.getPhoneNo());
            bloodGroup.setText(showPerson.getBloodGroup());
        }

        if (person != null) {
            dateOfBirth.setText(person.getDateOfBirth());
            gender.setText(person.getGender());
        }
    }

    void getPerson() {
        try {
            this.person = PersonController.getPersonById(showPerson.getId());
            this.personAddress = AddressController.getAddressByPersonId(showPerson.getId());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    private void initializeDatabaseChat() {
        updateWarning("🔄 Initializing database chat system...", "-fx-text-fill: #17a2b8;");

        threadPool.executeDatabaseTask(() -> {
            try {
                // Set user as online
                ChatController.updateUserOnlineStatus(user.getUserId(), true);
                isOnline = true;

                // Get or create conversation with the donor/recipient
                if (showPerson != null) {
                    currentConversationId = ChatController.getOrCreatePrivateConversation(
                        user.getUserId(), showPerson.getId());

                    // Load recent messages
                    loadRecentMessages();
                }

                Platform.runLater(() -> {
                    updateWarning("✅ Database chat system ready - Secure messaging active", "-fx-text-fill: #28a745;");
                    updateChatArea("🌟 Welcome to secure BloodBank messaging! Chat directly with " +
                        (showPerson != null ? showPerson.getName() : "other users"));
                });

                // Start periodic message refresh
                scheduleMessageRefresh();

            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateWarning("❌ Chat initialization failed: " + e.getMessage(), "-fx-text-fill: #dc3545;");
                    updateChatArea("❌ Unable to connect to messaging system. Please try again later.");
                });
            }
        });
    }

    private void loadRecentMessages() throws SQLException, ClassNotFoundException {
        if (currentConversationId > 0) {
            ObservableList<ChatController.ChatMessage> messages =
                ChatController.getRecentMessages(currentConversationId, user.getUserId());

            Platform.runLater(() -> {
                textArea.clear();
                textArea.appendText("💬 Recent Messages\n" + "=".repeat(50));

                for (ChatController.ChatMessage message : messages) {
                    String formattedTime = ChatController.formatTimestamp(message.getSentDate());
                    String prefix = message.getSenderId() == user.getUserId() ? "📤 You" : "📩 " + message.getSenderName();

                    textArea.appendText("\n[" + formattedTime + "] " + prefix + ": " + message.getContent());
                }

                textArea.appendText("\n" + "=".repeat(50) + "\n");
                textArea.setScrollTop(Double.MAX_VALUE);
            });
        }
    }

    private void scheduleMessageRefresh() {
        // Refresh messages every 30 seconds
        threadPool.schedulePeriodicTask(() -> {
            if (isOnline && currentConversationId > 0) {
                try {
                    loadRecentMessages();
                } catch (Exception e) {
                    System.err.println("Error refreshing messages: " + e.getMessage());
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // Enhanced send method using database chat
    public void send(ActionEvent event) {
        String messageText = textField != null ? textField.getText() : "";

        if (messageText == null || messageText.trim().isEmpty()) {
            updateWarning("⚠️ Please enter a message before sending", "-fx-text-fill: #ffc107;");
            return;
        }

        if (currentConversationId == 0) {
            updateWarning("❌ No active conversation. Please try again.", "-fx-text-fill: #dc3545;");
            return;
        }

        updateWarning("🔄 Sending message...", "-fx-text-fill: #17a2b8;");

        threadPool.executeDatabaseTask(() -> {
            try {
                // Send message through database
                boolean success = ChatController.sendMessage(
                    currentConversationId,
                    user.getUserId(),
                    messageText.trim(),
                    "TEXT"
                );

                Platform.runLater(() -> {
                    if (success) {
                        updateWarning("✅ Message sent successfully", "-fx-text-fill: #28a745;");
                        updateChatArea("📤 You: " + messageText);
                        if (textField != null) textField.clear();
                    } else {
                        updateWarning("❌ Failed to send message", "-fx-text-fill: #dc3545;");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateWarning("❌ Database error: " + e.getMessage(), "-fx-text-fill: #dc3545;");
                    updateChatArea("❌ Message failed to send. Please try again.");
                });
            }
        });
    }

    // Enhanced blood request method with database integration and chat
    public void request(ActionEvent event) {
        if (hospitalNameTExtFild == null || hospitalNameTExtFild.getText().trim().isEmpty()) {
            updateWarning("⚠️ Please enter hospital/emergency information before sending request", "-fx-text-fill: #ffc107;");
            return;
        }

        String hospitalInfo = hospitalNameTExtFild.getText().trim();

        if (showPerson == null) {
            updateWarning("❌ No donor selected for blood request", "-fx-text-fill: #dc3545;");
            return;
        }

        threadPool.executeDatabaseTask(() -> {
            try {
                updateWarning("🔄 Sending blood request...", "-fx-text-fill: #17a2b8;");

                // Create blood request in database
                String requestMessage = String.format("Urgent blood request for %s blood group at %s. Please respond if you can help!",
                    showPerson.getBloodGroup(), hospitalInfo);

                boolean requestSent = BloodRequestController.sendBloodRequest(
                    user.getUserId(),
                    showPerson.getId(),
                    requestMessage
                );

                if (requestSent) {
                    // Send blood request message through chat system
                    boolean chatSent = ChatController.sendBloodRequestMessage(
                        user.getUserId(),
                        showPerson.getId(),
                        hospitalInfo,
                        showPerson.getBloodGroup()
                    );

                    Platform.runLater(() -> {
                        updateWarning("✅ Blood request sent successfully!", "-fx-text-fill: #28a745;");
                        updateChatArea("🩸 Blood request sent to " + showPerson.getName() + " for " + showPerson.getBloodGroup() + " blood");
                        updateChatArea("📱 " + showPerson.getName() + " will receive both notification and chat message");
                        if (hospitalNameTExtFild != null) hospitalNameTExtFild.clear();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Blood Request Sent");
                        alert.setHeaderText("Request Successfully Delivered");
                        alert.setContentText("Your blood request has been sent to " + showPerson.getName() +
                            ". They will receive notifications and can respond through the app.");
                        alert.showAndWait();
                    });
                } else {
                    Platform.runLater(() -> {
                        updateWarning("❌ Failed to send blood request", "-fx-text-fill: #dc3545;");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Request Failed");
                        alert.setHeaderText("Unable to Send Blood Request");
                        alert.setContentText("There was an error sending your blood request. Please try again.");
                        alert.showAndWait();
                    });
                }

            } catch (SQLException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    updateWarning("❌ Database error: " + e.getMessage(), "-fx-text-fill: #dc3545;");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("Blood Request Failed");
                    alert.setContentText("Database error occurred while sending blood request: " + e.getMessage());
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateWarning("❌ Unexpected error: " + e.getMessage(), "-fx-text-fill: #dc3545;");
                });
            }
        });
    }

    // Enhanced accept method
    public void accept(ActionEvent event) {
        threadPool.executeDatabaseTask(() -> {
            try {
                updateWarning("🔄 Processing acceptance...", "-fx-text-fill: #17a2b8;");

                if (user.getMessage() != null) {
                    String hospitalInfo = hospitalNameTExtFild != null ? hospitalNameTExtFild.getText() : "Unknown Hospital";
                    Donation donation = new Donation(
                        hospitalInfo,
                        new Date(System.currentTimeMillis()),
                        showPerson.getId(),
                        0,
                        BloodGroup.valueOf(showPerson.getBloodGroup())
                    );

                    DonationController.saveDonation(donation);

                    // Send acceptance message through chat
                    if (currentConversationId > 0) {
                        ChatController.sendMessage(
                            currentConversationId,
                            user.getUserId(),
                            "✅ Great news! I have accepted your blood donation request. Let's coordinate the details.",
                            "TEXT"
                        );
                    }

                    Platform.runLater(() -> {
                        updateWarning("✅ Donation request accepted!", "-fx-text-fill: #28a745;");
                        updateChatArea("✅ You accepted the donation request. Thank you for saving lives!");
                    });

                    user.setMessage(null);
                } else {
                    updateWarning("⚠️ No pending donation request to accept", "-fx-text-fill: #ffc107;");
                }

            } catch (Exception e) {
                updateWarning("❌ Error accepting donation: " + e.getMessage(), "-fx-text-fill: #dc3545;");
            }
        });
    }

    // Enhanced reject method
    public void reject(ActionEvent event) {
        threadPool.executeDatabaseTask(() -> {
            try {
                updateWarning("🔄 Processing rejection...", "-fx-text-fill: #17a2b8;");

                if (user.getMessage() != null) {
                    // Send rejection message through chat
                    if (currentConversationId > 0) {
                        ChatController.sendMessage(
                            currentConversationId,
                            user.getUserId(),
                            "❌ I'm sorry, but I cannot fulfill your blood donation request at this time. Please try contacting other donors.",
                            "TEXT"
                        );
                    }

                    Platform.runLater(() -> {
                        updateWarning("📝 Donation request rejected", "-fx-text-fill: #ffc107;");
                        updateChatArea("❌ You rejected the donation request");
                    });

                    user.setMessage(null);
                } else {
                    updateWarning("⚠️ No donation request to reject", "-fx-text-fill: #ffc107;");
                }

            } catch (Exception e) {
                updateWarning("❌ Error rejecting donation: " + e.getMessage(), "-fx-text-fill: #dc3545;");
            }
        });
    }

    // Address view method
    public void onAddressClick(ActionEvent event) {
        if (personAddress != null) {
            String fullAddress = String.format("🏠 Address:\n%s\n%s, %s\n%s",
                    personAddress.getSubDistrict() != null ? personAddress.getSubDistrict() : "N/A",
                    personAddress.getDistrict() != null ? personAddress.getDistrict() : "N/A",
                    personAddress.getDivision() != null ? personAddress.getDivision() : "N/A",
                    personAddress.getCountry() != null ? personAddress.getCountry() : "N/A"
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Address Information");
            alert.setHeaderText("User Address Details");
            alert.setContentText(fullAddress);
            alert.showAndWait();
        } else {
            updateWarning("⚠️ Address information not available", "-fx-text-fill: #ffc107;");
        }
    }

    // Back to dashboard method with proper cleanup
    public void back(ActionEvent event) {
        try {
            System.out.println("🔄 Navigating back to dashboard...");

            // Cleanup database connections asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    cleanup();
                } catch (Exception e) {
                    System.err.println("⚠️ Cleanup warning: " + e.getMessage());
                }
            });

            // Navigate immediately
            Parent mainView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene mainScene = new Scene(mainView);
            currentStage.setTitle("Blood Bank Management System - Dashboard");
            currentStage.setScene(mainScene);
            currentStage.setResizable(false);
            currentStage.show();

            System.out.println("✅ Successfully navigated back to dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error loading main.fxml: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Navigation Error");
                alert.setHeaderText("Unable to return to dashboard");
                alert.setContentText("There was an error loading the main dashboard. Please restart the application.");
                alert.showAndWait();
            });

        } catch (Exception e) {
            System.err.println("❌ Unexpected error during navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cleanup method for proper resource management
    private void cleanup() {
        try {
            isOnline = false;

            // Set user as offline in database
            if (user != null && user.getUserId() > 0) {
                ChatController.updateUserOnlineStatus(user.getUserId(), false);
            }

            System.out.println("🧹 Database connections cleaned up successfully");

        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    // Destructor equivalent
    public void shutdown() {
        cleanup();
    }

    // Enhanced message handling with better formatting
    public void handleMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            updateChatArea("📩 " + message);

            // Handle urgent messages with simple console output
            if (message.toLowerCase().contains("urgent") || message.toLowerCase().contains("emergency")) {
                System.out.println("🚨 URGENT MESSAGE RECEIVED: " + message);
                updateWarning("🚨 Urgent message received!", "-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
        }
    }

    // Missing FXML method - My Donation History
    public void onMyDonationHistoryClick(ActionEvent event) {
        try {
            updateWarning("📋 Loading donation history...", "-fx-text-fill: #17a2b8;");

            // TODO: Implement donation history functionality
            // For now, show a placeholder message
            updateChatArea("📋 Donation History: Feature coming soon! Your donation records will be displayed here.");
            updateWarning("✅ Donation history feature will be implemented soon", "-fx-text-fill: #28a745;");

        } catch (Exception e) {
            updateWarning("❌ Error loading donation history: " + e.getMessage(), "-fx-text-fill: #dc3545;");
        }
    }

    // Missing FXML method - Blood Request Alerts
    public void onBloodRequestAlertsClick(ActionEvent event) {
        try {
            updateWarning("🔔 Checking blood request alerts...", "-fx-text-fill: #17a2b8;");

            // TODO: Implement blood request alerts functionality
            // For now, show a placeholder message
            updateChatArea("🔔 Blood Request Alerts: You will receive notifications for urgent blood requests in your area.");
            updateWarning("✅ Blood request alerts feature will be implemented soon", "-fx-text-fill: #28a745;");

        } catch (Exception e) {
            updateWarning("❌ Error loading blood request alerts: " + e.getMessage(), "-fx-text-fill: #dc3545;");
        }
    }
}
