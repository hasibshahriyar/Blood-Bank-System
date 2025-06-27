package com.hasib.bloodbank.socketserver;

import com.hasib.bloodbank.utils.Information;
import com.hasib.bloodbank.utils.NetworkUtility;
import com.hasib.bloodbank.utils.ThreadPoolManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateConnection implements Runnable {
    private final ConcurrentHashMap<Integer, Information> clientList;
    private final NetworkUtility networkUtility;
    private final AtomicBoolean isActive = new AtomicBoolean(true);
    private final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();

    public CreateConnection(ConcurrentHashMap<Integer, Information> clientList, NetworkUtility networkUtility) {
        this.clientList = clientList;
        this.networkUtility = networkUtility;
    }

    @Override
    public void run() {
        try {
            if (!networkUtility.isConnected()) {
                System.err.println("Cannot create connection - NetworkUtility is not connected");
                return;
            }

            // Read the user ID from the client
            Object userIdObj = networkUtility.read();
            if (userIdObj == null) {
                System.err.println("Failed to read user ID from client");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(userIdObj.toString());
            } catch (NumberFormatException e) {
                System.err.println("Invalid user ID received: " + userIdObj);
                return;
            }

            System.out.println("User: " + userId + " connected");

            // Create Information object for the client
            Information clientInfo = new Information(userId, networkUtility);
            clientList.put(userId, clientInfo);

            // Start the reader-writer server for this client in a managed thread
            threadPool.executeNetworkTask(new ReaderWriterServer(userId, networkUtility, clientList));

            System.out.println("Client " + userId + " setup complete. Total clients: " + clientList.size());

        } catch (Exception e) {
            System.err.println("Error in CreateConnection: " + e.getMessage());
            e.printStackTrace();

            // Clean up connection on error
            if (networkUtility != null) {
                networkUtility.closeConnection();
            }
        }
    }

    public void stop() {
        isActive.set(false);
    }

    public boolean isActive() {
        return isActive.get();
    }
}
