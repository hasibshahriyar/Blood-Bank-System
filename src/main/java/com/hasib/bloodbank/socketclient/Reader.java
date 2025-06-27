package com.hasib.bloodbank.socketclient;

import com.hasib.bloodbank.UserProfileController;
import com.hasib.bloodbank.utils.NetworkUtility;
import com.hasib.bloodbank.utils.ThreadPoolManager;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

public class Reader implements Runnable {
    private final NetworkUtility networkUtility;
    private final UserProfileController userProfileController;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();

    public Reader(NetworkUtility networkUtility, UserProfileController userProfileController) {
        this.networkUtility = networkUtility;
        this.userProfileController = userProfileController;
    }

    @Override
    public void run() {
        System.out.println("Reader thread started for user profile communication");

        while (running.get() && networkUtility.isConnected()) {
            try {
                Object receivedObject = networkUtility.read();

                if (receivedObject != null) {
                    String actualMessage = receivedObject.toString();
                    System.out.println("Received message: " + actualMessage);

                    // Process message on JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            processMessage(actualMessage);
                        } catch (Exception e) {
                            System.err.println("Error processing message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }

                // Small delay to prevent excessive CPU usage
                Thread.sleep(100);

            } catch (InterruptedException e) {
                System.out.println("Reader thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("Error in reader thread: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
        }

        System.out.println("Reader thread stopped");
    }

    private void processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        try {
            // Parse the message and handle different message types
            String[] parts = message.split("\\$");

            if (parts.length >= 4) {
                String senderId = parts[0];
                String receiverId = parts[1];
                String senderName = parts[2];
                String messageType = parts[3];

                switch (messageType) {
                    case "requestForBlood":
                        handleBloodRequest(senderId, senderName, parts);
                        break;
                    case "message":
                        handleChatMessage(senderName, parts);
                        break;
                    case "donationAccepted":
                        handleDonationAccepted(senderName);
                        break;
                    case "donationDeclined":
                        handleDonationDeclined(senderName);
                        break;
                    default:
                        handleGenericMessage(message);
                        break;
                }
            } else {
                handleGenericMessage(message);
            }

        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            handleGenericMessage(message);
        }
    }

    private void handleBloodRequest(String senderId, String senderName, String[] parts) {
        if (parts.length >= 5) {
            String hospitalName = parts[4];
            String formattedMessage = "🩸 Blood Request from " + senderName +
                    " for " + hospitalName + " hospital";

            if (userProfileController != null) {
                userProfileController.handleMessage(formattedMessage);
            }

            System.out.println("Blood request received from user " + senderId + " (" + senderName + ")");
        }
    }

    private void handleChatMessage(String senderName, String[] parts) {
        if (parts.length >= 5) {
            String messageContent = parts[4];
            String formattedMessage = senderName + ": " + messageContent;

            if (userProfileController != null) {
                userProfileController.handleMessage(formattedMessage);
            }
        }
    }

    private void handleDonationAccepted(String senderName) {
        String message = "✅ " + senderName + " has accepted your donation request!";
        if (userProfileController != null) {
            userProfileController.handleMessage(message);
        }
    }

    private void handleDonationDeclined(String senderName) {
        String message = "❌ " + senderName + " has declined your donation request.";
        if (userProfileController != null) {
            userProfileController.handleMessage(message);
        }
    }

    private void handleGenericMessage(String message) {
        if (userProfileController != null) {
            userProfileController.handleMessage(message);
        }
    }

    public void stop() {
        running.set(false);
        System.out.println("Reader stop requested");
    }

    public boolean isRunning() {
        return running.get();
    }
}
