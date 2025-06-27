package com.hasib.bloodbank.socketserver;

import com.hasib.bloodbank.utils.Data;
import com.hasib.bloodbank.utils.Information;
import com.hasib.bloodbank.utils.NetworkUtility;
import com.hasib.bloodbank.utils.ThreadPoolManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReaderWriterServer implements Runnable {
    private final int id;
    private final NetworkUtility networkUtility;
    private final ConcurrentHashMap<Integer, Information> clientList;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();

    public ReaderWriterServer(int id, NetworkUtility networkUtility, ConcurrentHashMap<Integer, Information> clientList) {
        this.id = id;
        this.networkUtility = networkUtility;
        this.clientList = clientList;
    }

    //    words[0] = Sender Id
    //    words[1] = Receiver Id
    //    words[2] = Sender Name
    //    words[3] = keyword
    //    words[4] = message/null
    @Override
    public void run() {
        System.out.println("ReaderWriterServer started for client: " + id);

        try {
            while (isRunning.get() && networkUtility.isConnected()) {
                Object obj = networkUtility.read();

                if (obj == null) {
                    System.out.println("Received null object, client " + id + " may have disconnected");
                    break;
                }

                if (!(obj instanceof Data)) {
                    System.err.println("Received object is not Data type: " + obj.getClass().getSimpleName());
                    continue;
                }

                Data dataObj = (Data) obj;
                String actualMessage = dataObj.message;
                System.out.println("Processing message from client " + id + ": " + actualMessage);

                // Process message in background thread to prevent blocking
                threadPool.executeNetworkTask(() -> processMessage(actualMessage));
            }
        } catch (Exception e) {
            System.err.println("Error in ReaderWriterServer for client " + id + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void processMessage(String actualMessage) {
        try {
            String[] words = actualMessage.split("\\$");

            if (words.length < 4) {
                System.err.println("Invalid message format: " + actualMessage);
                return;
            }

            int senderId = Integer.parseInt(words[0]);
            int receiverId = Integer.parseInt(words[1]);
            String senderName = words[2];
            String messageType = words[3];
            String messageContent = words.length > 4 ? words[4] : "";

            Information receiverInfo = clientList.get(receiverId);
            if (receiverInfo == null) {
                System.out.println("Receiver " + receiverId + " is not online");
                return;
            }

            if (!receiverInfo.networkUtility.isConnected()) {
                System.out.println("Receiver " + receiverId + " connection is closed");
                clientList.remove(receiverId);
                return;
            }

            switch (messageType) {
                case "text":
                    handleTextMessage(senderId, senderName, messageContent, receiverInfo);
                    break;
                case "requestForBlood":
                    handleBloodRequest(senderId, senderName, messageContent, receiverInfo);
                    break;
                case "donateBlood":
                    handleDonateBlood(senderId, senderName, messageContent, receiverInfo);
                    break;
                case "message":
                    handleGenericMessage(senderId, senderName, messageContent, receiverInfo);
                    break;
                default:
                    System.out.println("Unknown message type: " + messageType);
                    break;
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid user ID format in message: " + actualMessage);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTextMessage(int senderId, String senderName, String messageContent, Information receiverInfo) {
        String msgToSend = senderId + "$" + senderName + "$" + "text" + "$" + messageContent;
        System.out.println("Sending text message: " + msgToSend);
        receiverInfo.networkUtility.write(msgToSend);
    }

    private void handleBloodRequest(int senderId, String senderName, String messageContent, Information receiverInfo) {
        String msgToSend = senderId + "$" + senderName + "$" + "requestForBlood" + "$" + messageContent;
        System.out.println("Sending blood request: " + msgToSend);
        receiverInfo.networkUtility.write(msgToSend);
    }

    private void handleDonateBlood(int senderId, String senderName, String messageContent, Information receiverInfo) {
        String msgToSend = senderId + "$" + senderName + "$" + "donateBlood" + "$" + "Want to Donate you blood" + "$" + messageContent;
        System.out.println("Sending donate blood message: " + msgToSend);
        receiverInfo.networkUtility.write(msgToSend);
    }

    private void handleGenericMessage(int senderId, String senderName, String messageContent, Information receiverInfo) {
        String msgToSend = senderId + "$" + senderName + "$" + "message" + "$" + messageContent;
        System.out.println("Sending generic message: " + msgToSend);
        receiverInfo.networkUtility.write(msgToSend);
    }

    private void cleanup() {
        isRunning.set(false);

        // Remove this client from the client list
        Information clientInfo = clientList.remove(id);
        if (clientInfo != null) {
            System.out.println("Client " + id + " disconnected and removed from client list");

            // Close the network connection
            if (clientInfo.networkUtility != null) {
                clientInfo.networkUtility.closeConnection();
            }
        }

        System.out.println("ReaderWriterServer cleanup complete for client: " + id);
    }

    public void stop() {
        isRunning.set(false);
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
