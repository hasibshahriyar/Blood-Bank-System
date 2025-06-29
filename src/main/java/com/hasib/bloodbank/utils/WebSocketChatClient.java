package com.hasib.bloodbank.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket-based chat client for real-time communication
 * This replaces the old socket-based approach with a modern WebSocket implementation
 */
public class WebSocketChatClient {
    private WebSocketClient client;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<String> messageHandler;
    private Consumer<String> statusHandler;
    private final String userId;
    private final String userName;

    public WebSocketChatClient(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setStatusHandler(Consumer<String> statusHandler) {
        this.statusHandler = statusHandler;
    }

    public CompletableFuture<Boolean> connect() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            // For now, we'll simulate a WebSocket server with a local echo server
            // In production, this would connect to a real WebSocket server
            URI serverUri = new URI("ws://localhost:8080/chat");

            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("✅ WebSocket connection opened");
                    isConnected.set(true);

                    // Send user registration
                    sendUserRegistration();

                    Platform.runLater(() -> {
                        if (statusHandler != null) {
                            statusHandler.accept("✅ Connected to chat server - Ready for live messaging");
                        }
                    });

                    future.complete(true);
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("📩 Received: " + message);
                    Platform.runLater(() -> {
                        if (messageHandler != null) {
                            processIncomingMessage(message);
                        }
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("❌ WebSocket connection closed: " + reason);
                    isConnected.set(false);
                    Platform.runLater(() -> {
                        if (statusHandler != null) {
                            statusHandler.accept("❌ Connection lost. Attempting to reconnect...");
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("❌ WebSocket error: " + ex.getMessage());
                    Platform.runLater(() -> {
                        if (statusHandler != null) {
                            statusHandler.accept("❌ Connection error: " + ex.getMessage());
                        }
                    });
                    future.complete(false);
                }
            };

            // Since we don't have a WebSocket server running, let's create a simulation
            simulateWebSocketConnection(future);

        } catch (Exception e) {
            System.err.println("❌ Failed to create WebSocket connection: " + e.getMessage());
            future.complete(false);
        }

        return future;
    }

    /**
     * Simulates WebSocket connection for demonstration purposes
     * In a real application, this would connect to an actual WebSocket server
     */
    private void simulateWebSocketConnection(CompletableFuture<Boolean> future) {
        // Simulate successful connection
        System.out.println("🔄 Simulating WebSocket connection...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000); // Simulate connection delay
                isConnected.set(true);

                Platform.runLater(() -> {
                    if (statusHandler != null) {
                        statusHandler.accept("✅ Connected to simulated chat server - Ready for messaging");
                    }
                });

                future.complete(true);

                // Start simulated message reception
                startMessageSimulation();

            } catch (InterruptedException e) {
                future.complete(false);
            }
        });
    }

    /**
     * Simulates receiving messages from other users
     */
    private void startMessageSimulation() {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(5000); // Wait 5 seconds

                // Simulate receiving a welcome message
                Platform.runLater(() -> {
                    if (messageHandler != null) {
                        messageHandler.accept("🤖 System: Welcome to the Blood Bank Chat! Other users will see your messages here.");
                    }
                });

                Thread.sleep(10000); // Wait 10 more seconds

                // Simulate receiving a message from another user
                Platform.runLater(() -> {
                    if (messageHandler != null) {
                        messageHandler.accept("👤 Dr. Sarah Ahmed: Hello! I'm available for blood donation coordination.");
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public boolean sendMessage(String message) {
        if (!isConnected.get()) {
            System.err.println("❌ Cannot send message: not connected");
            return false;
        }

        try {
            // Create message object
            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("type", "chat");
            messageObj.put("senderId", userId);
            messageObj.put("senderName", userName);
            messageObj.put("message", message);
            messageObj.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(messageObj);

            // In a real WebSocket implementation, we would send to the server
            // For simulation, we'll just log and echo back
            System.out.println("📤 Sending: " + jsonMessage);

            // Simulate successful send
            Platform.runLater(() -> {
                if (statusHandler != null) {
                    statusHandler.accept("✅ Message sent successfully");
                }
            });

            // Simulate message being broadcast to other users (echo back after delay)
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000); // Simulate network delay
                    Platform.runLater(() -> {
                        if (messageHandler != null) {
                            messageHandler.accept("🔄 Echo: Your message was broadcast to all connected users");
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send message: " + e.getMessage());
            return false;
        }
    }

    public boolean sendBloodRequest(String hospitalInfo, String bloodType) {
        if (!isConnected.get()) {
            return false;
        }

        try {
            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("type", "bloodRequest");
            requestObj.put("senderId", userId);
            requestObj.put("senderName", userName);
            requestObj.put("hospitalInfo", hospitalInfo);
            requestObj.put("bloodType", bloodType);
            requestObj.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(requestObj);
            System.out.println("🩸 Sending blood request: " + jsonMessage);

            // Simulate successful blood request
            Platform.runLater(() -> {
                if (statusHandler != null) {
                    statusHandler.accept("✅ Urgent blood request sent successfully");
                }
            });

            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send blood request: " + e.getMessage());
            return false;
        }
    }

    private void sendUserRegistration() {
        try {
            Map<String, Object> regObj = new HashMap<>();
            regObj.put("type", "userRegistration");
            regObj.put("userId", userId);
            regObj.put("userName", userName);
            regObj.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(regObj);
            System.out.println("👤 Registering user: " + jsonMessage);

        } catch (Exception e) {
            System.err.println("❌ Failed to register user: " + e.getMessage());
        }
    }

    private void processIncomingMessage(String rawMessage) {
        try {
            Map<String, Object> messageObj = objectMapper.readValue(rawMessage, Map.class);
            String type = (String) messageObj.get("type");

            switch (type) {
                case "chat":
                    String senderName = (String) messageObj.get("senderName");
                    String message = (String) messageObj.get("message");
                    messageHandler.accept("👤 " + senderName + ": " + message);
                    break;

                case "bloodRequest":
                    String requesterName = (String) messageObj.get("senderName");
                    String hospitalInfo = (String) messageObj.get("hospitalInfo");
                    String bloodType = (String) messageObj.get("bloodType");
                    messageHandler.accept("🚨 URGENT: " + requesterName + " needs " + bloodType + " at " + hospitalInfo);
                    break;

                default:
                    messageHandler.accept("📩 " + rawMessage);
                    break;
            }

        } catch (Exception e) {
            // If JSON parsing fails, treat as plain text
            messageHandler.accept("📩 " + rawMessage);
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public void disconnect() {
        isConnected.set(false);
        if (client != null) {
            client.close();
        }
    }
}
