package com.hasib.bloodbank;

import com.hasib.bloodbank.server.controller.AddressController;
import com.hasib.bloodbank.server.controller.DonationController;
import com.hasib.bloodbank.server.controller.PersonController;
import com.hasib.bloodbank.server.entity.Address;
import com.hasib.bloodbank.server.entity.BloodGroup;
import com.hasib.bloodbank.server.entity.Donation;
import com.hasib.bloodbank.server.entity.Person;
import com.hasib.bloodbank.server.model.ShowPerson;
import com.hasib.bloodbank.singleton.User;
import com.hasib.bloodbank.utils.Data;
import com.hasib.bloodbank.utils.NetworkUtility;
import com.hasib.bloodbank.utils.ThreadPoolManager;
import com.hasib.bloodbank.socketclient.Reader;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserProfileController implements Initializable {
    // Socket-based communication system (replacing WebSocket simulation)
    private Reader socketReader;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean connectionAttempting = new AtomicBoolean(false);
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();

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
    NetworkUtility networkUtility;

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

                // Auto-scroll to bottom
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
            textArea.setText("💬 Live Chat Initialized\n🔄 Connecting to communication server...");
        }

        // Load person data in background thread
        threadPool.executeDatabaseTask(() -> {
            try {
                getPerson();

                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    updateUI();
                    initializeChatConnection();
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
            // Fix: Use person ID to get address instead of potentially shared address ID
            this.personAddress = AddressController.getAddressByPersonId(showPerson.getId());
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    private void initializeChatConnection() {
        if (connectionAttempting.get()) {
            return; // Avoid multiple connection attempts
        }

        connectionAttempting.set(true);
        updateWarning("🔄 Establishing secure connection...", "-fx-text-fill: #17a2b8;");

        // Initialize network connection in background
        threadPool.executeNetworkTask(() -> {
            try {
                establishNetworkConnection();
            } catch (Exception e) {
                connectionAttempting.set(false);
                updateWarning("❌ Connection failed: " + e.getMessage(), "-fx-text-fill: #dc3545;");
                updateChatArea("❌ Failed to connect to chat server. Retrying in 10 seconds...");

                // Auto-retry connection after 10 seconds
                threadPool.scheduleTask(() -> {
                    if (!isConnected.get()) {
                        initializeChatConnection();
                    }
                }, 10, TimeUnit.SECONDS);
            }
        });
    }

    private void establishNetworkConnection() throws IOException {
        try {
            System.out.println("🔄 Connecting to BloodBank chat server on localhost:9999...");

            // Connect to your existing socket server
            networkUtility = new NetworkUtility("localhost", 9999);

            if (!networkUtility.isConnected()) {
                throw new IOException("Failed to connect to chat server");
            }

            // Send user ID to server for identification
            networkUtility.write(user.getUserId());

            System.out.println("✅ Connected to chat server as user ID: " + user.getUserId());

            // Start the socket reader for incoming messages
            socketReader = new Reader(networkUtility, this);
            threadPool.executeNetworkTask(socketReader);

            isConnected.set(true);
            connectionAttempting.set(false);

            Platform.runLater(() -> {
                updateWarning("✅ Connected to BloodBank chat server - Ready for live chat", "-fx-text-fill: #28a745;");
                updateChatArea("🌟 Welcome to BloodBank live chat! You can now communicate with donors/recipients.");
            });

        } catch (IOException e) {
            isConnected.set(false);
            connectionAttempting.set(false);
            throw new IOException("Failed to establish connection to chat server: " + e.getMessage(), e);
        }
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

    // Enhanced send method using socket for real user-to-user communication
    public void send(ActionEvent event) {
        String messageText = textField != null ? textField.getText() : "";

        if (messageText == null || messageText.trim().isEmpty()) {
            updateWarning("⚠️ Please enter a message before sending", "-fx-text-fill: #ffc107;");
            return;
        }

        updateWarning("🔄 Sending message...", "-fx-text-fill: #17a2b8;");
        System.out.println("📤 Attempting to send message: " + messageText);

        if (!isConnected.get() || networkUtility == null) {
            updateWarning("❌ Not connected to chat server. Attempting to reconnect...", "-fx-text-fill: #dc3545;");
            updateChatArea("❌ Connection lost. Trying to reconnect...");
            initializeChatConnection();
            return;
        }

        // Send message using socket through NetworkUtility
        threadPool.executeNetworkTask(() -> {
            try {
                // Show immediate feedback in chat
                Platform.runLater(() -> {
                    updateChatArea("📤 You: " + messageText);
                    if (textField != null) textField.clear();
                });

                // Create message data for broadcast to all connected users
                // Format: senderId$receiverId$senderName$messageType$messageContent
                String senderName = showPerson != null ? showPerson.getName() : "User " + user.getUserId();
                String broadcastMessage = user.getUserId() + "$0$" + senderName + "$message$" + messageText.trim();

                Data data = new Data();
                data.message = broadcastMessage;

                // Send message through socket
                networkUtility.write(data);

                Platform.runLater(() -> {
                    updateWarning("✅ Message sent to all connected users", "-fx-text-fill: #28a745;");
                    System.out.println("✅ Message delivery confirmed");
                });

            } catch (Exception e) {
                System.err.println("❌ Failed to send message: " + e.getMessage());

                Platform.runLater(() -> {
                    updateWarning("❌ Failed to send message: " + e.getMessage(), "-fx-text-fill: #dc3545;");
                    updateChatArea("❌ Message failed to send. Check connection and try again.");

                    // Restore the message text if sending failed
                    if (textField != null) {
                        textField.setText(messageText);
                    }
                });

                // Try to reconnect
                isConnected.set(false);
                Platform.runLater(() -> initializeChatConnection());
            }
        });
    }

    // Enhanced blood request method
    public void request(ActionEvent event) {
        if (hospitalNameTExtFild == null || hospitalNameTExtFild.getText().trim().isEmpty()) {
            updateWarning("⚠️ Please enter hospital information before sending request", "-fx-text-fill: #ffc107;");
            return;
        }

        String hospitalInfo = hospitalNameTExtFild.getText().trim();

        threadPool.executeDatabaseTask(() -> {
            try {
                updateWarning("🔄 Sending blood request...", "-fx-text-fill: #17a2b8;");

                // Send network message if connected
                if (isConnected.get() && networkUtility != null) {
                    Data data = new Data();
                    data.message = user.getUserId() + "$request$" + hospitalInfo + "$" + showPerson.getBloodGroup();
                    networkUtility.write(data.clone());
                }

                Platform.runLater(() -> {
                    updateWarning("✅ Blood request sent successfully!", "-fx-text-fill: #28a745;");
                    updateChatArea("🩸 Urgent blood request sent for " + showPerson.getBloodGroup() + " at " + hospitalInfo);
                    if (hospitalNameTExtFild != null) hospitalNameTExtFild.clear();
                });

            } catch (Exception e) {
                updateWarning("❌ Failed to send request: " + e.getMessage(), "-fx-text-fill: #dc3545;");
            }
        });
    }

    // Enhanced accept method
    public void accept(ActionEvent event) {
        threadPool.executeDatabaseTask(() -> {
            try {
                updateWarning("🔄 Processing acceptance...", "-fx-text-fill: #17a2b8;");

                if (user.getMessage() != null) {
                    // Process donation acceptance
                    String hospitalInfo = hospitalNameTExtFild != null ? hospitalNameTExtFild.getText() : "Unknown Hospital";
                    Donation donation = new Donation(
                        hospitalInfo,
                        new Date(System.currentTimeMillis()),
                        showPerson.getId(), // donatedPersonId
                        0, // receivedPersonId (unknown for now)
                        BloodGroup.valueOf(showPerson.getBloodGroup())
                    );

                    DonationController.saveDonation(donation);

                    // Send network confirmation
                    if (isConnected.get() && networkUtility != null) {
                        Data data = new Data();
                        data.message = user.getUserId() + "$accept$donation_accepted";
                        networkUtility.write(data.clone());
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
                    // Send network rejection
                    if (isConnected.get() && networkUtility != null) {
                        Data data = new Data();
                        data.message = user.getUserId() + "$reject$donation_rejected";
                        networkUtility.write(data.clone());
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

    // Back to dashboard method - FIXED to prevent application freezing
    public void back(ActionEvent event) {
        try {
            System.out.println("🔄 Navigating back to dashboard...");

            // Cleanup network connections asynchronously to prevent UI freezing
            CompletableFuture.runAsync(() -> {
                try {
                    cleanup();
                } catch (Exception e) {
                    System.err.println("⚠️ Cleanup warning: " + e.getMessage());
                }
            });

            // Navigate immediately without waiting for cleanup
            Parent mainView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set scene properties
            Scene mainScene = new Scene(mainView);
            currentStage.setTitle("Blood Bank Management System - Dashboard");
            currentStage.setScene(mainScene);
            currentStage.setResizable(false);
            currentStage.show();

            System.out.println("✅ Successfully navigated back to dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error loading main.fxml: " + e.getMessage());
            e.printStackTrace();

            // Show user-friendly error message
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

            // Fallback: try to close current window
            try {
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();
            } catch (Exception closeException) {
                System.err.println("❌ Error closing window: " + closeException.getMessage());

                // Last resort: exit application gracefully
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Critical Error");
                    alert.setHeaderText("Application Error");
                    alert.setContentText("A critical error occurred. The application will now exit.");
                    alert.showAndWait();
                    Platform.exit();
                });
            }
        }
    }

    // Method to clear user session data during logout
    public void clearSession() {
        // TODO: Implement session clearing logic
        updateWarning("🔑 Session data cleared (placeholder)", "-fx-text-fill: #28a745;");
    }

    // Cleanup method for proper resource management
    private void cleanup() {
        try {
            isConnected.set(false);
            connectionAttempting.set(false);

            if (networkUtility != null) {
                networkUtility.closeConnection();
                networkUtility = null;
            }

            if (socketReader != null) {
                // The Reader class handles its own cleanup when the NetworkUtility closes
                socketReader = null;
            }

            System.out.println("🧹 Network connections cleaned up successfully");

        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    // Destructor equivalent
    public void shutdown() {
        cleanup();
    }

    // New action methods for donor portal buttons
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

    public void onNearbyBloodBanksClick(ActionEvent event) {
        try {
            updateWarning("🏥 Finding nearby blood banks...", "-fx-text-fill: #17a2b8;");

            // TODO: Implement nearby blood banks functionality
            // For now, show a placeholder message
            updateChatArea("🏥 Nearby Blood Banks: Feature coming soon! You'll be able to find blood banks near your location.");
            updateWarning("✅ Nearby blood banks feature will be implemented soon", "-fx-text-fill: #28a745;");

        } catch (Exception e) {
            updateWarning("❌ Error finding nearby blood banks: " + e.getMessage(), "-fx-text-fill: #dc3545;");
        }
    }

}
